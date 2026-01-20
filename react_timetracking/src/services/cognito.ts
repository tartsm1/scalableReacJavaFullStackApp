import {
  CognitoUserPool,
  CognitoUser,
  AuthenticationDetails,
  ICognitoUserAttributeData,
} from 'amazon-cognito-identity-js';

// Define a type for our global config for type safety
declare global {
  interface Window {
    APP_CONFIG: {
      aws_region: string;
      cognito_user_pool_id: string;
      cognito_client_id: string;
    };
  }
}

// Fallback to import.meta.env for development, but prioritize window.APP_CONFIG
export const awsRegion = window.APP_CONFIG?.aws_region || import.meta.env.VITE_AWS_REGION;
const userPoolId = window.APP_CONFIG?.cognito_user_pool_id || import.meta.env.VITE_COGNITO_USER_POOL_ID;
const clientId = window.APP_CONFIG?.cognito_client_id || import.meta.env.VITE_COGNITO_CLIENT_ID;

if (!userPoolId || !clientId || !awsRegion) {
  throw new Error("VITE_COGNITO_USER_POOL_ID, VITE_COGNITO_CLIENT_ID, and VITE_AWS_REGION must be set in your .env file.");
}

const poolData = {
  UserPoolId: userPoolId,
  ClientId: clientId,
  // The 'amazon-cognito-identity-js' library uses the region from the UserPoolId, but it's good practice to have it available.
};
export const userPool = new CognitoUserPool(poolData);

/**
 * Authenticates a user with Cognito.
 * @param username The user's username.
 * @param password The user's password.
 * @returns A promise that resolves with the user session on success.
 */
export const signIn = (username: any, password: any) => {
  return new Promise((resolve, reject) => {
    const authenticationData = {
      Username: username,
      Password: password,
    };
    const authenticationDetails = new AuthenticationDetails(authenticationData);

    const userData = {
      Username: username,
      Pool: userPool,
    };
    const cognitoUser = new CognitoUser(userData);

    cognitoUser.authenticateUser(authenticationDetails, {
      onSuccess: (result) => {
        resolve(result);
      },
      onFailure: (err) => {
        reject(err);
      },
    });
  });
};

// You can add other functions for signUp, confirmSignUp, signOut, getCurrentUser, etc.
// For example:
/*
export const signOut = () => {
  const cognitoUser = userPool.getCurrentUser();
  if (cognitoUser) {
    cognitoUser.signOut();
  }
};
*/