# Deployment Guide

This guide provides instructions for deploying your React Time Tracking App to various platforms.

## Prerequisites

Before deploying, ensure you have:

1. **Node.js 22+**: Required for building the application
2. **AWS Cognito Setup**: Follow the instructions in `AWS_COGNITO_SETUP.md`
3. **Production Build**: Update `app-config.js` with your AWS settings. Run `npm run build` to create the production build in the `build/` directory. For flexible deployment options, deploy scripts should overwrite `app-config.js` with the correct settings. 
This enables possibility to change configuration without rebuilding the application.

## Deployment Options

### 1. Local Testing

Test your production build locally using Caddy server:


# To preview the production build start java_timetracking and run caddy in project root 
```bash
java -jar java_timetracking/target/timetracking-0.0.1-SNAPSHOT.jar
```
```bash
caddy run
```

Visit `https://localhost` to test your app.

### 2. Terraform + AWS EKS

[Sample Terraform project](https://github.com/tartsm1/prodKubernetesTerraform)

### 3. AWS S3 + CloudFront

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
   - Error pages: Redirect to `index.html` for 404 errors (for SPA routing)

5. **Set up CI/CD (Optional):**
   ```bash
   # Add to your CI pipeline
   aws s3 sync build/ s3://your-time-tracking-app --delete
   aws cloudfront create-invalidation --distribution-id YOUR_DISTRIBUTION_ID --paths "/*"
   ```

### 4. Docker

The project includes a multi-stage Dockerfile using Node 22 for building and nginx:alpine for serving.

1. **Build Docker Image:**
   ```bash
   docker build -t react_timetracking .
   ```

2. **Run Container:**
   ```bash
   docker run -p 8080:80 react_timetracking
   ```

   Visit `http://localhost:8080` to access the app.

3. **Build with Environment Variables:**
   ```bash
   docker build \
     --build-arg VITE_AWS_REGION=us-east-1 \
     --build-arg VITE_COGNITO_USER_POOL_ID=your-pool-id \
     --build-arg VITE_COGNITO_CLIENT_ID=your-client-id \
     -t react_timetracking .
   ```

4. **Deploy to Cloud:**
   ```bash
   # Example for AWS ECR
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com
   docker tag react_timetracking:latest YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/react_timetracking:latest
   docker push YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/react_timetracking:latest
   ```

## Environment Variables

Set these environment variables in your deployment platform:

| Variable | Required | Description |
|----------|----------|-------------|
| `VITE_AWS_REGION` | Yes | Your AWS region (e.g., `us-east-1`) |
| `VITE_COGNITO_USER_POOL_ID` | Yes | Your Cognito User Pool ID |
| `VITE_COGNITO_CLIENT_ID` | Yes | Your Cognito App Client ID |
| `VITE_NAME` | No | App name (default: "Time Tracking App") |
| `VITE_VERSION` | No | App version |

> [!IMPORTANT]
> Vite environment variables must be prefixed with `VITE_` to be exposed to the client-side code. Variables are embedded at build time, not runtime.

## Post-Deployment Checklist

1. **Test Authentication**: Ensure users can sign up, sign in, and reset passwords
2. **Test Core Features**: Verify task creation, editing, and reporting work
3. **Check Mobile Responsiveness**: Test on various screen sizes
4. **Verify HTTPS**: Ensure your deployment uses HTTPS
5. **Test PWA Features**: Verify the app can be installed and works offline
6. **Monitor Performance**: Check loading times and bundle sizes
7. **Set up Monitoring**: Configure error tracking (e.g., Sentry)

## Troubleshooting

### Common Issues

1. **Authentication Errors:**
   - Verify AWS Cognito configuration
   - Check CORS settings in Cognito (add your deployment URL)
   - Ensure environment variables are set correctly at build time

2. **Routing Issues (404 on refresh):**
   - Configure redirects for SPA routing
   - Ensure all routes redirect to `index.html`
   - For nginx, the `docker_nginx.conf` handles this automatically

3. **Build Failures:**
   - Requires Node.js 22+
   - Run `npm install` before building
   - Check TypeScript errors with `npx tsc --noEmit`
   - Review build logs for specific errors

4. **Environment Variables Not Working:**
   - Ensure variables are prefixed with `VITE_`
   - Variables must be set before `npm run build`
   - For Docker, use `--build-arg` during build

5. **Performance Issues:**
   - Optimize bundle size with `npm run build -- --analyze`
   - Enable compression on your hosting platform
   - Use CDN for static assets

### Performance Optimization

1. **Enable Gzip/Brotli Compression** on your server
2. **Set Cache Headers** (nginx config included handles this)
3. **Optimize Images** before adding to the project
4. **Use CDN for Static Assets**
5. **Leverage PWA Caching** (already configured via vite-plugin-pwa)

## Security Considerations

1. **HTTPS Only**: Always use HTTPS in production
2. **Environment Variables**: Never commit `.env` files to version control
3. **CORS Configuration**: Configure allowed origins in AWS Cognito
4. **Content Security Policy**: Implement CSP headers in your server config
5. **Regular Updates**: Keep dependencies updated (`npm audit`)

## Monitoring and Analytics

Consider setting up:

1. **Error Tracking**: Sentry, LogRocket
2. **Performance Monitoring**: Google Analytics, Web Vitals (already included)
3. **User Analytics**: Mixpanel, Amplitude
4. **Server Monitoring**: AWS CloudWatch, New Relic

## Support

For deployment issues:

1. Check the troubleshooting section above
2. Review platform-specific documentation
3. Check AWS Cognito setup in `AWS_COGNITO_SETUP.md`
4. Verify environment variables are correctly set at build time