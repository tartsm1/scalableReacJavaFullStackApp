import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import {
  CognitoUserPool,
  CognitoUser,
  CognitoUserAttribute,
  AuthenticationDetails,
  CognitoUserSession,
} from 'amazon-cognito-identity-js';
import { userPool } from '../services/cognito';

interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  signIn: (username: string, password: string) => Promise<void>;
  signUp: (username: string, email: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  confirmSignUp: (username: string, code: string) => Promise<void>;
  forgotPassword: (username: string) => Promise<void>;
  confirmForgotPassword: (username: string, code: string, newPassword: string) => Promise<void>;
  error: string | null;
  clearError: () => void;
}
interface AuthUser {
  username: string;
  attributes: { [key: string]: any };
  [key: string]: any;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    checkAuthState();
  }, []);

  const checkAuthState = async () => {
    try {
      const cognitoUser = userPool.getCurrentUser();
      if (cognitoUser) {
        cognitoUser.getSession(async (err: Error | null, session: CognitoUserSession | null) => {
          if (err || !session || !session.isValid()) {
            setUser(null);
            setIsAuthenticated(false);
          } else {
            const attributes = await new Promise<any>((resolve, reject) => {
              cognitoUser.getUserAttributes((err, attributes) => {
                if (err) return reject(err);
                const attrs: { [key: string]: any } = {};
                attributes?.forEach(attr => attrs[attr.getName()] = attr.getValue());
                resolve(attrs);
              });
            });
            setUser({ username: cognitoUser.getUsername(), attributes });
            setIsAuthenticated(true);
          }
          setIsLoading(false);
        });
      } else {
        setIsLoading(false);
      }
    } catch (e) {
      setUser(null);
      setIsAuthenticated(false);
    } finally {
      setIsLoading(false);
    }
  };

  const signIn = async (username: string, password: string) => {
    try {
      setError(null);
      await new Promise<void>((resolve, reject) => {
        const cognitoUser = new CognitoUser({ Username: username, Pool: userPool });
        const authDetails = new AuthenticationDetails({ Username: username, Password: password });

        cognitoUser.authenticateUser(authDetails, {
          onSuccess: (session) => {
            cognitoUser.getUserAttributes((err, attributes) => {
              if (err) {
                return reject(err);
              }
              const attrs: { [key: string]: any } = {};
              attributes?.forEach(attr => attrs[attr.getName()] = attr.getValue());
              setUser({ username: cognitoUser.getUsername(), attributes: attrs });
              setIsAuthenticated(true);
              resolve();
            });
          },
          onFailure: (err) => {
            reject(err);
          },
          newPasswordRequired: (userAttributes, requiredAttributes) => {
            reject(new Error("New password required. This flow is not implemented."));
          }
        });
      });
    } catch (error: any) {
      setError(error.message || 'Failed to sign in');
      throw error;
    }
  };

  const signUp = async (username: string, email: string, password: string) => {
    try {
      setError(null);
      const attributeList: CognitoUserAttribute[] = [];
      const dataEmail = { Name: 'email', Value: email };
      attributeList.push(new CognitoUserAttribute(dataEmail));

      await new Promise<void>((resolve, reject) => {
        userPool.signUp(username, password, attributeList, [], (err, result) => {
          if (err) return reject(err);
          resolve();
        });
      });
    } catch (error: any) {
      setError(error.message || 'Failed to sign up');
      throw error;
    }
  };

  const confirmSignUp = async (username: string, code: string) => {
    try {
      setError(null);
      await new Promise<void>((resolve, reject) => {
        const cognitoUser = new CognitoUser({ Username: username, Pool: userPool });
        cognitoUser.confirmRegistration(code, true, (err, result) => {
          if (err) return reject(err);
          resolve();
        });
      });
    } catch (error: any) {
      setError(error.message || 'Failed to confirm sign up');
      throw error;
    }
  };

  const signOut = async () => {
    try {
      setError(null);
      const cognitoUser = userPool.getCurrentUser();
      if (cognitoUser) {
        cognitoUser.signOut();
        setUser(null);
        setIsAuthenticated(false);
      }
    } catch (error: any) {
      setError(error.message || 'Failed to sign out');
      throw error;
    }
  };

  const forgotPassword = async (username: string) => {
    try {
      setError(null);
      await new Promise<void>((resolve, reject) => {
        const cognitoUser = new CognitoUser({ Username: username, Pool: userPool });
        cognitoUser.forgotPassword({
          onSuccess: () => resolve(),
          onFailure: (err) => reject(err),
        });
      });
    } catch (error: any) {
      setError(error.message || 'Failed to send reset code');
      throw error;
    }
  };

  const confirmForgotPassword = async (username: string, code: string, newPassword: string) => {
    try {
      setError(null);
      await new Promise<void>((resolve, reject) => {
        const cognitoUser = new CognitoUser({ Username: username, Pool: userPool });
        cognitoUser.confirmPassword(code, newPassword, {
          onSuccess: () => resolve(),
          onFailure: (err) => reject(err),
        });
      });
    } catch (error: any) {
      setError(error.message || 'Failed to reset password');
      throw error;
    }
  };

  const clearError = () => {
    setError(null);
  };

  const value: AuthContextType = {
    user,
    isAuthenticated,
    isLoading,
    signIn,
    signUp,
    signOut,
    confirmSignUp,
    forgotPassword,
    confirmForgotPassword,
    error,
    clearError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}; 