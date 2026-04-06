# Architecture Overview

Request flow: **Client → Frontend → API Gateway → Services → MongoDB / Kafka**

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

For C4 diagrams (Context, Container, Component) see [c4.md](c4.md). 
For routing and design rationale see [design-decisions.md](design-decisions.md).
