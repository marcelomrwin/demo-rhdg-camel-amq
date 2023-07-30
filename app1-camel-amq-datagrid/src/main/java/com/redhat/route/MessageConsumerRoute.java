package com.redhat.route;

import com.redhat.model.App1Model;
import com.redhat.model.Constants;
import org.apache.camel.builder.RouteBuilder;

public class MessageConsumerRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        from("amqp:queue:APP1_DATA_RECORD_QUEUE")
                .routeId("data_record_consumer")
                .log("{{camel.context.name}} BODY\n${body}")
                .setHeader(Constants.REGISTRY_ID).jq(".id", String.class)
                .unmarshal().json(App1Model.class)
                .to("direct:get-registry-from-cache")
        ;
    }
}
