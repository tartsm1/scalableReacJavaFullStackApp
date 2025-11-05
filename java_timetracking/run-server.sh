#!/bin/bash

export COGNITO_USER_POOL_ID=eu-north-1_xxxxxxx
export COGNITO_CLIENT_ID=dsjfaskdfakjsfdas
export AWS_REGION=eu-north-1
# Uncomment and set for local DynamoDB
# export DYNAMODB_ENDPOINT=http://localhost:8000
# actually DynamoDb Endpoint not needed in prod, default will connect region
export DYNAMODB_ENDPOINT=https://dynamodb.eu-north-1.amazonaws.com
export port=80
export host=$(hostname -I)

java -jar /home/ec2-user/java_timetracking-1.0.0.jar "$@"
