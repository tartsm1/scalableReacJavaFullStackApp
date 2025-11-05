import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import Login from './Login';
import SignUp from './SignUp';
import ConfirmSignUp from './ConfirmSignUp';
import ForgotPassword from './ForgotPassword';
import ConfirmForgotPassword from './ConfirmForgotPassword';

type AuthView = 'login' | 'signup' | 'confirmSignUp' | 'forgotPassword' | 'confirmForgotPassword';

interface AuthWrapperProps {
  children: React.ReactNode;
}

const AuthWrapper: React.FC<AuthWrapperProps> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  const [currentView, setCurrentView] = useState<AuthView>('login');
  const [confirmUsername, setConfirmUsername] = useState('');

  if (isLoading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        <div>Loading...</div>
      </div>
    );
  }

  if (isAuthenticated) {
    return <>{children}</>;
  }

  const handleSwitchToLogin = () => {
    setCurrentView('login');
  };

  const handleSwitchToSignUp = () => {
    setCurrentView('signup');
  };

  const handleSwitchToForgotPassword = () => {
    setCurrentView('forgotPassword');
  };

  const handleSwitchToConfirmSignUp = (username: string) => {
    setConfirmUsername(username);
    setCurrentView('confirmSignUp');
  };

  const handleSwitchToConfirmForgotPassword = (username: string) => {
    setConfirmUsername(username);
    setCurrentView('confirmForgotPassword');
  };

  switch (currentView) {
    case 'login':
      return (
        <Login
          onSwitchToSignUp={handleSwitchToSignUp}
          onSwitchToForgotPassword={handleSwitchToForgotPassword}
        />
      );
    case 'signup':
      return (
        <SignUp
          onSwitchToLogin={handleSwitchToLogin}
          onSwitchToConfirmSignUp={handleSwitchToConfirmSignUp}
        />
      );
    case 'confirmSignUp':
      return (
        <ConfirmSignUp
          username={confirmUsername}
          onSwitchToLogin={handleSwitchToLogin}
        />
      );
    case 'forgotPassword':
      return (
        <ForgotPassword
          onSwitchToLogin={handleSwitchToLogin}
          onSwitchToConfirmForgotPassword={handleSwitchToConfirmForgotPassword}
        />
      );
    case 'confirmForgotPassword':
      return (
        <ConfirmForgotPassword
          username={confirmUsername}
          onSwitchToLogin={handleSwitchToLogin}
        />
      );
    default:
      return (
        <Login
          onSwitchToSignUp={handleSwitchToSignUp}
          onSwitchToForgotPassword={handleSwitchToForgotPassword}
        />
      );
  }
};

export default AuthWrapper; 