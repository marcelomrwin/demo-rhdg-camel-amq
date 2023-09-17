package com.redhat;

import io.quarkus.infinispan.client.Remote;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Startup
public class CacheConfig {

    private static Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Inject
    EventBus bus;

    @Inject
    @Remote("DATA-LAYER-CACHE")
    RemoteCache<String, String> cache;

    void onStart(@Observes StartupEvent ev) {
        logger.info("Application starting");
        cache.addClientListener(new CacheListener(bus));
    }

    //    @ApplicationScoped
    @ClientListener
    public static class CacheListener {
        EventBus bus;

        public CacheListener(EventBus eventBus) {
            this.bus = eventBus;
        }

        @ClientCacheEntryCreated
        public void entryCreated(ClientCacheEntryCreatedEvent<String> event) {
            logger.info("Added entry: {}", event.getKey());
            bus.publish("CACHE_EVENT", String.valueOf(event.getKey()));
        }

        @ClientCacheEntryModified
        public void entryModified(ClientCacheEntryModifiedEvent<String> event) {
            logger.info("Modified entry: {}", event.getKey());
            bus.publish("CACHE_EVENT", String.valueOf(event.getKey()));
        }
    }
}
