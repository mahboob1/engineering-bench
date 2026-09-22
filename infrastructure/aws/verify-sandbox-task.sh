#!/usr/bin/env bash

set -euo pipefail

AWS_PROFILE="engineering-bench"
AWS_REGION="us-east-2"

CLUSTER="engineering-bench"
TASK_DEFINITION="engineering-bench-sandbox:2"
CONTAINER_NAME="engineering-bench-sandbox"

SUBNET_ID="subnet-0f2014463b95a7c00"
SECURITY_GROUP_ID="sg-05db641b523a00737"

LOG_GROUP="/ecs/engineering-bench-sandbox"

echo "=============================================="
echo "Engineering Bench - Sandbox Capability Test"
echo "=============================================="
echo
echo "AWS Profile:       $AWS_PROFILE"
echo "AWS Region:        $AWS_REGION"
echo "Cluster:           $CLUSTER"
echo "Task Definition:   $TASK_DEFINITION"
echo

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
# Verify task definition
# ------------------------------------------------

echo "Verifying ECS task definition..."

TASK_ROLE_ARN=$(
    aws ecs describe-task-definition \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --task-definition "$TASK_DEFINITION" \
        --query 'taskDefinition.taskRoleArn' \
        --output text
)

echo "Task role:"
echo "$TASK_ROLE_ARN"
echo

EXPECTED_ROLE="arn:aws:iam::${AWS_ACCOUNT}:role/engineering-bench-sandbox-task-role"

if [[ "$TASK_ROLE_ARN" != "$EXPECTED_ROLE" ]]; then
    echo "ERROR: Unexpected task role."
    echo "Expected:"
    echo "$EXPECTED_ROLE"
    echo
    echo "Actual:"
    echo "$TASK_ROLE_ARN"
    exit 1
fi

echo "Task role verified."
echo

# ------------------------------------------------
# Launch Fargate task
# ------------------------------------------------

echo "Launching Fargate sandbox..."

RUN_TASK_OUTPUT=$(
    aws ecs run-task \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --cluster "$CLUSTER" \
        --task-definition "$TASK_DEFINITION" \
        --launch-type FARGATE \
        --network-configuration \
            "awsvpcConfiguration={subnets=[$SUBNET_ID],securityGroups=[$SECURITY_GROUP_ID],assignPublicIp=ENABLED}" \
        --overrides \
            '{
                "containerOverrides": [
                    {
                        "name": "engineering-bench-sandbox",
                        "command": [
                            "bash",
                            "-c",
                            "set -e; echo \"Engineering Bench sandbox smoke test\"; echo \"Sandbox started successfully\"; pwd; ls -la /workspace; echo \"Sandbox command completed successfully\""
                        ]
                    }
                ]
            }' \
        --query 'tasks[0].taskArn' \
        --output text
)

if [[ -z "$RUN_TASK_OUTPUT" || "$RUN_TASK_OUTPUT" == "None" ]]; then
    echo "ERROR: Fargate task did not start."
    exit 1
fi

TASK_ARN="$RUN_TASK_OUTPUT"

echo
echo "Fargate task started:"
echo "$TASK_ARN"
echo

# ------------------------------------------------
# Wait for task completion
# ------------------------------------------------

echo "Waiting for sandbox task to stop..."

while true; do

    TASK_STATUS=$(
        aws ecs describe-tasks \
            --profile "$AWS_PROFILE" \
            --region "$AWS_REGION" \
            --cluster "$CLUSTER" \
            --tasks "$TASK_ARN" \
            --query 'tasks[0].lastStatus' \
            --output text
    )

    echo "Task status: $TASK_STATUS"

    if [[ "$TASK_STATUS" == "STOPPED" ]]; then
        break
    fi

    sleep 3
done

# ------------------------------------------------
# Retrieve exit code
# ------------------------------------------------

EXIT_CODE=$(
    aws ecs describe-tasks \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --cluster "$CLUSTER" \
        --tasks "$TASK_ARN" \
        --query "tasks[0].containers[?name=='$CONTAINER_NAME'].exitCode | [0]" \
        --output text
)

echo
echo "Container exit code: $EXIT_CODE"
echo

# ------------------------------------------------
# Retrieve stop reason
# ------------------------------------------------

STOP_REASON=$(
    aws ecs describe-tasks \
        --profile "$AWS_PROFILE" \
        --region "$AWS_REGION" \
        --cluster "$CLUSTER" \
        --tasks "$TASK_ARN" \
        --query 'tasks[0].stoppedReason' \
        --output text
)

echo "Stopped reason:"
echo "$STOP_REASON"
echo

# ------------------------------------------------
# Final result
# ------------------------------------------------

if [[ "$EXIT_CODE" != "0" ]]; then
    echo "=============================================="
    echo "SANDBOX TEST FAILED"
    echo "=============================================="
    echo
    echo "Exit code: $EXIT_CODE"
    echo "Task: $TASK_ARN"
    exit 1
fi

echo "=============================================="
echo "SANDBOX TEST PASSED"
echo "=============================================="
echo
echo "Fargate task launched successfully."
echo "Task definition revision 2 executed successfully."
echo "Container exited with code 0."
echo
echo "Task:"
echo "$TASK_ARN"
echo
echo "CloudWatch log group:"
echo "$LOG_GROUP"
echo