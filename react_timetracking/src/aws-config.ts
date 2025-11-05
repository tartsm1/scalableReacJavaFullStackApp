import { Amplify } from 'aws-amplify';
import { config, validateConfig } from './config';

// Validate configuration on startup
validateConfig();

// AWS Cognito Configuration
const awsConfig = {
  Auth: {
    Cognito: {
      userPoolId: config.aws.userPoolId,
      userPoolClientId: config.aws.userPoolWebClientId,
      loginWith: {
        email: true,
        username: true,
      },
    },
  },
};

// Initialize Amplify with the configuration
Amplify.configure(awsConfig);

export default awsConfig; 