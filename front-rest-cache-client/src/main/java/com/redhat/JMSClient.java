package com.redhat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JMSClient {
    private final ConnectionFactory connectionFactory;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public JMSClient(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void sendRequest(String id) {
        logger.info("sending JMS request for key {}", id);
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            TextMessage textMessage = context.createTextMessage(id);
            context.createProducer().send(context.createTopic("CACHE_UPDATE_REQUEST"), textMessage);
        }
    }
}
