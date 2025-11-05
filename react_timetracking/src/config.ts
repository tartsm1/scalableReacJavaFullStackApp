// AWS Cognito Configuration
// Update these values with your actual AWS Cognito User Pool settings

export const config = {
  aws: {
    region: process.env.REACT_APP_AWS_REGION || 'us-east-1',
    userPoolId: process.env.REACT_APP_USER_POOL_ID || 'us-east-1_XXXXXXXXX',
    userPoolWebClientId: process.env.REACT_APP_CLIENT_ID || 'XXXXXXXXXXXXXXXXXXXXXXXXXX',
  },
  app: {
    name: 'Time Tracking App',
    version: '1.0.0',
  }
};

// Environment check
export const isDevelopment = process.env.NODE_ENV === 'development';
export const isProduction = process.env.NODE_ENV === 'production';

// Validation function
export const validateConfig = () => {
  const { aws } = config;
  
  if (aws.userPoolId === 'us-east-1_XXXXXXXXX') {
    console.warn('⚠️  Please update your AWS Cognito User Pool ID in src/config.ts or set COGNITO_USER_POOL_ID environment variable');
  }
  
  if (aws.userPoolWebClientId === 'XXXXXXXXXXXXXXXXXXXXXXXXXX') {
    console.warn('⚠️  Please update your AWS Cognito App Client ID in src/config.ts or set COGNITO_CLIENT_ID environment variable');
  }
  
  if (aws.region === 'us-east-1' && !process.env.AWS_REGION) {
    console.warn('⚠️  Using default AWS region (us-east-1). Set AWS_REGION environment variable to change.');
  }
}; 