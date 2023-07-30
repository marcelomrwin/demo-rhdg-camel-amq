package com.redhat.route;

import com.redhat.util.RandomNumbersUtil;
import org.apache.camel.builder.RouteBuilder;

public class MessageUpdateRequestRoute extends RouteBuilder {
    RandomNumbersUtil numbersUtil = new RandomNumbersUtil();

    @Override
    public void configure() throws Exception {
        from("amqp:topic:CACHE_UPDATE_REQUEST")
                .routeId("cache_update_request")
                .log("Update request received for App1 and registration ${body}")
                .setHeader("RANDOM_AMOUNT").method(numbersUtil, "randomDouble")
                .setHeader("RANDOM_COUNT").method(numbersUtil, "randomInt")
                .to("freemarker:sample-request.ftl")
                .log("Sending sample body \n${body}\nto APP1_DATA_RECORD_QUEUE")
                .to("amqp:queue:APP1_DATA_RECORD_QUEUE");
    }
}
