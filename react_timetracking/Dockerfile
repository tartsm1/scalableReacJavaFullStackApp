# Build Stage
FROM node:22 AS builder

# Optional: App Name
ENV VITE_NAME="Time Tracking App"

# Optional: App Version
ENV VITE_VERSION=1.0.0 

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Run Stage
FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY docker_nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
