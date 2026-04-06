package com.cyboul.eda.paymentservice;

import com.cyboul.eda.common.events.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

    @KafkaListener(topics = "order-created", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}, userId={}, productId={}, amount={}",
                event.orderId(), event.userId(), event.productId(), event.amount());

        // process payment
        System.out.println("Received OrderCreatedEvent: orderId=" + event.orderId());
    }
}
