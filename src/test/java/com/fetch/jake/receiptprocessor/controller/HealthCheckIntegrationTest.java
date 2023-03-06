package com.fetch.jake.receiptprocessor.controller;

import com.fetch.jake.receiptprocessor.model.Receipt;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HealthCheckIntegrationTest {

    @SpyBean
    @Autowired
    DynamoDbTable<Receipt> dynamoDbTable;

    @Autowired
    private WebTestClient webTestClient; // available with Spring WebFlux

    @Container
    static final GenericContainer dynamo =
            new GenericContainer(DockerImageName.parse("amazon/dynamodb-local"))
                    .withExposedPorts(8000);

    @BeforeAll
    public static void checkDbContainer() {
        assertTrue(dynamo.isRunning());
        String endpoint = "http://" + dynamo.getHost() + ":" + dynamo.getFirstMappedPort();
        System.out.println("Dynamo DB container reachable at: " + endpoint);
        System.out.println(System.getProperty("integration.tests"));
        System.setProperty("aws.dynamodb.endpoint", endpoint);
        System.setProperty("integration.tests", "enabled");
    }

    @Test
    @Order(1)
    void healthStatusUp() {
        webTestClient
                .get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().json("{ 'status': 'UP' }");
    }

    @Test
    @Order(2)
    public void healthStatusDown() throws InterruptedException {
        // Bring down database to cause down status
        dynamo.stop();
        Thread.sleep(1000);

        // Set longer request timeout when database is down
        webTestClient = webTestClient.mutate().responseTimeout(Duration.ofSeconds(30)).build();
        webTestClient
                .get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody().json("{ 'status': 'DOWN' }");
    }
}