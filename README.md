# Full-Stack Time Tracking Application - Production-Ready & Cloud-Native

A modern, scalable time tracking application built for performance and enterprise-grade deployment. Features a dynamic **React TypeScript frontend** and a robust, **stateless Java Vert.x backend** with comprehensive testing and containerization.

## 🚀 Key Features

- **Stateless Architecture**: Horizontally scalable design enabling unlimited scaling behind load balancers (AWS ALB)
- **Cloud-Native**: Fully containerized with Docker, ready for Kubernetes/EKS deployment
- **CI/CD Pipeline**: AWS CodeBuild integration with automated Docker image builds and ECR deployment
- **Secure Authentication**: AWS Cognito with JWT token-based authentication and role-based access control
- **High-Performance Database**: AWS DynamoDB for massive scalability and low-latency operations
- **Comprehensive Testing**: 17 Java unit tests (JUnit 5 + Mockito) with 100% pass rate
- **Modern Stack**: Java 25 with Vert.x 5.0, React 18 with TypeScript, Material-UI
- **Small Footprint**: Java Vert.x framework optimized for minimal memory usage and fast performance

## 🏗️ Architecture Overview

The application follows a modern microservices architecture with complete separation of concerns:

- **Frontend**: React 18 + TypeScript + Material-UI, served via Nginx in production
- **Backend**: Java 25 + Vert.x (non-blocking, event-driven), packaged as executable JAR
- **Authentication**: AWS Cognito User Pools with JWT validation
- **Database**: AWS DynamoDB with SDK v2
- **Containerization**: Multi-stage Docker builds for optimized image sizes
- **Deployment**: AWS EKS with Pod Identity for secure credential management

## ⚡ Quick Start

```bash
# Clone the repository
git clone <repository-url>
cd scalableReacJavaFullStackApp

# Start local DynamoDB
docker run -d -p 8000:8000 amazon/dynamodb-local

# Create Tasks table
cd react_timetracking && node createTasksTable.js && cd ..

# Configure environment (edit env_dev with your AWS Cognito details)
# Then use VSCode "Run and Debug" or:

# Terminal 1: Start Java backend
cd java_timetracking
./gradlew run

# Terminal 2: Start React frontend
cd react_timetracking
npm install
npm start

# Access the application at http://localhost:3000
```

## 📋 Table of Contents

