package com.redhat.route;

import com.redhat.model.ApiResponse;
import com.redhat.model.Registry;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.infinispan.InfinispanConstants;
import org.apache.camel.component.infinispan.InfinispanOperation;

public class CacheRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:getRegistry")
                .routeId("getRegistry")
                .log("Retrieving cached record for key ${in.header.CamelInfinispanKey}")
                .setHeader(InfinispanConstants.OPERATION, constant(InfinispanOperation.GET))
                .to("infinispan:DATA-LAYER-CACHE")
                .choice()
                .when(body().isNull()).to("direct:cacheNotFound")
                .otherwise().to("direct:cacheFound")
                .end()
        ;

        from("direct:cacheNotFound").routeId("cacheNotFound")
                .setBody(simple("${in.header.CamelInfinispanKey}"))
                .to("amqp:topic:CACHE_UPDATE_REQUEST")
                .process(exchange -> {
                    ApiResponse response = ApiResponse.builder()
                            .message("Data will be loaded, please wait a few seconds")
                            .type(ApiResponse.ApiResponseType.LOAD)
                            .build();
                    exchange.getMessage().setBody(response);
                }).marshal().json(true)
        ;

        from("direct:cacheFound")
                .routeId("cacheFound")
                .unmarshal().json(Registry.class)
                .process(exchange -> {
                    ApiResponse response = ApiResponse.builder()
                            .message("Successfully recovered data")
                            .type(ApiResponse.ApiResponseType.DATA)
                            .registry(exchange.getMessage().getBody(Registry.class))
                            .build();
                    exchange.getMessage().setBody(response);
                }).marshal().json(true)
        ;

    }
}
