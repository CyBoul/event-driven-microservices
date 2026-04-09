package com.cyboul.eda.notifservice;

import com.cyboul.eda.common.events.PaymentProceedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class PaymentProceedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentProceedEventConsumer.class);

    private final Sinks.Many<NotificationEvent> notificationSink;
    private final UserContactRepository userContactRepository;

    @KafkaListener(topics = "payment-proceed", groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentProceed(PaymentProceedEvent event) {
        log.info("Notif: Received PaymentProceedEvent, payment:{} status:{}", event.paymentId(), event.status());

        switch (event.status()) {
            case FAILED, CANCELLED -> notifyFrontEnd(event);
            case SUCCESS -> {
                notifyFrontEnd(event);
                sendOrderConfirmationEmailToCustomer(event);
            }
            default -> log.warn("Unhandled PaymentStatus: {}", event.status());
        }
    }

    private void sendOrderConfirmationEmailToCustomer(PaymentProceedEvent event) {
        String userId = event.order().userId();
        UserContactProjection contact = userContactRepository.findById(userId).block();

        if (contact == null) {
            log.warn("NOTIF: No user contact found for userId={}, cannot send confirmation email", userId);
            return;
        }

        // contact.getName() & contact.getEmail() can be used, but don't log !
        log.info("NOTIF: Sending Order confirmation email to {} \nTransaction: {}\n" +
                        "Order details: {{Product: {}, Quantity: {}}}",
                userId, event.paymentId(),
                event.order().productId(), event.order().amount());
    }

    private void notifyFrontEnd(PaymentProceedEvent event) {
        Sinks.EmitResult result = notificationSink
                .tryEmitNext(new NotificationEvent("payment-update", event));

        if (result.isFailure() && result != Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER) {
            log.warn("Failed to push notification to frontend: {}", result);
        }
    }
}
