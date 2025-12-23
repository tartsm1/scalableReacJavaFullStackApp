# AWS Cognito Setup for Time Tracking App

This guide will help you set up AWS Cognito User Pool for authentication in your React time tracking application.

## Prerequisites

- AWS Account
- AWS CLI configured (optional but recommended)
- Basic knowledge of AWS services

## Step 1: Create AWS Cognito User Pool

### Option A: Using AWS Console

1. **Sign in to AWS Console**
   - Go to https://console.aws.amazon.com/
   - Navigate to Amazon Cognito service

2. **Create User Pool**
   - Click "Create user pool"
   - Choose "Cognito user pool" (not hosted UI)

3. **Configure Sign-in Experience**
   - **User name requirements**: Choose "Allow email addresses and user names"
   - **User name case sensitivity**: Choose "Case insensitive"
   - **Sign-in options**: Select "User name" and "Email"
   - Click "Next"

4. **Configure Security Requirements**
   - **Password policy**: Choose "Cognito defaults" or customize
   - **Multi-factor authentication**: Choose "No MFA" for simplicity (or enable if needed)
   - **User account recovery**: Enable "Self-service account recovery"
   - Click "Next"

5. **Configure Sign-up Experience**
   - **Self-service sign-up**: Enable
   - **Cognito-assisted verification and confirmation**: Enable
   - **Verification message**: Choose "Email"
   - **Required attributes**: Select "Email" and "Name" (optional)
   - Click "Next"

6. **Configure Message Delivery**
   - **Email provider**: Choose "Send email with Cognito"
   - **From email address**: Use the default or create a custom one
   - Click "Next"

7. **Integrate Your App**
   - **User pool name**: Enter a name (e.g., "TimeTrackingUserPool")
   - **Initial app client**: Choose "Public client"
   - **App client name**: Enter a name (e.g., "TimeTrackingApp")
   - **Client secret**: Choose "Don't generate a client secret"
   - Click "Next"

8. **Review and Create**
   - Review your settings
   - Click "Create user pool"

### Option B: Using AWS CLI
###tegin terraformiga

```bash
# Create User Pool
aws cognito-idp create-user-pool \
  --pool-name "TimeTrackingUserPool" \
  --policies '{
    "PasswordPolicy": {
      "MinimumLength": 8,
      "RequireUppercase": true,
      "RequireLowercase": true,
      "RequireNumbers": true,
      "RequireSymbols": false
    }
  }' \
  --auto-verified-attributes email \
  --username-attributes email \
  --schema '[
    {
      "Name": "email",
      "AttributeDataType": "String",
      "Required": true,
      "Mutable": true
    }
  ]'

# Create App Client
aws cognito-idp create-user-pool-client \
  --user-pool-id YOUR_USER_POOL_ID \
  --client-name "TimeTrackingApp" \
  --no-generate-secret \
  --explicit-auth-flows ALLOW_USER_PASSWORD_AUTH ALLOW_REFRESH_TOKEN_AUTH
```

## Step 2: Get Your Configuration Values

After creating the User Pool, you'll need these values:

1. **User Pool ID**: Found in the User Pool details (format: `region_poolid`)
2. **App Client ID**: Found in the App integration tab
3. **Region**: The AWS region where you created the User Pool

## Step 3: Update Your Application Configuration

1. **Update `env_dev` and `app-config.js` for production **:
   ```json
   window.APP_CONFIG = {
      "aws_region": "eu-north-1",
      "cognito_user_pool_id": "eu-north-1_your pool",
      "cognito_client_id": "cognito client id"
      };
     ```

2. **Replace the placeholder values in `env_dev` with your actual values**:
 REACT_APP_AWS_REGION=eu-north-1

# AWS Cognito User Pool ID (format: region_poolid)
# Found in AWS Console > Cognito > User Pools > Your Pool > General Settings
REACT_APP_COGNITO_USER_POOL_ID=eu-north-1_XXXX

# AWS Cognito App Client ID
# Found in AWS Console > Cognito > User Pools > Your Pool > App Integration > App Clients
REACT_APP_COGNITO_CLIENT_ID=XXXXXX

#
## Step 4: Test Your Setup

1. **Start your development server**:
   ```bash
   npm start
   ```

2. **Test the authentication flow**:
   - Navigate to your app
   - You should see a login screen
   - Try creating a new account
   - Verify your email with the confirmation code
   - Sign in with your credentials

## Step 5: Production Configuration

For production deployment:

1. **Configure CORS** (if needed):
   - In your User Pool settings, add your domain to the allowed origins

2. **Set up custom domain** (optional):
   - Configure a custom domain for your Cognito hosted UI
   - Update the configuration accordingly

## Troubleshooting

### Common Issues:

1. **"User does not exist" error**:
   - Make sure the user is confirmed in the User Pool
   - Check that the username/email is correct

2. **"Invalid client" error**:
   - Verify your App Client ID is correct
   - Make sure the App Client is configured for your User Pool

3. **"Invalid credentials" error**:
   - Check that your User Pool ID and region are correct
   - Verify the App Client is properly configured

4. **CORS errors**:
   - Add your domain to the allowed origins in Cognito settings
   - Check that your API endpoints are properly configured

## Security Best Practices

1. **Use HTTPS in production**
2. **Implement proper password policies**
3. **Enable MFA for sensitive applications**
4. **Regularly rotate App Client secrets** (if using confidential clients)
5. **Monitor authentication logs**
6. **Implement proper session management**

## Additional Features

You can enhance your authentication by:

1. **Adding social login** (Google, Facebook, etc.)
2. **Implementing custom authentication flows**
3. **Adding user groups and roles**
4. **Setting up custom attributes**
5. **Implementing advanced security features**

For more information, refer to the [AWS Cognito Developer Guide](https://docs.aws.amazon.com/cognito/latest/developerguide/). 