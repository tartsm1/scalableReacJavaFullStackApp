// Mock fetch globally for tests
global.fetch = jest.fn();

// Import the API functions directly without importing React files
const API_BASE_URL = 'http://localhost:8888/api';
export {}; // Make this file a module