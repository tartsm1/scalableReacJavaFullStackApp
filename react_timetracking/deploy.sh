#!/bin/bash

# React Time Tracking App Deployment Script
# This script helps deploy the app to various platforms

set -e

echo "🚀 React Time Tracking App Deployment Script"
echo "============================================="

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ Error: .env file not found!"
    echo "Please copy env.example to .env and configure your AWS Cognito settings."
    exit 1
fi

# Build the application
echo "📦 Building the application..."
npm run build

if [ $? -eq 0 ]; then
    echo "✅ Build completed successfully!"
else
    echo "❌ Build failed!"
    exit 1
fi

# Check build output
if [ -d "build" ]; then
    echo "📁 Build directory created successfully"
    echo "📊 Build size:"
    du -sh build/
else
    echo "❌ Build directory not found!"
    exit 1
fi

# Deployment options
echo ""
echo "🎯 Choose deployment platform:"
echo "1) Local testing (serve build folder)"
echo "2) Netlify"
echo "3) Vercel"
echo "4) AWS S3 + CloudFront"
echo "5) GitHub Pages"
echo "6) Docker"
echo ""

read -p "Enter your choice (1-6): " choice

case $choice in
    1)
        echo "🌐 Starting local server..."
        if command -v serve &> /dev/null; then
            serve -s build -l 3000
        else
            echo "Installing serve globally..."
            npm install -g serve
            serve -s build -l 3000
        fi
        ;;
    2)
        echo "📤 Deploying to Netlify..."
        if command -v netlify &> /dev/null; then
            netlify deploy --prod --dir=build
        else
            echo "Please install Netlify CLI: npm install -g netlify-cli"
            echo "Then run: netlify deploy --prod --dir=build"
        fi
        ;;
    3)
        echo "📤 Deploying to Vercel..."
        if command -v vercel &> /dev/null; then
            vercel --prod
        else
            echo "Please install Vercel CLI: npm install -g vercel"
            echo "Then run: vercel --prod"
        fi
        ;;
    4)
        echo "📤 Deploying to AWS S3 + CloudFront..."
        echo "Please ensure you have AWS CLI configured and appropriate permissions."
        echo "You can use the AWS S3 sync command:"
        echo "aws s3 sync build/ s3://your-bucket-name --delete"
        echo "Then create/update CloudFront distribution."
        ;;
    5)
        echo "📤 Deploying to GitHub Pages..."
        echo "Please ensure you have gh-pages package installed:"
        echo "npm install --save-dev gh-pages"
        echo "Add to package.json scripts:"
        echo '"predeploy": "npm run build",'
        echo '"deploy": "gh-pages -d build"'
        echo "Then run: npm run deploy"
        ;;
    6)
        echo "🐳 Creating Docker deployment..."
        if [ ! -f "Dockerfile" ]; then
            cat > Dockerfile << 'EOF'
FROM nginx:alpine
COPY build/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
EOF
        fi
        
        if [ ! -f "nginx.conf" ]; then
            cat > nginx.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;
        
        location / {
            try_files $uri $uri/ /index.html;
        }
        
        location /static/ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
}
EOF
        fi
        
        echo "🐳 Building Docker image..."
        docker build -t react-timetracking .
        echo "🐳 Running Docker container..."
        docker run -p 8080:80 react-timetracking
        echo "🌐 App available at http://localhost:8080"
        ;;
    *)
        echo "❌ Invalid choice!"
        exit 1
        ;;
esac

echo ""
echo "✅ Deployment script completed!" 