import { userPool, awsRegion } from './services/cognito';

const awsConfig = {
  Auth: {
    Cognito: {
      userPoolId: userPool.getUserPoolId(),
      userPoolClientId: userPool.getClientId(),
      region: awsRegion,
    },
  },
};

export default awsConfig; 