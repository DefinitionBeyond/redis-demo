package com.lt.dev.model;

import org.springframework.stereotype.Component;

@Component
public class MessageSubscriber {

    public void onMessage(SimpleMessage message, String pattern) {

        if (message.getPublisher().equals("admin")) {
            System.out.println(String.format("niu bi topic {%s} received {%s} ", pattern, message.getContent()));
            System.out.println("OK ============= OK");
        } else {
            System.out.println(String.format("niu bi topic {%s} received {%s} ", pattern, message.getContent()));
            System.out.println("Test ============= Test");
        }
    }
}
