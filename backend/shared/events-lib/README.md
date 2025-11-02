# Events Library

This shared library provides a complete Kafka-based event-driven architecture for the ecommerce microservices platform. It enables asynchronous communication between services using domain events, supporting patterns like sagas and event sourcing.

## Features

- **Domain Events**: Pre-defined events for common ecommerce scenarios
- **Event Publisher**: Simple interface for publishing events to Kafka
- **Auto-Configuration**: Spring Boot auto-configuration for easy setup
- **Correlation Tracking**: Built-in correlation ID support for distributed tracing
- **Type-Safe Serialization**: JSON serialization with proper type handling
- **Error Handling**: Built-in retry and error handling mechanisms

## Quick Start

### 1. Add Dependency

Add the events library to your service's `pom.xml`:

```xml
<dependency>
    <groupId>com.ecommerce</groupId>
    <artifactId>events-lib</artifactId>
</dependency>
```

### 2. Configure Kafka

Add Kafka configuration to your `application.yml`:

```yaml
ecommerce:
  events:
    bootstrap-servers: localhost:9092
    topics:
      order-events: order-events
      cart-events: cart-events
      product-events: product-events
```

### 3. Publishing Events

Inject the `EventPublisher` and publish events using the Builder pattern:

````java
@Service
public class OrderService {

    private final EventPublisher eventPublisher;

    public OrderService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void createOrder(CreateOrderRequest request) {
        // Create order logic...
        Order order = orderRepository.save(newOrder);

        // Publish event using Builder pattern
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(order.getId())
            .userId(order.getUserId())
            .totalAmount(order.getTotalAmount())
            .currency(order.getCurrency())
            .items(order.getItems())
            .source("order-service")
            .correlationId(EventCorrelationUtils.getOrCreateCorrelationId())
            .build();

        eventPublisher.publish(event);
    }
}
```### 4. Consuming Events

Use `@KafkaListener` to handle events:

```java
@Component
public class CartValidationHandler {

    @KafkaListener(topics = "order-events", groupId = "cart-service")
    public void handleOrderCreated(OrderCreatedEvent event, Acknowledgment ack) {
        try {
            // Validate cart logic...
            CartValidationCompletedEvent response = CartValidationCompletedEvent.builder()
                .cartId(event.getAggregateId())
                .userId(event.getUserId())
                .isValid(true)
                .validationErrors(Collections.emptyList())
                .requestingService("cart-service")
                .source("cart-service")
                .correlationId(event.getCorrelationId())
                .build();

            eventPublisher.publish(response);
            ack.acknowledge();
        } catch (Exception e) {
            // Handle error
            log.error("Failed to validate cart for order {}", event.getAggregateId(), e);
        }
    }
}
````

## Available Events

### Order Events

- `OrderCreatedEvent`: Published when a new order is created
- `OrderUpdatedEvent`: Published when order details change
- `OrderCancelledEvent`: Published when an order is cancelled

### Product Events

- `ProductUpdatedEvent`: Published when product information changes
- `ProductValidationRequestedEvent`: Request validation of products
- `ProductValidationCompletedEvent`: Response to product validation

### Cart Events

- `CartValidationRequestedEvent`: Request validation of cart contents
- `CartValidationCompletedEvent`: Response to cart validation

## Configuration Properties

```yaml
ecommerce:
  events:
    bootstrap-servers: localhost:9092
    topics:
      order-events: order-events
      cart-events: cart-events
      product-events: product-events
      user-events: user-events
      payment-events: payment-events
    producer:
      retries: 3
      acks: all
      batch-size: 16384
      linger-ms: 5
      enable-idempotence: true
    consumer:
      auto-offset-reset: earliest
      enable-auto-commit: false
      isolation-level: read_committed
      max-poll-records: 500
```

## Saga Pattern Example

Here's how to implement a distributed transaction using the saga pattern:

### Order Creation Saga

1. **Order Service** creates order and publishes `OrderCreatedEvent`
2. **Cart Service** validates cart and publishes `CartValidationCompletedEvent`
3. **Catalog Service** validates products and publishes `ProductValidationCompletedEvent`
4. **Order Service** processes validation results and completes/cancels order

```java
@Component
public class OrderSagaOrchestrator {

    @KafkaListener(topics = "cart-events", groupId = "order-service-saga")
    public void handleCartValidation(CartValidationCompletedEvent event, Acknowledgment ack) {
        if (!event.getIsValid()) {
            // Cancel order
            cancelOrder(event.getAggregateId(), "Cart validation failed");
        } else {
            // Proceed to next step
            requestProductValidation(event.getAggregateId(), event.getCorrelationId());
        }
        ack.acknowledge();
    }

    @KafkaListener(topics = "product-events", groupId = "order-service-saga")
    public void handleProductValidation(ProductValidationCompletedEvent event, Acknowledgment ack) {
        if (!event.getIsValid()) {
            // Cancel order
            cancelOrder(event.getAggregateId(), "Product validation failed");
        } else {
            // Complete order
            completeOrder(event.getAggregateId());
        }
        ack.acknowledge();
    }
}
```

## Error Handling

The library includes built-in error handling and retry mechanisms:

```java
@Component
public class EventErrorHandler {

    @EventListener
    public void handleKafkaError(ListenerExecutionFailedException ex) {
        log.error("Kafka listener failed", ex);
        // Implement custom error handling logic
        // e.g., dead letter queue, alerting, etc.
    }
}
```

## Testing

The library includes test utilities for integration testing:

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "ecommerce.events.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = {"test-events"})
class EventIntegrationTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Test
    void shouldPublishAndConsumeEvent() {
        // Test implementation
    }
}
```

## Best Practices

1. **Use Correlation IDs**: Always set correlation IDs for event tracing
2. **Idempotent Consumers**: Make event handlers idempotent
3. **Schema Evolution**: Design events for backward compatibility
4. **Error Handling**: Implement proper error handling and dead letter queues
5. **Monitoring**: Monitor event processing metrics and lag
6. **Partitioning**: Use meaningful partition keys for ordering guarantees

## Monitoring

The library exposes metrics for monitoring:

- Event publish/consume rates
- Processing latencies
- Error rates
- Kafka lag

Configure your monitoring system to track these metrics for operational visibility.
