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
# If the build output is just dist/frontend, you can uncomment the line below instead
# COPY --from=build /app/dist/frontend /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
