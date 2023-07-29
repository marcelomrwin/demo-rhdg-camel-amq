package com.redhat.aggregation;

import java.util.ArrayList;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.model.App2Model;
import com.redhat.model.Application;
import com.redhat.model.Constants;
import com.redhat.model.Herd;
import com.redhat.model.Registry;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

//the update can be total or partial
@ApplicationScoped
@Named("transformerStrategy")
public class TransformerStrategy implements AggregationStrategy {

    ObjectMapper objectMapper;

    @Inject
    public TransformerStrategy(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {

        App2Model model = oldExchange.getMessage().getBody(App2Model.class);
        Registry cacheModel = Registry.builder()
                .id(Integer.valueOf(oldExchange.getIn().getHeader(Constants.REGISTRY_ID, String.class)))
                .build();

        if (Objects.nonNull(newExchange.getMessage().getBody())) {
            try {
                cacheModel = objectMapper.readValue(newExchange.getMessage().getBody(String.class), Registry.class);
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

        oldExchange.getIn().setBody(cacheModel);

        return oldExchange;
    }
}

