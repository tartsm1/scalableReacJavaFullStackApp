# Full-stack demo Time Tracking application is built for performance and scale. It features a dynamic React frontend and a robust, stateless Java backend. Authentication with JWT tokens.

Authentication is managed by AWS Cognito, which handles secure, email-based user registration and authorization using JWT tokens. For the database, DynamoDB was chosen for its massive scalability and low-latency performance.

The entire architecture is stateless, a key design choice that allows the application to scale horizontally without limits. In a production environment, this enables it to work seamlessly behind a load balancer, such as AWS ALB, to ensure high availability and performance under heavy load. Java Vert.x framework for small memory footprint and fast performance. 

# VScode launch configuration included to start both frontend and backend
- reacJavaFullStackApp/.vscode/launch.json
- Caddyfile included to simulate prod environment

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
   ```bash
   cp env.example .env
   ```
   
   Edit `.env` and add your AWS Cognito configuration:
   ```env
   REACT_APP_AWS_REGION=us-east-1
   REACT_APP_COGNITO_USER_POOL_ID=your-user-pool-id
   REACT_APP_COGNITO_CLIENT_ID=your-app-client-id
   ```

5. **Start the development server**:
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
src/
├── components/          # React components
│   ├── AuthWrapper.tsx # Authentication wrapper
│   ├── Login.tsx       # Login form
│   ├── SignUp.tsx      # Registration form
│   ├── ConfirmSignUp.tsx # Email verification
│   ├── ForgotPassword.tsx # Password reset
│   └── ConfirmForgotPassword.tsx # Password reset confirmation
├── contexts/           # React contexts
│   └── AuthContext.tsx # Authentication context
├── api.ts             # API functions
├── aws-config.ts      # AWS Amplify configuration
├── config.ts          # App configuration
└── App.tsx           # Main application component
```

## Configuration

### Environment Variables

The app uses the following environment variables:

- `REACT_APP_AWS_REGION`: AWS region (e.g., us-east-1)
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

1. Update the domain in `src/aws-config.ts`
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

- Java 17 or higher
- AWS account with DynamoDB and Cognito User Pools configured
- AWS credentials configured (via AWS CLI, environment variables, or IAM roles)

## Configuration

The application uses the following environment variables for AWS Cognito configuration:

- `COGNITO_USER_POOL_ID`: Your AWS Cognito User Pool ID
- `COGNITO_CLIENT_ID`: Your AWS Cognito App Client ID
- `AWS_REGION`: AWS region (e.g., us-east-1)

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
export AWS_REGION="us-east-1"
export DYNAMODB_ENDPOINT=http://localhost:8000
export port=80
export host=$(hostname -I)

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

### Building without Tests

```bash
./gradlew shadowJar -x test
```

## Architecture

- **MainVerticle**: Main application entry point and HTTP server setup
- **TaskService**: Business logic for task operations with DynamoDB
- **CognitoAuthService**: AWS Cognito authentication and JWT validation
- **AuthMiddleware**: HTTP middleware for authentication and authorization
- **DynamoDBClientProvider**: AWS DynamoDB client configuration

## Dependencies

- Vert.x Core and Web for HTTP server
- AWS SDK for DynamoDB and Cognito
- Auth0 JWT library for token validation
- Jackson for JSON processing
- JUnit 5 for testing 

# For development best option is to use local DynamoDB
https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html

In DynamoDB should exist table `Tasks` with primary key named `"id"`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License
MIT License
Free to use unless reference to my homepage https://friendly-solution.com/ not removed ;) 
