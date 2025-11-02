package com.ecommerce.shared.events.util;

import org.slf4j.MDC;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.util.UUID;

/**
 * Utility class for managing event correlation and tracing.
 */
public class EventCorrelationUtils {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CAUSATION_ID_HEADER = "X-Causation-ID";
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_EVENT_TYPE = "eventType";

    /**
     * Gets the correlation ID from the current MDC context, creating one if it doesn't exist.
     */
    public static String getOrCreateCorrelationId() {
        String correlationId = MDC.get(MDC_CORRELATION_ID);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
            MDC.put(MDC_CORRELATION_ID, correlationId);
        }
        return correlationId;
    }

    /**
     * Sets up MDC context from a Kafka message.
     */
    public static void setupMDCFromMessage(Message<?> message) {
        // Extract correlation ID from headers
        String correlationId = getHeaderValue(message, CORRELATION_ID_HEADER);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_CORRELATION_ID, correlationId);

        // Extract event type if available
        String topic = getHeaderValue(message, KafkaHeaders.RECEIVED_TOPIC);
        if (topic != null) {
            MDC.put(MDC_EVENT_TYPE, topic);
        }
    }

    /**
     * Clears MDC context.
     */
    public static void clearMDC() {
        MDC.clear();
    }

    /**
     * Extracts a header value from a Kafka message.
     */
    @SuppressWarnings("unchecked")
    private static String getHeaderValue(Message<?> message, String headerName) {
        Object header = message.getHeaders().get(headerName);
        if (header instanceof byte[]) {
            return new String((byte[]) header);
        } else if (header instanceof String) {
            return (String) header;
        }
        return null;
    }

    /**
     * Creates a new causation ID for event chains.
     */
    public static String createCausationId() {
        return UUID.randomUUID().toString();
    }
}