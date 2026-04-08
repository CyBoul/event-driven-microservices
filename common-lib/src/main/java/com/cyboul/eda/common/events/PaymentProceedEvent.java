package com.cyboul.eda.common.events;

public record PaymentProceedEvent(
    String paymentId,
    OrderCreatedEvent order,
    PaymentStatus status
){}
