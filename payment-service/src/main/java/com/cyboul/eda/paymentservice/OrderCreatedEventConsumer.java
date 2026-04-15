package com.cyboul.eda.paymentservice;

import com.cyboul.eda.common.events.OrderCreatedEvent;
import com.cyboul.eda.common.events.PaymentProceedEvent;
import com.cyboul.eda.common.events.PaymentStatus;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

    private final KafkaTemplate<String, PaymentProceedEvent> kafkaTemplate;
    private final PaymentRecordRepository paymentRecordRepository;

    @KafkaListener(topics = "order-created", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("PAYMENT: Received OrderCreatedEvent, orderId={}, userId={}, productId={}, amount={}",
                event.orderId(), event.userId(), event.productId(), event.amount());

        String paymentUuid = UUID.randomUUID().toString();
        PaymentStatus status = processPayment(event);
        notifySuccessOrFailure(paymentUuid, event, status);
        PaymentStatus status = processPayment(event.orderId());
        paymentRecordRepository.save(new PaymentRecord(event.orderId(), status, Instant.now()));
    }

    private PaymentStatus processPayment(String orderId) {
        log.info("PAYMENT: Processing...");
        PaymentStatus status = PaymentStatus.FAILED;
        try {
            TimeUnit.SECONDS.sleep(30);
            status = PaymentStatus.SUCCESS;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("PAYMENT: (!) Processing interrupted for orderId={}", orderId);
        }
        log.info("PAYMENT: Processed for orderId={} status={}", orderId, status.name());
        return status;
    }
}
