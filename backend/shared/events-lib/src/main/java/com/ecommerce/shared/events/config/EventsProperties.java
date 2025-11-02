package com.ecommerce.shared.events.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Kafka events.
 */
@Data
@ConfigurationProperties(prefix = "ecommerce.events")
public class EventsProperties {

    private String bootstrapServers = "localhost:9092";
    private Producer producer = new Producer();
    private Consumer consumer = new Consumer();
    private Topics topics = new Topics();

    @Data
    public static class Producer {
        private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";
        private String valueSerializer = "org.springframework.kafka.support.serializer.JsonSerializer";
        private int retries = 3;
        private String acks = "all";
        private int batchSize = 16384;
        private int lingerMs = 5;
        private long bufferMemory = 33554432L;
        private boolean enableIdempotence = true;
    }

    @Data
    public static class Consumer {
        private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
        private String valueDeserializer = "org.springframework.kafka.support.serializer.JsonDeserializer";
        private String autoOffsetReset = "earliest";
        private boolean enableAutoCommit = false;
        private String isolationLevel = "read_committed";
        private int maxPollRecords = 500;
        private int sessionTimeoutMs = 30000;
        private int heartbeatIntervalMs = 3000;
    }

    @Data
    public static class Topics {
        private String orderEvents = "order-events";
        private String cartEvents = "cart-events";
        private String productEvents = "product-events";
        private String userEvents = "user-events";
        private String paymentEvents = "payment-events";
    }
}