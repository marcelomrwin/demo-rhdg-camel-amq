```shell
docker build --build-arg API_HOST=app-camel-rest-cache-demo-rhdg-camel-amq.apps.ocp4.masales.cloud --build-arg API_PORT=80 --build-arg API_PROTOCOL=http . -t marcelodsales/nodejs-rest-client
HOST=$(oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}')
docker login -u kubeadmin -p $(oc whoami -t) $HOST
docker tag marcelodsales/nodejs-rest-client  $HOST/demo-rhdg-camel-amq/nodejs-rest-client:latest
docker push $HOST/demo-rhdg-camel-amq/nodejs-rest-client:latest
oc new-app --name nodejs-rest-client nodejs-rest-client:latest
oc expose service/nodejs-rest-client
```