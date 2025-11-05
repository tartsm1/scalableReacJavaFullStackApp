import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { 
  signIn as amplifySignIn, 
  signUp as amplifySignUp, 
  signOut as amplifySignOut, 
  confirmSignUp as amplifyConfirmSignUp, 
  resetPassword, 
  confirmResetPassword, 
  getCurrentUser 
} from 'aws-amplify/auth';
import type { AuthUser } from 'aws-amplify/auth';

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
      const currentUser = await getCurrentUser();
      setUser(currentUser);
      setIsAuthenticated(true);
    } catch (error) {
      setUser(null);
      setIsAuthenticated(false);
    } finally {
      setIsLoading(false);
    }
  };

  const signIn = async (username: string, password: string) => {
    try {
      setError(null);
      await amplifySignIn({ username, password });
      const currentUser = await getCurrentUser();
      setUser(currentUser);
      setIsAuthenticated(true);
    } catch (error: any) {
      setError(error.message || 'Failed to sign in');
      throw error;
    }
  };

  const signUp = async (username: string, email: string, password: string) => {
    try {
      setError(null);
      await amplifySignUp({
        username,
        password,
        options: {
          userAttributes: {
            email,
          },
        },
      });
    } catch (error: any) {
      setError(error.message || 'Failed to sign up');
      throw error;
    }
  };

  const confirmSignUp = async (username: string, code: string) => {
    try {
      setError(null);
      await amplifyConfirmSignUp({ username, confirmationCode: code });
    } catch (error: any) {
      setError(error.message || 'Failed to confirm sign up');
      throw error;
    }
  };

  const signOut = async () => {
    try {
      setError(null);
      await amplifySignOut();
      setUser(null);
      setIsAuthenticated(false);
    } catch (error: any) {
      setError(error.message || 'Failed to sign out');
      throw error;
    }
  };

  const forgotPassword = async (username: string) => {
    try {
      setError(null);
      await resetPassword({ username });
    } catch (error: any) {
      setError(error.message || 'Failed to send reset code');
      throw error;
    }
  };

  const confirmForgotPassword = async (username: string, code: string, newPassword: string) => {
    try {
      setError(null);
      await confirmResetPassword({ username, confirmationCode: code, newPassword });
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