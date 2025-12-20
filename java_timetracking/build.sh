#!/bin/bash

if [ -z "$1" ]
  then
    echo "No TAG supplied"
    exit 1
fi

aws ecr get-login-password --region eu-north-1 | docker login --username AWS --password-stdin your-aws-account-id.dkr.ecr.eu-north-1.amazonaws.com
docker build -t javaapp .
docker tag javaapp:latest your-aws-account-id.dkr.ecr.eu-north-1.amazonaws.com/javaapp:latest
docker tag javaapp:latest your-aws-account-id.dkr.ecr.eu-north-1.amazonaws.com/javaapp:$1
docker push your-aws-account-id.dkr.ecr.eu-north-1.amazonaws.com/javaapp:$1
docker push your-aws-account-id.dkr.ecr.eu-north-1.amazonaws.com/javaapp:latest
