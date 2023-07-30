package com.redhat;

import java.time.LocalTime;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

import com.redhat.model.Registry;
import io.quarkus.vertx.ConsumeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api")
public class AppCacheResource {

    @Inject
    AppCacheService service;

    private Sse sse;
    private SseEventSink sseEventSink = null;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void consume(@Context SseEventSink sseEventSink, @Context Sse sse) {
        this.sse = sse;
        this.sseEventSink = sseEventSink;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRegistry(String id) {
        try {
            Optional<Registry> registryOptional = service.query(id);
            if (registryOptional.isEmpty()) {
                service.updateCache(id);
                ApiResponse response = ApiResponse.builder()
                        .message("Data will be loaded, please wait a few seconds")
                        .type(ApiResponse.ApiResponseType.LOAD)
                        .build();
                return Response.ok(response).build();
            } else {
                ApiResponse response = ApiResponse.builder()
                        .message("Successfully recovered data")
                        .type(ApiResponse.ApiResponseType.DATA)
                        .registry(registryOptional.get())
                        .build();
                return Response.ok(response).build();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return Response.serverError().entity(e).build();
        }
    }

    @ConsumeEvent("CACHE_EVENT")
    public void consume(String cacheKey) {
        logger.info("Update content key {}", cacheKey);
        try {
            if (sseEventSink != null) {
                Registry registry = service.query(cacheKey).get();
                OutboundSseEvent sseEvent = sse.newEventBuilder()
                        .id(cacheKey)
                        .mediaType(MediaType.APPLICATION_JSON_TYPE)
                        .data(registry)
                        .reconnectDelay(3000)
                        .comment("Event generated at: " + LocalTime.now())
                        .build();
                sseEventSink.send(sseEvent);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}