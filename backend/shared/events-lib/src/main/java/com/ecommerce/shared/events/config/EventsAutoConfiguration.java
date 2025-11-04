package com.ecommerce.shared.events.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.ecommerce.shared.events.EventPublisher;
import com.ecommerce.shared.events.impl.KafkaEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

/**
 * Auto-configuration for Kafka events.
 */
@Configuration
@EnableKafka
@EnableConfigurationProperties(EventsProperties.class)
@RequiredArgsConstructor
public class EventsAutoConfiguration {

    private final EventsProperties eventsProperties;

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper eventsObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public ProducerFactory<String, Object> kafkaProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, eventsProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, eventsProperties.getProducer().getKeySerializer());
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, eventsProperties.getProducer().getValueSerializer());
        configProps.put(ProducerConfig.RETRIES_CONFIG, eventsProperties.getProducer().getRetries());
        configProps.put(ProducerConfig.ACKS_CONFIG, eventsProperties.getProducer().getAcks());
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, eventsProperties.getProducer().getBatchSize());
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, eventsProperties.getProducer().getLingerMs());
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, eventsProperties.getProducer().getBufferMemory());
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, eventsProperties.getProducer().isEnableIdempotence());

        // Configure JSON serializer
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsumerFactory<String, Object> kafkaConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, eventsProperties.getBootstrapServers());
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, eventsProperties.getConsumer().getKeyDeserializer());
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, eventsProperties.getConsumer().getValueDeserializer());
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, eventsProperties.getConsumer().getAutoOffsetReset());
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, eventsProperties.getConsumer().isEnableAutoCommit());
        configProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, eventsProperties.getConsumer().getIsolationLevel());
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, eventsProperties.getConsumer().getMaxPollRecords());
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, eventsProperties.getConsumer().getSessionTimeoutMs());
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, eventsProperties.getConsumer().getHeartbeatIntervalMs());

        // Configure JSON deserializer
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.ecommerce.*");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
        configProps.put(JsonDeserializer.TYPE_MAPPINGS, 
            "CartItemAddedEvent:com.ecommerce.shared.events.domain.CartItemAddedEvent," +
            "CartItemUpdatedEvent:com.ecommerce.shared.events.domain.CartItemUpdatedEvent," +
            "CartItemRemovedEvent:com.ecommerce.shared.events.domain.CartItemRemovedEvent," +
            "ProductUpdatedEvent:com.ecommerce.shared.events.domain.ProductUpdatedEvent," +
            "OrderCreatedEvent:com.ecommerce.shared.events.domain.OrderCreatedEvent," +
            "CartValidationRequestedEvent:com.ecommerce.shared.events.domain.CartValidationRequestedEvent," +
            "CartValidationCompletedEvent:com.ecommerce.shared.events.domain.CartValidationCompletedEvent");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public EventPublisher eventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate, eventsProperties);
    }
}