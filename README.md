# Demo

## Run Separate

### Compile all projects
```shell
mvn clean package -DskipTests
```

### Start the infra services
```shell
docker compose down && docker compose up
```

### Execute the Generator client (a fake source)
```shell
cd amq-client
./mvnw clean quarkus:dev -Ddebug=false
```

### Execute the First Integration layer
```shell
cd app1-camel-amq-datagrid
./mvnw clean quarkus:dev -Ddebug=false

cd app2-camel-amq-datagrid
./mvnw clean quarkus:dev -Ddebug=false
```

### Execute the second Integration layer
```shell
cd app-cache-client
./mvnw clean quarkus:dev -Ddebug=false
```

## Run with Docker compose

### Compile all projects
```shell
mvn clean package -DskipTests
```

### Execute all projects
```shell
docker compose down -f docker-compose-complete.yml && docker compose up -f docker-compose-complete.yml --build
```
