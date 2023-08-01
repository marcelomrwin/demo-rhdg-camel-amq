package com.redhat.route;

import com.redhat.model.ApiResponse;
import org.apache.camel.builder.RouteBuilder;

public class RestRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        rest("/api").enableCORS(true)
                .get("/{CamelInfinispanKey}").outType(ApiResponse.class)
                .to("direct:getRegistry");
    }
}
