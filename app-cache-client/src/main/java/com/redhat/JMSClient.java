package com.redhat;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.TextMessage;
@ApplicationScoped
public class JMSClient {

    private final ConnectionFactory connectionFactory;

    public JMSClient(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void sendRequest(String id) {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            TextMessage textMessage = context.createTextMessage(id);
            context.createProducer().send(context.createTopic("CACHE_UPDATE_REQUEST"), textMessage);
        }
    }
}
