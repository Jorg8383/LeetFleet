FROM node:19

ARG ARG_FIRST_SERVER_PORT=8200

ENV ENV_FIRST_SERVER_PORT=${ARG_FIRST_SERVER_PORT}

# Create app work directory
WORKDIR /usr/src/app

# Copy app dependencies
COPY ["package.json", "package-lock.json*", "./"]

RUN npm run clean

# Install app dependencies
RUN npm install
# If you are building your code for production
# RUN npm ci --only=production

# Copy soure files (copy all locally stored files except those declared in .dockerignore)
COPY . .

RUN npm run build

# EXPOSE 8080
CMD [ "node", "index.js" ]