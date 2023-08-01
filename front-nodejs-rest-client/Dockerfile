FROM node:18-slim

ARG API_HOST
ARG API_PORT
ARG API_PROTOCOL

WORKDIR /usr/src/app

COPY package*.json ./

RUN npm ci --only=production

COPY . .

RUN sed -i "s/http\:\/\/localhost\:8089/$API_PROTOCOL:\/\/$API_HOST:$API_PORT/g" script.js

EXPOSE 3000

CMD ["node", "server.js"]