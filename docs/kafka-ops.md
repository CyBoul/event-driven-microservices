# Kafka Operations

## Consume messages on the order-created topic

```bash
docker exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic order-created \
  --from-beginning
```

## Inspect the payment-service consumer group

```bash
docker exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server kafka:9092 \
  --group payment-service \
  --describe
```

## Topic setup

The `kafka-init` container creates the `order-created` topic automatically on stack startup (1 partition, replication factor 1). No manual setup required.
