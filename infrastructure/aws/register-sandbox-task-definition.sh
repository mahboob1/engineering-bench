#!/usr/bin/env bash

set -euo pipefail

AWS_PROFILE="engineering-bench"
AWS_REGION="us-east-2"

TASK_DEFINITION_FAMILY="engineering-bench-sandbox"

TASK_ROLE_NAME="engineering-bench-sandbox-task-role"

GITHUB_SECRET_NAME="engineering-bench/github-token"

echo "=============================================="
echo "Engineering Bench - Sandbox Task Definition"
echo "=============================================="
echo
echo "AWS Profile:       $AWS_PROFILE"
echo "AWS Region:        $AWS_REGION"
echo "Task Definition:   $TASK_DEFINITION_FAMILY"
echo "Task Role:         $TASK_ROLE_NAME"
echo "GitHub Secret:     $GITHUB_SECRET_NAME"
echo

# ------------------------------------------------
# Check required tools
# ------------------------------------------------

if ! command -v aws >/dev/null 2>&1; then
    echo "ERROR: AWS CLI is required."
    exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
    echo "ERROR: jq is required."
    echo
    echo "Install it with:"
    echo "  brew install jq"
    exit 1
fi

# ------------------------------------------------
# Verify AWS identity
# ------------------------------------------------

echo "Checking AWS identity..."

AWS_ACCOUNT=$(
    aws sts get-caller-identity \
        --profile "$AWS_PROFILE" \
        --query 'Account' \
        --output text
)

echo "AWS account verified: $AWS_ACCOUNT"
echo

# ------------------------------------------------
# Retrieve task role ARN
# ------------------------------------------------

echo "Retrieving sandbox task role..."

TASK_ROLE_ARN=$(
    aws iam get-role \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --role-name "$TASK_ROLE_NAME" \
        --query 'Role.Arn' \
        --output text
)

if [[ -z "$TASK_ROLE_ARN" || "$TASK_ROLE_ARN" == "None" ]]; then
    echo "ERROR: Task role could not be found:"
    echo "       $TASK_ROLE_NAME"
    exit 1
fi

echo "Task role ARN:"
echo "$TASK_ROLE_ARN"
echo

# ------------------------------------------------
# Retrieve GitHub secret ARN
# ------------------------------------------------

echo "Retrieving GitHub token secret..."

GITHUB_SECRET_ARN=$(
    aws secretsmanager describe-secret \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --secret-id "$GITHUB_SECRET_NAME" \
        --query 'ARN' \
        --output text
)

if [[ -z "$GITHUB_SECRET_ARN" || "$GITHUB_SECRET_ARN" == "None" ]]; then
    echo "ERROR: GitHub token secret could not be found:"
    echo "       $GITHUB_SECRET_NAME"
    exit 1
fi

echo "GitHub secret ARN:"
echo "$GITHUB_SECRET_ARN"
echo

# ------------------------------------------------
# Retrieve current task definition
# ------------------------------------------------

echo "Retrieving current ECS task definition..."

CURRENT_TASK_DEFINITION=$(
    aws ecs describe-task-definition \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --task-definition "$TASK_DEFINITION_FAMILY" \
        --query 'taskDefinition' \
        --output json
)

CURRENT_REVISION=$(
    echo "$CURRENT_TASK_DEFINITION" |
        jq -r '.revision'
)

CURRENT_TASK_ROLE=$(
    echo "$CURRENT_TASK_DEFINITION" |
        jq -r '.taskRoleArn // empty'
)

CURRENT_EXECUTION_ROLE=$(
    echo "$CURRENT_TASK_DEFINITION" |
        jq -r '.executionRoleArn // empty'
)

CURRENT_GITHUB_SECRET_ARN=$(
    echo "$CURRENT_TASK_DEFINITION" |
        jq -r '
            .containerDefinitions[0].secrets[]?
            | select(.name == "GITHUB_TOKEN")
            | .valueFrom
        '
)

echo "Current revision:      $CURRENT_REVISION"
echo "Current task role:     ${CURRENT_TASK_ROLE:-none}"
echo "Execution role:        ${CURRENT_EXECUTION_ROLE:-none}"
echo "GitHub secret:         ${CURRENT_GITHUB_SECRET_ARN:-none}"
echo

# ------------------------------------------------
# Idempotency check
# ------------------------------------------------

if [[ "$CURRENT_TASK_ROLE" == "$TASK_ROLE_ARN" &&
      "$CURRENT_GITHUB_SECRET_ARN" == "$GITHUB_SECRET_ARN" ]]; then

    echo "The current task definition already uses:"
    echo "$TASK_ROLE_ARN"
    echo
    echo "and injects:"
    echo "GITHUB_TOKEN <- $GITHUB_SECRET_NAME"
    echo
    echo "No new revision is required."
    echo
    echo "=============================================="
    echo "Sandbox task definition already configured"
    echo "=============================================="

    exit 0
fi

# ------------------------------------------------
# Build registration payload
# ------------------------------------------------

echo "Preparing new task-definition revision..."

PAYLOAD_FILE=$(mktemp)

trap 'rm -f "$PAYLOAD_FILE"' EXIT

echo "$CURRENT_TASK_DEFINITION" |
    jq \
        --arg taskRoleArn "$TASK_ROLE_ARN" \
        --arg githubSecretArn "$GITHUB_SECRET_ARN" \
        '
        {
            family,
            taskRoleArn: $taskRoleArn,
            executionRoleArn,
            networkMode,
            containerDefinitions,
            volumes,
            placementConstraints,
            requiresCompatibilities,
            cpu,
            memory,
            pidMode,
            ipcMode,
            proxyConfiguration,
            inferenceAccelerators,
            runtimePlatform,
            ephemeralStorage
        }
        |
        .containerDefinitions[0].secrets = [
            {
                name: "GITHUB_TOKEN",
                valueFrom: $githubSecretArn
            }
        ]
        |
        with_entries(
            select(.value != null)
        )
        ' \
    > "$PAYLOAD_FILE"

echo "Registration payload prepared."
echo

# ------------------------------------------------
# Register new revision
# ------------------------------------------------

echo "Registering new ECS task-definition revision..."

REGISTERED_TASK_DEFINITION=$(
    aws ecs register-task-definition \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --cli-input-json "file://$PAYLOAD_FILE" \
        --query 'taskDefinition' \
        --output json
)

NEW_REVISION=$(
    echo "$REGISTERED_TASK_DEFINITION" |
        jq -r '.revision'
)

NEW_TASK_DEFINITION_ARN=$(
    echo "$REGISTERED_TASK_DEFINITION" |
        jq -r '.taskDefinitionArn'
)

NEW_TASK_ROLE=$(
    echo "$REGISTERED_TASK_DEFINITION" |
        jq -r '.taskRoleArn'
)

NEW_GITHUB_SECRET_ARN=$(
    echo "$REGISTERED_TASK_DEFINITION" |
        jq -r '
            .containerDefinitions[0].secrets[]?
            | select(.name == "GITHUB_TOKEN")
            | .valueFrom
        '
)

echo
echo "=============================================="
echo "Sandbox task definition updated"
echo "=============================================="
echo
echo "Previous revision:"
echo "$CURRENT_REVISION"
echo
echo "New revision:"
echo "$NEW_REVISION"
echo
echo "Task definition ARN:"
echo "$NEW_TASK_DEFINITION_ARN"
echo
echo "Task role ARN:"
echo "$NEW_TASK_ROLE"
echo
echo "GitHub secret:"
echo "GITHUB_TOKEN <- $NEW_GITHUB_SECRET_ARN"
echo
echo "Use this revision for subsequent sandbox tasks."