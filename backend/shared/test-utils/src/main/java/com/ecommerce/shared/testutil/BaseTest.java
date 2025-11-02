package com.ecommerce.shared.testutil;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base test class that provides common test configuration.
 * All test classes should extend this to get the proper test setup.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestEventConfiguration.class)
public abstract class BaseTest {
    // Common test setup can be added here if needed
}