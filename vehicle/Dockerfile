FROM node:19

ARG ARG_PORT=8100
ARG ARG_VEHICLE_NUMBER=1

ENV ENV_PORT=${ARG_PORT}
ENV ENV_VEHICLE_NUMBER=${ARG_VEHICLE_NUMBER}


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