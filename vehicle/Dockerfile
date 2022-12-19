FROM node:16
ENV PORT=8100

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
COPY ["package.json", "package-lock.json*", "./"]

RUN npm install
# If you are building your code for production
# RUN npm ci --only=production

# Bundle app source
COPY . .

# EXPOSE 8080
CMD [ "node", "index.js" ]