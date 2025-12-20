# Build Stage
FROM node:22 AS builder
# AWS Region (e.g., us-east-1, eu-west-1, ap-southeast-1)
ENV REACT_APP_AWS_REGION=eu-north-1

# AWS Cognito User Pool ID (format: region_poolid)
# Found in AWS Console > Cognito > User Pools > Your Pool > General Settings
ENV REACT_APP_COGNITO_USER_POOL_ID=your-user-pool-id

# AWS Cognito App Client ID
# Found in AWS Console > Cognito > User Pools > Your Pool > App Integration > App Clients
ENV REACT_APP_COGNITO_CLIENT_ID=your-app-cognito-client-id

# Optional: App Name
ENV REACT_APP_NAME="Time Tracking App"

# Optional: App Version
ENV REACT_APP_VERSION=1.0.0 

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Run Stage
FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY docker_nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
