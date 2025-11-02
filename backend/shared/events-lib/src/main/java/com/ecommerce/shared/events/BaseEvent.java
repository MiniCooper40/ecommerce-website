package com.ecommerce.shared.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the ecommerce system.
 * Provides common fields and behavior for event tracking and correlation.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseEvent {

    @JsonProperty("eventId")
    @NotBlank
    @EqualsAndHashCode.Include
    private String eventId;

    @JsonProperty("eventType")
    @NotBlank
    private String eventType;

    @JsonProperty("aggregateId")
    @NotBlank
    private String aggregateId;

    @JsonProperty("aggregateType")
    @NotBlank
    private String aggregateType;

    @JsonProperty("timestamp")
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("causationId")
    private String causationId;

    @JsonProperty("source")
    private String source;

    protected BaseEvent(String aggregateId, String aggregateType, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.version = 1;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.source = source;
        this.eventType = this.getClass().getSimpleName();
    }

    protected BaseEvent(String aggregateId, String aggregateType, String source, String correlationId) {
        this(aggregateId, aggregateType, source);
        this.correlationId = correlationId;
    }
}