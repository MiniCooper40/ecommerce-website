package com.ecommerce.e2e.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for E2E tests that provides shared Testcontainers infrastructure.
 * All infrastructure containers are started once and shared across all test classes.
 */
@Slf4j
public abstract class E2ETestBase {

    protected static Network network;
    
    // Infrastructure containers
    protected static PostgreSQLContainer<?> postgresContainer;
    protected static GenericContainer<?> kafkaContainer;
    protected static GenericContainer<?> redisContainer;
    
    // Service containers
    protected static GenericContainer<?> eurekaServerContainer;
    protected static GenericContainer<?> securityServiceContainer;
    protected static GenericContainer<?> catalogServiceContainer;
    protected static GenericContainer<?> cartServiceContainer;
    protected static GenericContainer<?> orderServiceContainer;
    protected static GenericContainer<?> gatewayContainer;

    // Service base URLs
    protected static String GATEWAY_URL;
    protected static String CATALOG_URL;
    protected static String CART_URL;
    protected static String ORDER_URL;
    protected static String SECURITY_URL;

    private static final String DOCKER_IMAGE_PREFIX = System.getProperty("docker.image.prefix", "ecommerce");
    private static final String IMAGE_TAG = System.getProperty("docker.image.tag", "latest");

    @BeforeAll
    public static void setUpInfrastructure() {
        log.info("Starting E2E test infrastructure...");
        
        // Create shared network
        network = Network.newNetwork();
        
        // Start infrastructure containers
        startPostgreSQL();
        startKafka();
        startRedis();
        
        // Start service discovery
        startEurekaServer();
        
        // Wait for Eureka to be ready
        waitForService(eurekaServerContainer, 8761);
        
        // Start microservices
        startSecurityService();
        startCatalogService();
        startCartService();
        startOrderService();
        startGateway();
        
        // Set service URLs
        GATEWAY_URL = "http://localhost:" + gatewayContainer.getMappedPort(8080);
        CATALOG_URL = "http://localhost:" + catalogServiceContainer.getMappedPort(8083);
        CART_URL = "http://localhost:" + cartServiceContainer.getMappedPort(8082);
        ORDER_URL = "http://localhost:" + orderServiceContainer.getMappedPort(8084);
        SECURITY_URL = "http://localhost:" + securityServiceContainer.getMappedPort(8081);
        
        log.info("E2E test infrastructure started successfully");
        log.info("Gateway URL: {}", GATEWAY_URL);
        log.info("Catalog URL: {}", CATALOG_URL);
        log.info("Cart URL: {}", CART_URL);
        log.info("Order URL: {}", ORDER_URL);
        log.info("Security URL: {}", SECURITY_URL);
        
        // Give services additional time to fully initialize and register with Eureka
        log.info("Waiting for services to fully initialize and register with Eureka...");
        try {
            Thread.sleep(30000); // Wait 30 seconds for Eureka registration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Services initialization wait complete");
    }

    @AfterAll
    public static void tearDownInfrastructure() {
        log.info("Stopping E2E test infrastructure...");
        
        // Stop in reverse order
        if (gatewayContainer != null) gatewayContainer.stop();
        if (orderServiceContainer != null) orderServiceContainer.stop();
        if (cartServiceContainer != null) cartServiceContainer.stop();
        if (catalogServiceContainer != null) catalogServiceContainer.stop();
        if (securityServiceContainer != null) securityServiceContainer.stop();
        if (eurekaServerContainer != null) eurekaServerContainer.stop();
        
        if (redisContainer != null) redisContainer.stop();
        if (kafkaContainer != null) kafkaContainer.stop();
        if (postgresContainer != null) postgresContainer.stop();
        
        if (network != null) network.close();
        
        log.info("E2E test infrastructure stopped");
    }

    private static void startPostgreSQL() {
        log.info("Starting PostgreSQL container...");
        postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withNetwork(network)
                .withNetworkAliases("postgres")
                .withDatabaseName("ecommerce")
                .withUsername("postgres")
                .withPassword("password")
                .withReuse(false);
        postgresContainer.start();
        log.info("PostgreSQL started on port: {}", postgresContainer.getMappedPort(5432));
    }

    private static void startKafka() {
        log.info("Starting Kafka container with custom configuration...");
        // Use GenericContainer instead of KafkaContainer to have full control over advertised listeners
        GenericContainer<?> zookeeperContainer = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-zookeeper:7.5.0"))
                .withNetwork(network)
                .withNetworkAliases("zookeeper")
                .withEnv("ZOOKEEPER_CLIENT_PORT", "2181")
                .withEnv("ZOOKEEPER_TICK_TIME", "2000")
                .withExposedPorts(2181)
                .withReuse(false);
        zookeeperContainer.start();
        log.info("Zookeeper started");
        
        kafkaContainer = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
                .withNetwork(network)
                .withNetworkAliases("kafka")
                .withExposedPorts(9093, 9092)
                .withEnv("KAFKA_BROKER_ID", "1")
                .withEnv("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181")
                .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT")
                .withEnv("KAFKA_LISTENERS", "INTERNAL://0.0.0.0:9093,EXTERNAL://0.0.0.0:9092")
                .withEnv("KAFKA_ADVERTISED_LISTENERS", "INTERNAL://kafka:9093,EXTERNAL://localhost:9092")
                .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "INTERNAL")
                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
                .dependsOn(zookeeperContainer)
                .waitingFor(Wait.forLogMessage(".*\\[KafkaServer id=1\\] started.*", 1)
                        .withStartupTimeout(Duration.ofMinutes(2)))
                .withReuse(false);
        kafkaContainer.start();
        log.info("Kafka started on internal: kafka:9093, external: localhost:{}", kafkaContainer.getMappedPort(9092));
    }

