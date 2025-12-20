#!/bin/bash

if [ -z "$1" ]
  then
    echo "No TAG supplied"
    exit 1
fi

aws ecr get-login-password --region eu-north-1 | docker login --username AWS --password-stdin your-aws-account-id.dkr.ecr.eu-north-1.amazonaws.com
docker build -t reactapp .
docker tag reactapp:latest your-aws-account-id.dkr.ecr.eu-north-1.amazonaws.com/reactapp:latest
docker tag reactapp:latest your-aws-account-id.dkr.ecr.eu-north-1.amazonaws.com/reactapp:$1
docker push your-aws-account-id.dkr.ecr.eu-north-1.amazonaws.com/reactapp:$1
docker push your-aws-account-id.dkr.ecr.eu-north-1.amazonaws.com/reactapp:latest
