package com.redhat;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import freemarker.template.Template;
import io.quarkiverse.freemarker.TemplatePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/message")
public class ProducerResource {

    static Random random = new Random();
    protected Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    JMSClient jmsClient;
    @Inject
    @TemplatePath("request.ftl")
    Template requestTemplate;

    public static int randomInt() {
        return random.nextInt(1000) + 1;
    }

    public static double randomDouble() {
        return BigDecimal.valueOf(random.nextDouble(1000) + 1).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response app1SendMessage(String jsonMessage) {
        try {
            logger.info("sending message\n {}",jsonMessage);
            jmsClient.sendMessage(jsonMessage, "APP1_DATA_RECORD_QUEUE");
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            return Response.serverError().entity(e).build();
        }
    }

    @POST
    @Path("/random/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendRandomValues(@PathParam("id") String id) {
        try {
            StringWriter stringWriter = new StringWriter();
            requestTemplate.process(Map.of("id", id, "amount", String.valueOf(randomDouble()), "count", String.valueOf(randomInt())), stringWriter);
            String jsonMessage = stringWriter.toString();
            logger.info("sending message\n {}",jsonMessage);
            jmsClient.sendMessage(jsonMessage, "APP1_DATA_RECORD_QUEUE");
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            return Response.serverError().entity(e).build();
        }
    }

}