package com.redhat;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.model.Registry;
import io.quarkus.infinispan.client.Remote;
import org.infinispan.client.hotrod.RemoteCache;

@ApplicationScoped
public class AppCacheService {
    @Inject
    JMSClient jmsClient;

    @Inject
    @Remote("DATA-LAYER-CACHE")
    RemoteCache<String, String> cache;
    @Inject
    ObjectMapper objectMapper;

    public Optional<Registry> query(String id) throws JsonProcessingException {

        String cacheRegistry = cache.get(id);
        if (Objects.isNull(cacheRegistry)) {
            jmsClient.sendRequest(id);
            return Optional.empty();
        } else {
            Registry registry = objectMapper.readValue(cacheRegistry, Registry.class);
            return Optional.of(registry);
        }
    }

    public void updateCache(String id) {

    }


}
