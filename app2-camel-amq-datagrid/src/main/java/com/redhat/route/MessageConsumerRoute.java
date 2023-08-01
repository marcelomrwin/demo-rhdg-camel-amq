package com.redhat.route;

import com.redhat.model.App2Model;
import com.redhat.model.Constants;
import org.apache.camel.builder.RouteBuilder;

public class MessageConsumerRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        from("amqp:queue:APP2_DATA_RECORD_QUEUE")
                .routeId("data_record_consumer")
                .log("{{camel.context.name}} BODY: [${body}]HEADERS:\n${headers}")
                .choice()
                .when(body().convertToString().isNotEqualTo(""))
                .setHeader(Constants.REGISTRY_ID).xpath("/request/@id", String.class)
                .unmarshal().jacksonXml(App2Model.class)
                .to("direct:get-registry-from-cache")
                .end()
        ;
    }
}
