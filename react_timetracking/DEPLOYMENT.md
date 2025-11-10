# Deployment Guide

This guide provides instructions for deploying your React Time Tracking App to various platforms.

## Prerequisites

Before deploying, ensure you have:

1. **AWS Cognito Setup**: Follow the instructions in `AWS_COGNITO_SETUP.md`
2. **Environment Variables**: Copy `env.example` to `.env` and configure your AWS settings
3. **Production Build**: Run `npm run build` to create the production build

## Quick Deployment

Use the automated deployment script:

```bash
./deploy.sh
```

This script will guide you through deployment options.

## Deployment Options

### 1. Local Testing

Test your production build locally:

```bash
# Install serve globally
npm install -g serve

# Serve the build folder
serve -s build -l 3000
```

Visit `http://localhost:3000` to test your app.

### 2. Netlify

**Automatic Deployment:**
1. Connect your GitHub repository to Netlify
2. Set build command: `npm run build`
3. Set publish directory: `build`
4. Add environment variables in Netlify dashboard

**Manual Deployment:**
```bash
# Install Netlify CLI
npm install -g netlify-cli

# Deploy
netlify deploy --prod --dir=build
```

### 3. Vercel

**Automatic Deployment:**
1. Connect your GitHub repository to Vercel
2. Vercel will automatically detect it's a Create React App
3. Add environment variables in Vercel dashboard

**Manual Deployment:**
```bash
# Install Vercel CLI
npm install -g vercel

# Deploy
vercel --prod
```

### 4. AWS S3 + CloudFront

1. **Create S3 Bucket:**
   ```bash
   aws s3 mb s3://your-time-tracking-app
   ```

2. **Configure S3 for Static Website Hosting:**
   ```bash
   aws s3 website s3://your-time-tracking-app --index-document index.html --error-document index.html
   ```

3. **Upload Build Files:**
   ```bash
   aws s3 sync build/ s3://your-time-tracking-app --delete
   ```

4. **Create CloudFront Distribution:**
   - Origin: Your S3 bucket
   - Default root object: `index.html`
   - Error pages: Redirect to `index.html` for 404 errors

5. **Set up CI/CD (Optional):**
   ```bash
   # Add to your CI pipeline
   aws s3 sync build/ s3://your-time-tracking-app --delete
   aws cloudfront create-invalidation --distribution-id YOUR_DISTRIBUTION_ID --paths "/*"
   ```

### 5. GitHub Pages

1. **Install gh-pages:**
   ```bash
   npm install --save-dev gh-pages
   ```

2. **Add to package.json:**
   ```json
   {
     "scripts": {
       "predeploy": "npm run build",
       "deploy": "gh-pages -d build"
     },
     "homepage": "https://yourusername.github.io/react_timetracking"
   }
   ```

3. **Deploy:**
   ```bash
   npm run deploy
   ```

### 6. Docker

1. **Build Docker Image:**
   ```bash
   docker build -t react-timetracking .
   ```

2. **Run Container:**
   ```bash
   docker run -p 8080:80 react-timetracking
   ```

3. **Deploy to Cloud:**
   ```bash
   # Example for AWS ECS
   docker tag react-timetracking:latest your-ecr-repo:latest
   docker push your-ecr-repo:latest
   ```

## Environment Variables

Set these environment variables in your deployment platform:

- `REACT_APP_AWS_REGION`: Your AWS region (e.g., us-east-1)
- `REACT_APP_COGNITO_USER_POOL_ID`: Your Cognito User Pool ID
- `REACT_APP_COGNITO_CLIENT_ID`: Your Cognito App Client ID
- `REACT_APP_NAME`: App name (optional)
- `REACT_APP_VERSION`: App version (optional)

## Post-Deployment Checklist

1. **Test Authentication**: Ensure users can sign up, sign in, and reset passwords
2. **Test Core Features**: Verify task creation, editing, and reporting work
3. **Check Mobile Responsiveness**: Test on various screen sizes
4. **Verify HTTPS**: Ensure your deployment uses HTTPS
5. **Monitor Performance**: Check loading times and bundle sizes
6. **Set up Monitoring**: Configure error tracking (e.g., Sentry)

## Troubleshooting

### Common Issues

1. **Authentication Errors:**
   - Verify AWS Cognito configuration
   - Check CORS settings in Cognito
   - Ensure environment variables are set correctly

2. **Routing Issues:**
   - Configure redirects for SPA routing
   - Ensure all routes redirect to `index.html`

3. **Build Failures:**
   - Check Node.js version compatibility
   - Verify all dependencies are installed
   - Review build logs for specific errors

4. **Performance Issues:**
   - Optimize bundle size
   - Enable compression
   - Use CDN for static assets

### Performance Optimization

1. **Enable Gzip Compression**
2. **Set Cache Headers**
3. **Optimize Images**
4. **Use CDN for Static Assets**
5. **Implement Service Worker for Caching**

## Security Considerations

1. **HTTPS Only**: Always use HTTPS in production
2. **Environment Variables**: Never commit sensitive data to version control
3. **CORS Configuration**: Configure allowed origins in AWS Cognito
4. **Content Security Policy**: Implement CSP headers
5. **Regular Updates**: Keep dependencies updated

## Monitoring and Analytics

Consider setting up:

1. **Error Tracking**: Sentry, LogRocket
2. **Performance Monitoring**: Google Analytics, Web Vitals
3. **User Analytics**: Mixpanel, Amplitude
4. **Server Monitoring**: AWS CloudWatch, New Relic

## Support

For deployment issues:

1. Check the troubleshooting section above
2. Review platform-specific documentation
3. Check AWS Cognito setup in `AWS_COGNITO_SETUP.md`
4. Verify environment variables are correctly set 