# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build --configuration=production

# Run stage
FROM nginx:alpine
# In Angular 17+, the build output is usually inside dist/<project-name>/browser
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Copy the nginx template
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Cloud Run sets the PORT env var (default 8080)
# The nginx:alpine image natively processes templates in /etc/nginx/templates and outputs to /etc/nginx/conf.d
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
