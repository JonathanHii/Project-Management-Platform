FROM node:20-alpine

WORKDIR /app

# Copy dependency files first 
COPY package*.json ./

# Install all dependencies
RUN npm ci

# Copy the rest of your frontend code
COPY . .

# grabs the vairable from docker compose here.
ARG NEXT_PUBLIC_API_URL 

# Sets it att run time
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL

# Build the Next.js application
RUN npm run build

# Open the port Next.js runs on
EXPOSE 3000

# Start the application
CMD ["npm", "start"]