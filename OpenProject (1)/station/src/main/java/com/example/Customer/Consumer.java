package com.example.Customer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;





@Component
public class Consumer {

    @RabbitListener(queues = "myQueue")
    public void processMessage(String message) {
        System.out.println("Received message: " + message);
    }

}
