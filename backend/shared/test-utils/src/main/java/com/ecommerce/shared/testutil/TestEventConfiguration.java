package com.ecommerce.shared.testutil;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.ecommerce.shared.events.EventPublisher;

/**
 * Test configuration that provides a mock EventPublisher for testing.
 * This allows tests to run without requiring Kafka to be running.
 */
@TestConfiguration
@Profile("test")
public class TestEventConfiguration {

    @Bean
    @Primary
    public EventPublisher eventPublisher() {
        return Mockito.mock(EventPublisher.class);
    }
}