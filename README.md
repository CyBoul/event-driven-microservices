# Event-Driven Microservices Platform

A cloud-native, event-driven microservices platform built with **Spring Boot 3.5** and **Java 21**. Demonstrates real-world architectural patterns: API Gateway routing, database-per-service isolation, reactive I/O, and asynchronous inter-service communication via Apache Kafka.

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen?logo=springboot)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-3.9-black?logo=apachekafka)
![MongoDB](https://img.shields.io/badge/MongoDB-7-green?logo=mongodb)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)

---

## Architecture

```
                      User
                       │
                ┌──────▼────────┐
                │   Frontend    │
                └──────┬────────┘
                ┌──────▼────────┐
                │  API Gateway  │  :8080
                └──────┬────────┘
     ┌─────────────────┼─────────────────┐
┌────▼────┐     ┌──────▼─────┐    ┌──────▼─────┐
│ User MS │     │  Order MS  │    │ Product MS │
│  :8081  │     │   :8083    │    │   :8082    │
└─────────┘     └──────┬─────┘    └────────────┘
                       │
                  Kafka Events
                  (order-created)
                       │
                ┌──────▼──────┐
                │ Payment MS  │
                │   :8084     │
                └─────────────┘
```

---

## Modules

| Module | Role | Stack |
|---|---|---|
| `api-gateway` | Entry point, path-based routing | Spring Cloud Gateway |
| `user-service` | User management | Spring Web + MongoDB |
| `product-service` | Product catalog | Spring WebFlux + MongoDB (reactive) |
| `order-service` | Order processing, event producer | Spring Boot + Kafka |
| `payment-service` | Payment handling, event consumer | Spring Boot + Kafka |
| `common-lib` | Shared DTOs and Kafka event records | Java 21 records |

---

## Running the Project

```bash
docker compose up --build
```

MongoDB auto-initializes on first startup — collections, indexes, and sample data are seeded automatically.

---

## Documentation

- [Architecture & C4 diagrams](docs/c4.md)
- [Design decisions](docs/design-decisions.md)
- [Tech stack](docs/tech-stack.md)
- [Local development](docs/local-dev.md)
- [Kafka operations](docs/kafka-ops.md)