- [Development Setup](#️-development-setup)
- [Docker & Containerization](#-docker--containerization)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Kubernetes Deployment](#️-kubernetes-deployment)
- [Testing](#-testing)
- [React Frontend](#react-time-tracking-app)
- [Java Backend](#java-vertex-server-with-dynamodb-and-aws-cognito-authentication)
- [Local DynamoDB Setup](#local-dynamodb-setup-for-development)
 

## 🛠️ Development Setup

### VSCode Launch Configuration
- `.vscode/launch.json` - Pre-configured to start both frontend and backend
- `Caddyfile` - Included to simulate production environment locally
- `env_dev` - Development environment variables

## 🐳 Docker & Containerization

### Multi-Stage Docker Builds

Both applications use optimized multi-stage Docker builds:

**Java Backend**:
- Build stage: Gradle with JDK 25
- Runtime stage: Amazon Corretto 25 (minimal JRE)
- Final image: ~200MB with executable JAR

**React Frontend**:
- Build stage: Node.js 22 for npm build
- Runtime stage: Nginx Alpine (minimal web server)
- Final image: ~50MB with static assets

### Building Docker Images

```bash
# Build Java backend
cd java_timetracking
docker build -t java_timetracking:latest .

# Build React frontend
cd react_timetracking
docker build -t react_timetracking:latest .
```

### Running with Docker

```bash
# Run Java backend (development with local DynamoDB)
docker run -p 8888:8888 \
  -e DYNAMODB_ENDPOINT=http://host.docker.internal:8000 \
  -e AWS_ACCESS_KEY_ID=fake \
  -e AWS_SECRET_ACCESS_KEY=fake \
  java_timetracking

# Run React frontend
docker run -p 80:80 react_timetracking
```

## 🔄 CI/CD Pipeline

### AWS CodeBuild Integration

The project includes `buildspec.yml` for automated builds:

1. **Pre-build**: ECR login and commit hash tagging
2. **Build**: Docker image creation for both applications
3. **Post-build**: Push to ECR with `latest` and commit hash tags
4. **Artifacts**: Generate `imagedefinitions.json` for deployment

```yaml
# Automated workflow:
Git Push → CodeBuild → Docker Build → ECR Push → EKS Deployment
```

### Image Tagging Strategy
- `latest`: Always points to the most recent build
- `<commit-hash>`: Specific version for rollback capability

## ☸️ Kubernetes Deployment

### AWS EKS Architecture

- **Namespace**: `development` (configurable)
- **Pod Identity**: Secure AWS credential injection without static keys
- **Ingress**: AWS ALB Controller with Cognito authentication
- **Auto-scaling**: Horizontal Pod Autoscaler (HPA) configured
- **Service Mesh**: Internal service-to-service communication

### Key Resources
- Service Accounts with IAM role associations
- ConfigMaps for environment configuration
- Secrets for sensitive data
- HPA for automatic scaling based on CPU/memory

## 🧪 Testing

### Java Backend Tests

**Test Coverage**: 17 unit tests, 100% pass rate

```bash
cd java_timetracking
./gradlew test
```

**Test Suites**:
1. **AuthMiddlewareTest** (8 tests)
   - Token validation and authentication flows
   - Role-based access control
   - Development mode authentication bypass

2. **TaskServiceTest** (6 tests)
   - CRUD operations with DynamoDB
   - Task retrieval and listing
   - Error handling for missing items

3. **TaskTest** (3 tests)
   - Model validation
   - Constructor and getter/setter tests

**Technologies**: JUnit 5, Mockito, Vert.x JUnit 5 integration

### React Frontend Tests

```bash
cd react_timetracking
npm test
```

**Test Framework**: Jest + React Testing Library

# React Time Tracking App

A modern, responsive time tracking application built with React, TypeScript, and Material-UI. Users can track their daily work by project and task, with comprehensive reporting and analytics.

## Features

- **User Authentication**: Secure login/signup with AWS Cognito
- **Task Management**: Add, edit, and delete time tracking entries
- **Project Organization**: Group tasks by project for better organization
- **Monthly Reports**: Comprehensive analytics and reporting
- **Responsive Design**: Works on desktop and mobile devices
- **Modern UI**: Beautiful Material-UI interface

## Authentication

This app uses AWS Cognito for user authentication, providing:

- User registration and login
- Email verification
- Password reset functionality
- Secure session management
- User profile management

## Getting Started

### Prerequisites

- Node.js (v14 or higher)
- npm or yarn
- AWS Account (for Cognito setup)

### Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd react_timetracking
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Set up AWS Cognito**:
   - Follow the instructions in [AWS_COGNITO_SETUP.md](./AWS_COGNITO_SETUP.md)
   - Create a User Pool and App Client in AWS Cognito
   - Get your User Pool ID and App Client ID

4. **Configure environment variables**:
   
   Edit `env_dev` or `react_timetracking/.env` and add your AWS Cognito configuration:
   ```env
   REACT_APP_AWS_REGION=eu-north-1
   REACT_APP_COGNITO_USER_POOL_ID=your-user-pool-id
   REACT_APP_COGNITO_CLIENT_ID=your-app-client-id
   ```

5. **Start the development server**:
   - In VSCode use "Run and Debug".  `.vscode/launch.json` -> `env_dev`. 
   - Command line loads environment variables from `react_timetracking/.env` file. 
   ```bash
   npm start
   ```

6. **Open your browser**:
   Navigate to [http://localhost:3000](http://localhost:3000)

## Usage

### First Time Setup

1. **Create an account**: Click "Sign Up" and enter your details
2. **Verify your email**: Check your email for a confirmation code
3. **Sign in**: Use your username/email and password to log in

### Using the App

1. **Add Tasks**: Click "Add Task" to log your work
   - Enter project name
   - Describe the task
   - Set the date
   - Specify hours worked

2. **View Reports**: Click "Monthly Report" to see analytics
   - Total hours worked
   - Hours by project
   - Task count and project count

3. **Manage Tasks**: Edit or delete existing tasks as needed

4. **Sign Out**: Use the "Sign Out" button to securely log out

## Project Structure

```
├── components/ # Reusable UI components (Login, SignUp, etc.)
├── contexts/ # React contexts (e.g., AuthContext)
├── services/ # External service integrations
│ ├── api.ts # Functions for calling the backend API
│ └── cognito.ts # Cognito/Amplify configuration and services
├── App.tsx # Main application component
└── index.tsx # Application entry point
```

## Configuration

### Environment Variables

The app uses the following environment variables:

- `REACT_APP_AWS_REGION`: AWS region (e.g., eu-north-1)
- `REACT_APP_COGNITO_USER_POOL_ID`: Cognito User Pool ID
- `REACT_APP_COGNITO_CLIENT_ID`: Cognito App Client ID

### AWS Cognito Setup

For detailed instructions on setting up AWS Cognito, see [AWS_COGNITO_SETUP.md](./AWS_COGNITO_SETUP.md).

## Development

### Available Scripts

- `npm start`: Start development server
- `npm build`: Build for production
- `npm test`: Run tests
- `npm eject`: Eject from Create React App

### Adding Features

1. **New Components**: Add to `src/components/`
2. **API Functions**: Add to `src/api.ts`
3. **Authentication**: Use the `useAuth` hook from `src/contexts/AuthContext.tsx`

## Deployment

### Build for Production

```bash
npm run build
```

### Environment Configuration

For production deployment:

1. Update the `react_timetracking/public/app-config.js`
2. Configure CORS settings in AWS Cognito
3. Set up proper environment variables
4. Use HTTPS in production

## Troubleshooting

### Common Issues

1. **Authentication errors**: Check your AWS Cognito configuration
2. **Build errors**: Ensure all dependencies are installed
3. **CORS errors**: Configure allowed origins in Cognito

### Debug Mode

Enable debug logging by adding to `src/aws-config.ts`:

```typescript
const awsConfig = {
  Auth: {
    // ... existing config
  },
  Logging: {
    level: 'DEBUG'
  }
};
```

For issues and questions:

1. Check the [AWS_COGNITO_SETUP.md](./AWS_COGNITO_SETUP.md) for authentication setup
2. Review the troubleshooting section above
3. Create an issue in the repository

## Security

- All authentication is handled securely through AWS Cognito
- Passwords are never stored locally
- Sessions are managed securely
- HTTPS is required in production

For more security information, see the AWS Cognito documentation. 

# Java Vertex Server with DynamoDB and AWS Cognito Authentication

This is a Java Vert.x server application that provides a REST API for task management with AWS DynamoDB storage and AWS Cognito User Pools authentication.

## Features

- REST API for CRUD operations on tasks
- AWS DynamoDB integration for data persistence
- AWS Cognito User Pools authentication
- JWT token validation
- Role-based access control
- Executable JAR with all dependencies included

## Prerequisites

- Java 25 or higher
- AWS account with DynamoDB and Cognito User Pools configured
- AWS credentials configured (via AWS CLI, environment variables, or IAM roles)

## Configuration

The application uses the following environment variables from  `env_dev` for AWS Cognito configuration:

- `COGNITO_USER_POOL_ID`: Your AWS Cognito User Pool ID
- `COGNITO_CLIENT_ID`: Your AWS Cognito App Client ID
- `AWS_REGION`: AWS region (e.g., eu-north-1)

- `DYNAMODB_ENDPOINT=http://localhost:8000` DynamoDB endpoint for dev env
- `dev=true` Disables auhtentification for development in backend
- `port=8888` Vert.x server port and host
- `host=localhost`

## Building the Application

```bash
./gradlew shadowJar
```

This creates an executable JAR file at `build/libs/java_timetracking-1.0.0.jar`

## Running the Application

### Without Authentication (Development)

If you set dev=true environment variable, the backend will run without authentication:

```bash
java -jar build/libs/java_timetracking-1.0.0.jar
```

### With Authentication (Production)

Set the required environment variables and run:
NB: in EC2 instance DYNAMODB_ENDPOINT not needed. Instance should have IAM role for DynamoDB access. 

```bash
export COGNITO_USER_POOL_ID="your-user-pool-id"
export COGNITO_CLIENT_ID="your-client-id"
export AWS_REGION="eu-north-1"
export DYNAMODB_ENDPOINT=http://localhost:8000
export port=80
export host=127.0.0.1

java -jar build/libs/java_timetracking-1.0.0.jar
```

## API Endpoints

All API endpoints are prefixed with `/api`.

### Authentication

- `POST /api/auth/login` - Authenticate user with username and password
  ```json
  {
    "username": "user@example.com",
    "password": "password"
  }
  ```

### Tasks (Protected when authentication is enabled)

- `GET /api/tasks` - List all tasks
- `GET /api/tasks/:id` - Get task by ID
- `POST /api/tasks` - Create new task
- `PUT /api/tasks/:id` - Update task
- `DELETE /api/tasks/:id` - Delete task

### Task Object Structure

```json
{
  "id": 1234567890,
  "date": "2025-07-05",
  "project": "Project Name",
  "hours": 8,
  "task": "Task description"
}
```

## Authentication Flow

1. **Login**: Use the `/api/auth/login` endpoint with username and password
2. **Get Token**: The response includes an `idToken` (JWT)
3. **API Calls**: Include the token in the Authorization header:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

## AWS Cognito Setup

1. Create a Cognito User Pool in AWS Console
2. Create an App Client in the User Pool
3. Configure the App Client to allow USER_PASSWORD_AUTH
4. Note down the User Pool ID and App Client ID
5. Set up users in the User Pool

## Error Responses

- `401 Unauthorized`: Missing or invalid authentication token
- `403 Forbidden`: Insufficient permissions
- `503 Service Unavailable`: Authentication service not configured

## Development

### Running Tests

```bash
./gradlew test
```

**Test Results**: 17 tests across 3 test suites
- ✅ AuthMiddlewareTest: 8 tests
- ✅ TaskServiceTest: 6 tests  
- ✅ TaskTest: 3 tests

### Building without Tests

```bash
./gradlew shadowJar -x test
```

### Docker Build

```bash
# Build Docker image
docker build -t java_timetracking:latest .

# Run with local DynamoDB
docker run -p 8888:8888 \
  -e DYNAMODB_ENDPOINT=http://host.docker.internal:8000 \
  -e AWS_ACCESS_KEY_ID=fake \
  -e AWS_SECRET_ACCESS_KEY=fake \
  java_timetracking
```

## Architecture

- **MainVerticle**: Main application entry point and HTTP server setup
- **TaskService**: Business logic for task operations with DynamoDB
- **CognitoAuthService**: AWS Cognito authentication and JWT validation
- **AuthMiddleware**: HTTP middleware for authentication and authorization
- **DynamoDBClientProvider**: AWS DynamoDB client configuration

## Technology Stack

### Backend
- **Java**: 25 (LTS)
- **Framework**: Vert.x 5.0 (non-blocking, event-driven)
- **Build Tool**: Gradle 9.0 with Shadow plugin for fat JAR
- **AWS SDK**: 2.40 (DynamoDB, Cognito)
- **Authentication**: Auth0 JWT 4.4.0 + JWKS RSA 0.22.1
- **JSON Processing**: Jackson 2.17.1
- **Logging**: SLF4J 2.0.13 + Logback 1.5.19
- **Testing**: JUnit 5.10.2, Mockito 5.11.0

### Frontend
- **React**: 18.3.1
- **TypeScript**: 4.9.5
- **UI Framework**: Material-UI 5.15.21
- **State Management**: React Context API
- **Authentication**: amazon-cognito-identity-js 6.3.15
- **AWS SDK**: @aws-sdk/client-dynamodb 3.840.0
- **Build Tool**: react-scripts 5.0.1
- **Testing**: Jest, React Testing Library

### Infrastructure
- **Containerization**: Docker (multi-stage builds)
- **Container Registry**: AWS ECR
- **Orchestration**: AWS EKS (Kubernetes)
- **Load Balancer**: AWS ALB with Cognito integration
- **Database**: AWS DynamoDB
- **CI/CD**: AWS CodeBuild
- **Authentication**: AWS Cognito User Pools
- **IAM**: EKS Pod Identity for secure credential injection

## Dependencies

- Vert.x Core and Web for HTTP server
- AWS SDK for DynamoDB and Cognito
- Auth0 JWT library for token validation
- Jackson for JSON processing
- JUnit 5 for testing 

# Local DynamoDB Setup for Development

For development, it's recommended to use local DynamoDB to avoid AWS costs and enable offline development.

## Option 1: DynamoDB Local (Docker)

```bash
# Run DynamoDB Local in Docker
docker run -p 8000:8000 amazon/dynamodb-local

# Verify it's running
curl http://localhost:8000
```

## Option 2: Download DynamoDB Local

Download from: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html

## Create Tasks Table

The application requires a `Tasks` table with primary key `id` (Number).

```bash
# Using AWS CLI with local endpoint
aws dynamodb create-table \
    --table-name Tasks \
    --attribute-definitions AttributeName=id,AttributeType=N \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --endpoint-url http://localhost:8000

# Or use the provided script
cd react_timetracking
node createTasksTable.js
```

## Environment Configuration

Set `DYNAMODB_ENDPOINT=http://localhost:8000` in your environment to use local DynamoDB.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Contacts
My Slack: [friendly-solutions](https://join.slack.com/t/friendlysolutionsco/shared_invite/zt-3gqtsiax0-m7uCPEfzprlPWYntp4lcXg) 

## License
MIT License
Free to use unless reference to my homepage https://friendly-solution.com/ not removed ;) 
