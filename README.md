# Demo

This project demonstrates how to use [Red Hat AMQ](https://www.redhat.com/en/technologies/jboss-middleware/amq) + [Red Hat Data Grid](https://www.redhat.com/en/technologies/jboss-middleware/data-grid) and [Red Hat Build of Camel with Quarkus](https://access.redhat.com/documentation/en-us/red_hat_build_of_apache_camel_extensions_for_quarkus/) to create a data layer with a canonical data model.

## Requirements
* JDK 17+
* Docker runtime 24+
* Docker Compose 2.19+
* NPM 9.5+
* Node 18+

> **Note**
> The examples below were run on a MacOS Ventura 13.4.1

> **Warning**
> Before running the commands below, check your platform configuration to support the above requirements.

## Run Separate

### Compile all projects
```shell
./mvnw clean install -DskipTests
```

### Start the infra services
```shell
docker compose down && docker compose up
```

### Execute the Generator client (a fake source)
```shell
cd amq-client
./mvnw quarkus:dev -Ddebug=false
```

### Execute the First Integration layer
```shell
cd app1-camel-amq-datagrid
./mvnw quarkus:dev -Ddebug=false

cd app2-camel-amq-datagrid
./mvnw quarkus:dev -Ddebug=false
```

### Execute the second Integration layer
```shell
cd camel-rest-cache
./mvnw quarkus:dev -Ddebug=false
```

### Execute a custom app that can connect with datagrid and amq
```shell
cd app-cache-client
./mvnw quarkus:dev -Ddebug=false
```

### Execute the view client for camel-rest-cache
```shell
cd nodejs-rest-client
npm install
node server.js
```

---

## Run with Docker compose

### Compile all projects
```shell
./mvnw clean install -Dskiptests
```

### Execute all projects
```shell
docker compose -f docker-compose-complete.yml down && docker compose -f docker-compose-complete.yml up --build
```
