# camel-rest-cache

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/app-cache-client-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Related Guides

- SmallRye Reactive Messaging - AMQP Connector ([guide](https://quarkus.io/guides/amqp)): Connect to AMQP with Reactive Messaging
- Infinispan Client ([guide](https://quarkus.io/guides/infinispan-client)): Connect to the Infinispan data grid for distributed caching

## Provided Code

### Reactive Messaging codestart

Use SmallRye Reactive Messaging

[Related Apache AMQP 1.0 guide section...](https://quarkus.io/guides/amqp)


### RESTEasy Reactive

Easily start your Reactive RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

## Forwarding openshift containers
```shell
oc port-forward artemis-ss-0 61616:61616
oc port-forward infinispan-0 11222:11222
```

## Generate a docker image
```shell
docker build -f src/main/docker/Dockerfile.jvm -t marcelodsales/camel-rest-cache:latest .
```

## Deploy image to Openshift
```shell
oc patch configs.imageregistry.operator.openshift.io/cluster --patch '{"spec":{"defaultRoute":true}}' --type=merge
HOST=$(oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}')
docker login -u kubeadmin -p $(oc whoami -t) $HOST
echo $HOST >> default-route-openshift-image-registry.apps.ocp4.masales.cloud
docker tag marcelodsales/camel-rest-cache  $HOST/demo-rhdg-camel-amq/camel-rest-cache:1.0-SNAPSHOT
docker push default-route-openshift-image-registry.apps.ocp4.masales.cloud/demo-rhdg-camel-amq/camel-rest-cache:1.0-SNAPSHOT
oc new-app --name camel-rest-cache camel-rest-cache:1.0-SNAPSHOT
```

## Run the image in local environment
```shell
docker run -it --rm --name camel-rest-cache \
-p 8089:8080 \
-e DG_USER=developer \
-e DG_PASS=ngTKaVlLcAZWjJCn \
-e AMQP_HOST=host.docker.internal \
-e DG_HOST=host.docker.internal \
-e QUARKUS_LOG_LEVEL=DEBUG \
default-route-openshift-image-registry.apps.ocp4.masales.cloud/demo-rhdg-camel-amq/camel-rest-cache:1.0-SNAPSHOT
```

## Deploy to Openshift
```shell
oc delete dc camel-rest-cache
oc delete bc camel-rest-cache
oc delete is camel-rest-cache
oc delete svc camel-rest-cache
./mvnw clean package -DskipTests -Dquarkus.kubernetes.deploy=true -Dquarkus.openshift.route.expose=true
```