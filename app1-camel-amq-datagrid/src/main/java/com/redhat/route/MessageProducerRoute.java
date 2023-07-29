package com.redhat.route;

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.node.IntNode;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.infinispan.InfinispanConstants;
import org.apache.camel.component.infinispan.InfinispanOperation;
import org.apache.camel.model.dataformat.JsonLibrary;

public class MessageProducerRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:dg")
                .routeId("dg")
                .removeHeaders("*", "breadcrumbId")
                .setHeader(InfinispanConstants.OPERATION).constant(InfinispanOperation.PUT)
                .setHeader(InfinispanConstants.KEY).jq(".id", String.class)
                .setHeader(InfinispanConstants.LIFESPAN_TIME).constant(5L)
                .setHeader(InfinispanConstants.LIFESPAN_TIME_UNIT).constant(TimeUnit.MINUTES.toString())
                .setHeader(InfinispanConstants.VALUE, simple("${body}"))
                .to("infinispan:DATA-LAYER-CACHE?")
        ;
    }
}
