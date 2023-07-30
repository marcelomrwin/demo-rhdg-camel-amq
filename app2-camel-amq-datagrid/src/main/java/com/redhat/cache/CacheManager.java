package com.redhat.cache;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.TransactionManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.model.App2Model;
import com.redhat.model.Application;
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

@ApplicationScoped
@Named("customCacheManager")
public class CacheManager implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

    @Inject
    ObjectMapper objectMapper;

    RemoteCache<String, String> cache;

    @Inject
    @ConfigProperty(name = "quarkus.infinispan-client.host")
    String infinispanHost;

    @Inject
    @ConfigProperty(name = "quarkus.infinispan-client.auth-username")
    String infinispanUser;

    @Inject
    @ConfigProperty(name = "quarkus.infinispan-client.auth-password")
    String infinispanPassword;

    @Inject
    @ConfigProperty(name = "quarkus.infinispan-client.remote.cache")
    String infinispanRemoteCache;

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

        TransactionManager transactionManager = cache.getTransactionManager();
        transactionManager.begin();

        App2Model model = exchange.getMessage().getBody(App2Model.class);

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

        for (App2Model.Loan loan : model.getLoans()) {
            cacheModel.getApplications().add(Application.builder()
                    .schemeCode(model.getAppId())
                    .appAmount(loan.getValue())
                    .appStatus(loan.getStatus())
                    .build());
        }

        if (Objects.isNull(cacheModel.getHerds())) cacheModel.setHerds(new ArrayList<>());

        for (App2Model.Flock flock : model.getFlocks()) {
            cacheModel.getHerds().add(Herd.builder()
                    .count(flock.getTotal()).herdType(flock.getType()).herdSubType(flock.getLocation())
                    .build());
        }

        exchange.getIn().setBody(cacheModel);

        cache.put(String.valueOf(model.getId()), objectMapper.writeValueAsString(cacheModel));
        transactionManager.commit();
        logger.info("Cache key {} updated",model.getId());
    }
}
