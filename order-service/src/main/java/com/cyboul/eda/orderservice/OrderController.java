package com.cyboul.eda.orderservice;

import com.cyboul.eda.common.events.OrderCreatedEvent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody OrderCreation order) {
        OrderCreatedEvent event = new OrderCreatedEvent(order.uuid(), order.userId(), order.productId(), order.amount());
        kafkaTemplate.send("order-created", event);
        return ResponseEntity.ok("Order created");
    }

    @GetMapping("/eZ")
    public ResponseEntity<String> easyCreation() {
        return create(
                new OrderCreation(
                    UUID.randomUUID().toString(),
                    "1","1",1.00
                ));
    }

    public record OrderCreation(
            @NotBlank String uuid,
            @NotBlank String userId,
            @NotBlank String productId,
            @Positive double amount
    ) {}
}