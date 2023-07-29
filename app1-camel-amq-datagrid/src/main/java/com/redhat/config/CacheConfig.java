package com.redhat.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.infinispan.client.Remote;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import org.infinispan.client.hotrod.RemoteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Startup
public class CacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Inject
    @Remote("DATA-LAYER-CACHE")
    RemoteCache<String, String> cache;

    void onStart(@Observes StartupEvent ev) {
        logger.info("Application starting");
        logger.info(cache.getRemoteCacheContainer().getConfiguration().toString());
    }


}
