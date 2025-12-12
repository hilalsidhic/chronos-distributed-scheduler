# Stage 1: Build the React App
FROM node:18-alpine as build
WORKDIR /app

# Copy package files first to cache dependencies
COPY package*.json ./
RUN npm install --legacy-peer-deps

# Copy the rest of the code
COPY . .

# CRITICAL: Accept the API URL argument from Docker Compose
# and bake it into the React build as an environment variable.
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

# Build the app (creates the /dist folder)
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Copy the built files from Stage 1
COPY --from=build /app/dist /usr/share/nginx/html

# Copy our custom Nginx config (fixes React Router refresh 404s)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]