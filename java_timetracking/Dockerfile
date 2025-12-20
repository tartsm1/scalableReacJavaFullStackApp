# Build Stage
FROM gradle:jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle build shadowJar -x test --no-daemon

# Run Stage
FROM amazoncorretto:21
WORKDIR /app
COPY --from=builder /app/build/libs/java_timetracking-1.0.0.jar app.jar
# AWS Region (e.g., us-east-1, eu-west-1, ap-southeast-1)
ENV AWS_REGION=eu-north-1

# AWS Cognito User Pool ID (format: region_poolid)
# Found in AWS Console > Cognito > User Pools > Your Pool > General Settings
ENV COGNITO_USER_POOL_ID=your-user-pool-id

# AWS Cognito App Client ID
# Found in AWS Console > Cognito > User Pools > Your Pool > App Integration > App Clients
ENV COGNITO_CLIENT_ID=your-app-cognito-client-id
ENV port=8888
ENV host=0.0.0.0 

EXPOSE 8888
CMD ["java", "-jar", "app.jar"]
# in development use: 
# docker run -p 8888:8888 -e DYNAMODB_ENDPOINT=http://host.docker.internal:8000 -e AWS_ACCESS_KEY_ID=fake -e AWS_SECRET_ACCESS_KEY=fake java_timetracking