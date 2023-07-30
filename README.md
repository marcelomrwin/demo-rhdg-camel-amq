# Demo

This project demonstrates how to use ![https://www.redhat.com/en/technologies/jboss-middleware/amq](Red Hat AMQ) + Red Hat Data Grid and Red Hat Build of Camel with Quarkus to create a data layer with a canonical data model.

## Run Separate

### Compile all projects
```shell
mvn clean install -DskipTests
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
cd app-cache-client
./mvnw quarkus:dev -Ddebug=false
```

## Run with Docker compose

### Compile all projects
```shell
./mvnw clean install -Dskiptests
```

### Execute all projects
```shell
docker compose -f docker-compose-complete.yml down && docker compose -f docker-compose-complete.yml up --build
```
