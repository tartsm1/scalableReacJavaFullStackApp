
export const config = {
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
  // You can add other non-AWS config validations here if needed.
}; 