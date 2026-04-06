# Design Decisions

## Hybrid I/O model

`user-service` uses blocking Spring Web; `product-service` uses reactive Spring WebFlux with a reactive MongoDB driver. New services default to the reactive model for better throughput under high load.

WebFlux uses a non-blocking, event-loop model — threads are not held waiting for I/O. This makes it well-suited for high-volume, read-heavy scenarios like product catalog queries where many concurrent requests are expected.

## Database per service

Each service owns its MongoDB database (`userdb`, `productdb`, `orderdb`). Services never query each other's databases — cross-service data is exchanged through REST APIs or Kafka events.

In development, all databases share a single MongoDB instance for simplicity. In production, each service should have its own instance to eliminate data access bottlenecks — with dedicated write instances and read replicas as services scale horizontally.

## Asynchronous event flow

`order-service` publishes an `OrderCreatedEvent` to the `order-created` Kafka topic after persisting an order. `payment-service` consumes this event independently, enabling loose coupling and resilience between the two services.

## Shared contracts via `common-lib`

DTOs (`UserDTO`, `ProductDTO`) and Kafka event records (`OrderCreatedEvent`) are defined as Java records in `common-lib` and shared across services — keeping the API contract explicit without duplicating code.

## API Gateway as single entry point

All external traffic enters through the gateway on port `8080`. Services are not directly exposed in production. Routes are path-prefix based:

| Path | Target |
|---|---|
| `/api/users/**` | user-service :8081 |
| `/api/products/**` | product-service :8082 |
| `/api/orders/**` | order-service :8083 |
