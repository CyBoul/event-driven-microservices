# Architecture Overview

Request flow: **Client → Frontend → API Gateway → Services → MongoDB / Kafka**

```
                      User
                       │
                ┌──────▼────────┐
                │   Frontend    │◄─── SSE ───────────────────┐
                └──────┬────────┘                            │
                ┌──────▼────────┐                     ┌──────┴──────┐
                │  API Gateway  │ :8080               │   Notif MS  │ :8085
                └──────┬────────┘                     └──────▲──────┘
     ┌─────────────────┼──────────────────┐                  │
┌────▼────┐     ┌──────▼─────┐    ┌───────▼────┐      (payment-proceed)
│ User MS │     │  Order MS  │    │ Product MS │             │
│  :8081  │     │   :8083    │    │   :8082    │      ┌──────┴──────┐
└─────────┘     └──────┬─────┘    └────────────┘      │ Payment MS  │
                       │                              │   :8084     │
                  (order-created)                     └─────────────┘
                       │                                     ▲
                       └──────────── Kafka ──────────────────┘
```

For routing and design rationale see [design-decisions.md](design-decisions.md).

---

# Diagrams

## Level 2 — Container Diagram

> User Service and Product Service omitted — standard synchronous REST + MongoDB pattern, not specific to the event-driven architecture.

```mermaid
C4Container
    Person(user, "User", "End user of the platform")
    System_Boundary(b, "Event-Driven Microservices Platform") {
        Container(apiGateway, "API Gateway", "Spring Cloud Gateway", "Routes external requests")
        Container(orderService, "Order Service", "Spring Boot + Kafka", "Order orchestration, event producer")
        Container(kafka, "Apache Kafka", "Event Streaming", "Async communication")
        Container(notifService, "Notification Service", "Spring WebFlux + Kafka", "Real-time notifications via SSE")
        ContainerDb(mongo, "MongoDB", "NoSQL Database", "Per-service databases")
        Container(paymentService, "Payment Service", "Spring Boot + Kafka", "Payment processing, event consumer/producer") 
    }
    Rel(user, apiGateway, "HTTP")
    Rel(apiGateway, orderService, "/api/orders")
    Rel(orderService, mongo, "orderdb")
    Rel(orderService, kafka, "OrderCreatedEvent")
    Rel(kafka, paymentService, "OrderCreatedEvent")
    Rel(paymentService, kafka, "PaymentProceedEvent")
    Rel(kafka, notifService, "PaymentProceedEvent")
    Rel(notifService, user, "SSE /notifications/stream")
```

## Level 3 — Component Diagram (Notification Service)

```mermaid
C4Component
    Container(kafka, "Apache Kafka", "Event Streaming", "Message broker")
    Person(user, "User", "End user of the platform")
    Container_Boundary(b, "Notification Service") {
        Component(consumer, "PaymentProceedEventConsumer", "Spring Kafka", "Consumes PaymentProceedEvent, routes by status")
        Component(sink, "NotificationSinkConfig", "Reactor Sinks.Many", "Reactive bridge between Kafka thread and SSE stream")
        Component(controller, "NotificationController", "Spring WebFlux", "Exposes SSE stream to frontend")
    }
    Rel(kafka, consumer, "PaymentProceedEvent")
    Rel(consumer, sink, "tryEmitNext(NotificationEvent)")
    Rel(sink, controller, "asFlux()")
    Rel(controller, user, "SSE /notifications/stream")
```

