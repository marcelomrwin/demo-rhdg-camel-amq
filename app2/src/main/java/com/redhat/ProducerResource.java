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
    public static int randomInt() {
        return random.nextInt(1000) + 1;
    }

    public static double randomDouble() {
        return BigDecimal.valueOf(random.nextDouble(1000) + 1).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }
    @Inject
    JMSClient jmsClient;

    @Inject
    @TemplatePath("request.ftl")
    Template requestTemplate;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public Response app2SendMessage(String xmlString) {
        try {
            logger.info("sending message\n {}",xmlString);
            jmsClient.sendMessage(xmlString,"APP2_DATA_RECORD_QUEUE");
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            return Response.serverError().entity(e).build();
        }
    }

    @POST
    @Path("/random/{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendRandomValues(@PathParam("id") String id) {
        try {
            StringWriter stringWriter = new StringWriter();
            requestTemplate.process(Map.of("id", id, "amount", String.valueOf(randomDouble()), "count", String.valueOf(randomInt())), stringWriter);
            String xmlString = stringWriter.toString();
            logger.info("sending message\n {}",xmlString);
            jmsClient.sendMessage(xmlString, "APP2_DATA_RECORD_QUEUE");
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            return Response.serverError().entity(e).build();
        }
    }


}