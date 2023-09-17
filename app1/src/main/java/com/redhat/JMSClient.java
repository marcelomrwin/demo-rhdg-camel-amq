package com.redhat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.TextMessage;

@ApplicationScoped
public class JMSClient {

    private final ConnectionFactory connectionFactory;

    public JMSClient(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void sendMessage(String message,String queue) {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            TextMessage textMessage = context.createTextMessage(message);
            context.createProducer().send(context.createQueue(queue), textMessage);
        }
    }

}
