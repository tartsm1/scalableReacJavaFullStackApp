module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['**/*.test.ts'],
  moduleNameMapper: {
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
    '\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$': '<rootDir>/__mocks__/fileMock.js',
    '^react$': '<rootDir>/__mocks__/reactMock.js',
    '^react-dom$': '<rootDir>/__mocks__/reactDomMock.js',
  },
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.ts'],
  testPathIgnorePatterns: [
    '<rootDir>/node_modules/',
    '<rootDir>/src/index.tsx',
    '<rootDir>/src/serviceWorkerRegistration.ts',
    '<rootDir>/src/reportWebVitals.ts',
  ],
  transformIgnorePatterns: [
    'node_modules/(?!(node-fetch)/)',
  ],
  globals: {
    'ts-jest': {
      useESM: true,
    },
  },
}; 