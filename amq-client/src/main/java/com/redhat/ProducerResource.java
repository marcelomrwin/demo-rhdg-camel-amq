package com.redhat;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/message")
public class ProducerResource {

    @Inject
    JMSClient jmsClient;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Path("/app1")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response app1SendMessage(String jsonMessage) {
        try {
            jmsClient.sendMessage(jsonMessage,"APP1_DATA_RECORD_QUEUE");
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            return Response.serverError().entity(e).build();
        }
    }

    @Path("/app2")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public Response app2SendMessage(String xmlString) {
        try {

            logger.warn(xmlString);
            jmsClient.sendMessage(xmlString,"APP2_DATA_RECORD_QUEUE");

            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            return Response.serverError().entity(e).build();
        }
    }


}