    private static void startRedis() {
        log.info("Starting Redis container...");
        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withNetwork(network)
                .withNetworkAliases("redis")
                .withExposedPorts(6379)
                .withReuse(false);
        redisContainer.start();
        log.info("Redis started on port: {}", redisContainer.getMappedPort(6379));
    }

    private static void startEurekaServer() {
        log.info("Starting Eureka Server...");
        eurekaServerContainer = new GenericContainer<>(getDockerImage("eureka-server"))
                .withNetwork(network)
                .withNetworkAliases("eureka-server")
                .withExposedPorts(8761)
                .withEnv(getCommonEnv())
                .waitingFor(Wait.forHttp("/actuator/health")
                        .forPort(8761)
                        .withStartupTimeout(Duration.ofMinutes(2)))
                .withReuse(false);
        eurekaServerContainer.start();
        log.info("Eureka Server started on port: {}", eurekaServerContainer.getMappedPort(8761));
    }

    private static void startSecurityService() {
        log.info("Starting Security Service...");
        Map<String, String> env = getCommonEnv();
        env.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/ecommerce");
        env.put("SPRING_DATASOURCE_USERNAME", "postgres");
        env.put("SPRING_DATASOURCE_PASSWORD", "password");
        env.put("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", "http://eureka-server:8761/eureka/");
        
        securityServiceContainer = new GenericContainer<>(getDockerImage("security-service"))
                .withNetwork(network)
                .withNetworkAliases("security-service")
                .withExposedPorts(8081)
                .withEnv(env)
                .dependsOn(postgresContainer, eurekaServerContainer)
                .waitingFor(Wait.forHttp("/actuator/health")
                        .forPort(8081)
                        .withStartupTimeout(Duration.ofMinutes(3)))
                .withReuse(false);
        securityServiceContainer.start();
        log.info("Security Service started on port: {}", securityServiceContainer.getMappedPort(8081));
    }

    private static void startCatalogService() {
        log.info("Starting Catalog Service...");
        Map<String, String> env = getCommonEnv();
        env.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/ecommerce");
        env.put("SPRING_DATASOURCE_USERNAME", "postgres");
        env.put("SPRING_DATASOURCE_PASSWORD", "password");
        env.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9093");
        env.put("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", "http://eureka-server:8761/eureka/");
        env.put("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI", "http://security-service:8081/.well-known/jwks.json");
        
        catalogServiceContainer = new GenericContainer<>(getDockerImage("catalog-service"))
                .withNetwork(network)
                .withNetworkAliases("catalog-service")
                .withExposedPorts(8083)
                .withEnv(env)
                .dependsOn(postgresContainer, kafkaContainer, eurekaServerContainer, securityServiceContainer)
                .waitingFor(Wait.forLogMessage(".*Started CatalogServiceApplication.*", 1)
                        .withStartupTimeout(Duration.ofMinutes(5)))
                .withReuse(false);
        catalogServiceContainer.start();
        log.info("Catalog Service started on port: {}", catalogServiceContainer.getMappedPort(8083));
    }

