package com.cyboul.cloudplatform.common.events;

public record OrderCreatedEvent(
        String orderId,
        String userId,
        String productId,
        double amount
) {}