#!/usr/bin/env bash

set -euo pipefail

AWS_PROFILE="engineering-bench"
AWS_REGION="us-east-2"
AWS_ACCOUNT_ID="437045580573"

ROLE_NAME="engineering-bench-sandbox-task-role"

echo "=============================================="
echo "Engineering Bench - Sandbox Task IAM Role"
echo "=============================================="
echo
echo "AWS Profile: ${AWS_PROFILE}"
echo "AWS Region:  ${AWS_REGION}"
echo "AWS Account: ${AWS_ACCOUNT_ID}"
echo "Role:        ${ROLE_NAME}"
echo

echo "Checking AWS identity..."

CURRENT_ACCOUNT_ID="$(
    aws sts get-caller-identity \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --query Account \
        --output text
)"

if [[ "$CURRENT_ACCOUNT_ID" != "$AWS_ACCOUNT_ID" ]]; then
    echo
    echo "ERROR: AWS account mismatch."
    echo "Expected: ${AWS_ACCOUNT_ID}"
    echo "Current:  ${CURRENT_ACCOUNT_ID}"
    exit 1
fi

echo "AWS account verified."
echo

TMP_DIR="$(mktemp -d)"

cleanup() {
    rm -rf "$TMP_DIR"
}

trap cleanup EXIT

TRUST_POLICY="${TMP_DIR}/trust-policy.json"
PERMISSIONS_POLICY="${TMP_DIR}/permissions-policy.json"

cat > "$TRUST_POLICY" <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Action": "sts:AssumeRole",
      "Condition": {
        "StringEquals": {
          "aws:SourceAccount": "${AWS_ACCOUNT_ID}"
        },
        "ArnLike": {
          "aws:SourceArn": "arn:aws:ecs:${AWS_REGION}:${AWS_ACCOUNT_ID}:*"
        }
      }
    }
  ]
}
EOF

cat > "$PERMISSIONS_POLICY" <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "EcsExec",
      "Effect": "Allow",
      "Action": [
        "ssmmessages:CreateControlChannel",
        "ssmmessages:CreateDataChannel",
        "ssmmessages:OpenControlChannel",
        "ssmmessages:OpenDataChannel"
      ],
      "Resource": "*"
    }
  ]
}
EOF

echo "Checking whether IAM role already exists..."

if aws iam get-role \
    --profile "$AWS_PROFILE" \
    --region "$AWS_REGION" \
    --role-name "$ROLE_NAME" \
    >/dev/null 2>&1; then

    echo "Role already exists: ${ROLE_NAME}"

else

    echo "Creating IAM role..."

    aws iam create-role \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --role-name "$ROLE_NAME" \
        --assume-role-policy-document "file://${TRUST_POLICY}" \
        --description \
        "IAM task role for Engineering Bench Fargate sandbox"

    echo "IAM role created."

fi

echo
echo "Creating/updating inline ECS Exec policy..."

aws iam put-role-policy \
    --profile "$AWS_PROFILE" \
    --region "$AWS_REGION" \
    --role-name "$ROLE_NAME" \
    --policy-name "EngineeringBenchEcsExec" \
    --policy-document "file://${PERMISSIONS_POLICY}"

echo "ECS Exec permissions configured."

echo
echo "Retrieving role ARN..."

ROLE_ARN="$(
    aws iam get-role \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --role-name "$ROLE_NAME" \
        --query 'Role.Arn' \
        --output text
)"

echo
echo "=============================================="
echo "Engineering Bench sandbox task role ready"
echo "=============================================="
echo
echo "Role name:"
echo "${ROLE_NAME}"
echo
echo "Role ARN:"
echo "${ROLE_ARN}"
echo
echo "Use this ARN in the ECS task definition."
echo