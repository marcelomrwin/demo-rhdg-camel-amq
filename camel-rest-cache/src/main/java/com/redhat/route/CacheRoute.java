package com.redhat.route;

import com.redhat.model.ApiResponse;
import com.redhat.model.Registry;
import org.apache.camel.ExchangePattern;
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
                .log("Key ${in.header.CamelInfinispanKey} not found in cache DATA-LAYER-CACHE")
                .to(ExchangePattern.InOnly, "seda:request-update")
                .process(exchange -> {
                    ApiResponse response = ApiResponse.builder()
                            .message("Data will be loaded, please wait a few seconds")
                            .type(ApiResponse.ApiResponseType.LOAD)
                            .build();
                    exchange.getMessage().setBody(response);
                }).marshal().json(true)
        ;

        from("seda:request-update").routeId("request-update")
                .setBody(simple("${in.header.CamelInfinispanKey}"))
                .removeHeaders("CamelHttp*")
                .to("amqp:topic:CACHE_UPDATE_REQUEST");

        from("direct:cacheFound")
                .routeId("cacheFound")
                .log("Key ${in.header.CamelInfinispanKey} founded in cache DATA-LAYER-CACHE")
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
