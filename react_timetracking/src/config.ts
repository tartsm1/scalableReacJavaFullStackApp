
export const config = {
  app: {
    name: 'Time Tracking App',
    version: '1.0.0',
  }
};

// Environment check
export const isDevelopment = import.meta.env.MODE === 'development';
export const isProduction = import.meta.env.MODE === 'production';

// Validation function
export const validateConfig = () => {
  // You can add other non-AWS config validations here if needed.
}; 