    private static void startCartService() {
        log.info("Starting Cart Service...");
        Map<String, String> env = getCommonEnv();
        env.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/ecommerce");
        env.put("SPRING_DATASOURCE_USERNAME", "postgres");
        env.put("SPRING_DATASOURCE_PASSWORD", "password");
        env.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9093");
        env.put("SPRING_DATA_REDIS_HOST", "redis");
        env.put("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", "http://eureka-server:8761/eureka/");
        env.put("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI", "http://security-service:8081/.well-known/jwks.json");
        
        cartServiceContainer = new GenericContainer<>(getDockerImage("cart-service"))
                .withNetwork(network)
                .withNetworkAliases("cart-service")
                .withExposedPorts(8082)
                .withEnv(env)
                .dependsOn(postgresContainer, kafkaContainer, redisContainer, eurekaServerContainer, securityServiceContainer, catalogServiceContainer)
                .waitingFor(Wait.forLogMessage(".*Started CartServiceApplication.*", 1)
                        .withStartupTimeout(Duration.ofMinutes(5)))
                .withReuse(false);
        cartServiceContainer.start();
        log.info("Cart Service started on port: {}", cartServiceContainer.getMappedPort(8082));
    }

    private static void startOrderService() {
        log.info("Starting Order Service...");
        Map<String, String> env = getCommonEnv();
        env.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/ecommerce");
        env.put("SPRING_DATASOURCE_USERNAME", "postgres");
        env.put("SPRING_DATASOURCE_PASSWORD", "password");
        env.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9093");
        env.put("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", "http://eureka-server:8761/eureka/");
        env.put("SERVER_PORT", "8084");
        env.put("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI", "http://security-service:8081/.well-known/jwks.json");
        
        orderServiceContainer = new GenericContainer<>(getDockerImage("order-service"))
                .withNetwork(network)
                .withNetworkAliases("order-service")
                .withExposedPorts(8084)
                .withEnv(env)
                .dependsOn(postgresContainer, kafkaContainer, eurekaServerContainer, securityServiceContainer)
                .waitingFor(Wait.forLogMessage(".*Started OrderServiceApplication.*", 1)
                        .withStartupTimeout(Duration.ofMinutes(5)))
                .withReuse(false);
        orderServiceContainer.start();
        log.info("Order Service started on port: {}", orderServiceContainer.getMappedPort(8084));
    }

    private static void startGateway() {
        log.info("Starting Gateway...");
        Map<String, String> env = getCommonEnv();
        env.put("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", "http://eureka-server:8761/eureka/");
        
        gatewayContainer = new GenericContainer<>(getDockerImage("gateway"))
                .withNetwork(network)
                .withNetworkAliases("gateway")
                .withExposedPorts(8080)
                .withEnv(env)
                .dependsOn(eurekaServerContainer)
                .waitingFor(Wait.forHttp("/actuator/health")
                        .forPort(8080)
                        .withStartupTimeout(Duration.ofMinutes(3)))
                .withReuse(false);
        gatewayContainer.start();
        log.info("Gateway started on port: {}", gatewayContainer.getMappedPort(8080));
    }

    private static String getDockerImage(String serviceName) {
        return DOCKER_IMAGE_PREFIX + "/" + serviceName + ":" + IMAGE_TAG;
    }

    private static Map<String, String> getCommonEnv() {
        Map<String, String> env = new HashMap<>();
        env.put("SPRING_PROFILES_ACTIVE", "docker");
        return env;
    }

    private static void waitForService(GenericContainer<?> container, int port) {
        try {
            Thread.sleep(5000); // Give service time to register with Eureka
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // These properties are available to Spring tests if needed
        registry.add("test.postgres.url", () -> postgresContainer.getJdbcUrl());
        registry.add("test.kafka.bootstrap-servers", () -> "localhost:" + kafkaContainer.getMappedPort(9092));
        registry.add("test.redis.host", () -> redisContainer.getHost());
        registry.add("test.redis.port", () -> redisContainer.getMappedPort(6379));
    }
}
