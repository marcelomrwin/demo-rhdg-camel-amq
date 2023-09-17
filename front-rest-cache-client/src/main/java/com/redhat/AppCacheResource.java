package com.redhat;

import java.time.LocalTime;
import java.util.Optional;

import com.redhat.model.Registry;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api")
public class AppCacheResource {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Inject
    AppCacheService service;
    private Sse sse;
    private SseEventSink sseEventSink = null;

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
        logger.info("Receive request for key {}",id);
        try {
            Optional<Registry> registryOptional = service.query(id);
            if (registryOptional.isEmpty()) {
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
            logger.error(e.getMessage(), e);
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