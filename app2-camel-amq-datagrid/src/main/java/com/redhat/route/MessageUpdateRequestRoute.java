package com.redhat.route;

import org.apache.camel.builder.RouteBuilder;

public class MessageUpdateRequestRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("amqp:topic:CACHE_UPDATE_REQUEST")
                .routeId("cache_update_request")
                .log("Update request received for App2 with registration ID ${body}")
                .toD("http:{{app2.endpoint.url}}/message/random/${body}?bridgeEndpoint=true")
        ;
    }
}