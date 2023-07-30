package com.redhat.route;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.TransactionManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.model.Application;
import com.redhat.model.Constants;
import com.redhat.model.Herd;
import com.redhat.model.Registry;
import io.quarkus.runtime.StartupEvent;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.hotrod.transaction.lookup.GenericTransactionManagerLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCacheProcessor implements Processor {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractCacheProcessor.class);

    @Inject
    protected ObjectMapper objectMapper;

    protected RemoteCache<String, String> cache;

    @Inject
    @ConfigProperty(name = "quarkus.infinispan-client.host")
    protected String infinispanHost;

    @Inject
    @ConfigProperty(name = "quarkus.infinispan-client.auth-username")
    protected String infinispanUser;

    @Inject
    @ConfigProperty(name = "quarkus.infinispan-client.auth-password")
    protected String infinispanPassword;

    @Inject
    @ConfigProperty(name = "quarkus.infinispan-client.remote.cache")
    protected String infinispanRemoteCache;

    void onStart(@Observes StartupEvent ev) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.clientIntelligence(ClientIntelligence.BASIC);
        cb.addServer().host(infinispanHost).port(ConfigurationProperties.DEFAULT_HOTROD_PORT).security().authentication().username(infinispanUser).password(infinispanPassword);
        cb.transactionTimeout(1L, TimeUnit.MINUTES);
        cb.remoteCache(infinispanRemoteCache).transactionManagerLookup(GenericTransactionManagerLookup.getInstance()).transactionMode(TransactionMode.NON_XA);
        RemoteCacheManager cacheManager = new RemoteCacheManager(cb.build());
        cache = cacheManager.getCache(infinispanRemoteCache);
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        TransactionManager tx = cache.getTransactionManager();
        tx.begin();

        String registryId = exchange.getIn().getHeader(Constants.REGISTRY_ID, String.class);

        Registry cacheModel = Registry.builder()
                .id(Integer.valueOf(registryId))
                .build();

        processModel(exchange,cacheModel);

        exchange.getIn().setBody(cacheModel);
        cache.put(registryId, objectMapper.writeValueAsString(cacheModel));
        tx.commit();
        logger.info("Cache key {} updated",registryId);
    }

    protected abstract void processModel(Exchange exchange, Registry cacheModel);
}
