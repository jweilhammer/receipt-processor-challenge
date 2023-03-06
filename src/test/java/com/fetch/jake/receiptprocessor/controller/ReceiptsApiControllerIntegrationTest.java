package com.fetch.jake.receiptprocessor.controller;

import com.fetch.jake.receiptprocessor.domain.GetReceiptPointsResponse;
import com.fetch.jake.receiptprocessor.domain.ProcessReceiptResponse;
import org.json.JSONException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReceiptsApiControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient; // available with Spring WebFlux

    static String receiptId;

    @Container
    static final GenericContainer dynamo =
            new GenericContainer(DockerImageName.parse("amazon/dynamodb-local"))
                    .withExposedPorts(8000);

    @BeforeAll
    public static void checkDbContainer() {
        assertTrue(dynamo.isRunning());
        String endpoint = "http://" + dynamo.getHost() + ":" + dynamo.getFirstMappedPort();
        System.out.println("Dynamo DB container reachable at: " + endpoint);
        System.setProperty("aws.dynamodb.endpoint", endpoint);
        System.setProperty("integration.tests", "enabled");
    }


    @Test
    @Order(1)
    void process() throws JSONException {
        String requestJson = "{" +
                "    'retailer': 'M&M Corner Market'," +
                "    'purchaseDate': '2022-03-20'," +
                "    'purchaseTime': '14:33'," +
                "    'items': [" +
                "        {'shortDescription': 'Gatorade', 'price': '2.25'}," +
                "        {'shortDescription': 'Gatorade', 'price': '2.25'}," +
                "        {'shortDescription': 'Gatorade', 'price': '2.25'}," +
                "        {'shortDescription': 'Gatorade', 'price': '2.25'}" +
                "    ]," +
                "'total': '9.00' }";

        // Need actual quotes for json values
        String body = requestJson.replaceAll("'", "\"");

        ProcessReceiptResponse response = this.webTestClient
                .post()
                .uri("/receipts/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProcessReceiptResponse.class)
                .returnResult().getResponseBody();

        // Make sure response is valid UUID
        UUID uuid = UUID.fromString(response.getId());
        assertFalse(response.getId().isEmpty());
        assertEquals(response.getId(), uuid.toString());

        receiptId = response.getId();
        System.out.println("RECEIVED ID:" + receiptId);
    }

    @Test
    @Order(2)
    public void retrieve() {
        GetReceiptPointsResponse response = this.webTestClient
                .get()
                .uri("/receipts/" + receiptId + "/points")
                .exchange()
                .expectStatus().isOk()
                .expectBody(GetReceiptPointsResponse.class)
                .returnResult().getResponseBody();

        assertEquals(109, response.getPoints());
    }

    @Test
    @Order(3)
    public void retrieveNotFound() {
        webTestClient
                .get()
                .uri("/receipts/this_id_is_not_found/points")
                .exchange()
                .expectStatus().isNotFound();
    }
}