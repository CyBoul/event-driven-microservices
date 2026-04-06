package com.cyboul.eda.common.events;

public record OrderCreatedEvent(
        String orderId,
        String userId,
        String productId,
        double amount
) {}