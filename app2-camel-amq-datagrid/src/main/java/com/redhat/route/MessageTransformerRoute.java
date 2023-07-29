package com.redhat.route;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.redhat.aggregation.TransformerStrategy;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.infinispan.InfinispanConstants;
import org.apache.camel.component.infinispan.InfinispanOperation;

@ApplicationScoped
public class MessageTransformerRoute extends RouteBuilder {

    @Inject
    TransformerStrategy transformerStrategy;

    @Override
    public void configure() throws Exception {
//recovery registry from cache (if exists)
        from("direct:get-registry-from-cache")
                .routeId("get-registry-from-cache")
                .log("Retrieving cached record for key ${in.header.REGISTRY_ID}")
                .setHeader(InfinispanConstants.OPERATION, constant(InfinispanOperation.GET))
                .setHeader(InfinispanConstants.KEY, simple("${in.header.REGISTRY_ID}"))
                .enrich("infinispan:DATA-LAYER-CACHE", transformerStrategy)
                .to("direct:convert-body")
        ;

        from("direct:convert-body")
                .marshal().json(true).convertBodyTo(String.class, "UTF-8")
                .to("direct:dg");

    }
}
