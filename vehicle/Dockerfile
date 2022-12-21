FROM node:19

ARG arg_port=8100
ARG arg_vehicle_number=1

ENV port=${arg_port}
ENV vehicle_number=${arg_vehicle_number}

# Create app work directory
WORKDIR /usr/src/app

# Copy app dependencies
COPY ["package.json", "package-lock.json*", "./"]

# Install app dependencies
RUN npm install
# If you are building your code for production
# RUN npm ci --only=production

# Copy soure files (copy all locally stored files except those declared in .dockerignore)
COPY . .

RUN npm run build

# EXPOSE 8080
CMD [ "node", "index.js" ]