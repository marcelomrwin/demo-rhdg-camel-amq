package com.redhat.cache;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.model.App1Model;
import com.redhat.model.Application;
import com.redhat.model.Herd;
import com.redhat.model.Registry;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.TransactionManager;
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

@ApplicationScoped
@Named("customCacheManager")
public class DemoCacheManager implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(DemoCacheManager.class);

    @Inject
    ObjectMapper objectMapper;

    RemoteCache<String, String> cache;

    @Inject
    @ConfigProperty(name = "demo.infinispan-client.host")
    String infinispanHost;

    @Inject
    @ConfigProperty(name = "demo.infinispan-client.auth-username")
    String infinispanUser;

    @Inject
    @ConfigProperty(name = "demo.infinispan-client.auth-password")
    String infinispanPassword;

    @Inject
    @ConfigProperty(name = "demo.infinispan-client.remote.cache")
    String infinispanRemoteCache;

    RemoteCacheManager cacheManager;

    void onStart(@Observes StartupEvent ev) {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.clientIntelligence(ClientIntelligence.BASIC);
        cb.addServer().host(infinispanHost).port(ConfigurationProperties.DEFAULT_HOTROD_PORT).security().authentication().username(infinispanUser).password(infinispanPassword);
        cb.transactionTimeout(1L, TimeUnit.MINUTES);
        cb.remoteCache(infinispanRemoteCache)
                .transactionManagerLookup(GenericTransactionManagerLookup.getInstance())
                .transactionMode(TransactionMode.NON_XA);

        cacheManager = new RemoteCacheManager(cb.build());
        cacheManager.start();

    }

    @Override
    public void process(Exchange exchange) throws Exception {
        cache = cacheManager.getCache(infinispanRemoteCache);
        cache.start();

        logger.info("Processing {} for cache {}, is stats? {}", this, cache.getName(), cache.stats().getStatsMap());
        TransactionManager transactionManager = cache.getTransactionManager();
        transactionManager.begin();

        App1Model model = exchange.getMessage().getBody(App1Model.class);
        Registry cacheModel = Registry.builder()
                .id(model.getId())
                .build();
        String cacheValue = cache.get(String.valueOf(model.getId()));

        if (Objects.nonNull(cacheValue)) {
            try {
                cacheModel = objectMapper.readValue(cacheValue, Registry.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        //update cacheModel from app model
        cacheModel.setRoleCode(model.getAttribute());

        if (Objects.isNull(cacheModel.getApplications())) cacheModel.setApplications(new ArrayList<>());

        for (App1Model.Loan loan : model.getLoans()) {
            cacheModel.getApplications().add(Application.builder()
                    .schemeCode(model.getAppId())
                    .appAmount(loan.getValue())
                    .appStatus(loan.getStatus())
                    .build());
        }

        if (Objects.isNull(cacheModel.getHerds())) cacheModel.setHerds(new ArrayList<>());

        for (App1Model.Flock flock : model.getFlocks()) {
            cacheModel.getHerds().add(Herd.builder()
                    .count(flock.getTotal()).herdType(flock.getType()).herdSubType(flock.getLocation())
                    .build());
        }

        exchange.getIn().setBody(cacheModel);
        cache.put(String.valueOf(model.getId()), objectMapper.writeValueAsString(cacheModel));
        transactionManager.commit();
        logger.info("Cache key {}", model.getId());
    }
}
