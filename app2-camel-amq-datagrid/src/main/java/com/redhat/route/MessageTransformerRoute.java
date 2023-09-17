package com.redhat.route;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.RollbackException;

import org.apache.camel.builder.RouteBuilder;

@ApplicationScoped
public class MessageTransformerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:get-registry-from-cache")
                .routeId("get-registry-from-cache")
                .onException(RollbackException.class)
                .redeliveryDelay(300L)
                .maximumRedeliveries(5)
                .useExponentialBackOff()
                .backOffMultiplier(2.5)
                .useOriginalMessage()
                .to("log:com.redhat.route?level=ERROR")
                .end()
                .log("Retrieving cached record for key ${in.header.REGISTRY_ID}")
                .process("customCacheManager");

    }
}
