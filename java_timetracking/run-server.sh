#!/bin/bash

export COGNITO_USER_POOL_ID=your-user-pool-id
export COGNITO_CLIENT_ID=your-app-cognito-client-id
export AWS_REGION=eu-north-1
# Uncomment and set for local DynamoDB
# export DYNAMODB_ENDPOINT=http://localhost:8000
# actually DynamoDb Endpoint not needed in prod, default will connect region
export DYNAMODB_ENDPOINT=https://dynamodb.eu-north-1.amazonaws.com
export port=8888
export host=127.0.0.1

java -jar /home/ec2-user/java_timetracking-1.0.0.jar "$@"
