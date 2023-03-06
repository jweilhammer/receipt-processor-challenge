package com.fetch.jake.receiptprocessor.service;

import com.fetch.jake.receiptprocessor.domain.ProcessReceiptRequest;
import com.fetch.jake.receiptprocessor.domain.ReceiptRequestItem;
import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.repository.ReceiptRepository;
import com.fetch.jake.receiptprocessor.service.pointrules.ReceiptPointRuleOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
public class ReceiptServiceIntegrationTest {

    @SpyBean
    @Autowired
    ReceiptService receiptService;

    @SpyBean
    @Autowired
    ReceiptRepository receiptRepository;

    @Autowired
    ReceiptPointRuleOptions defaultPointRuleOptions;


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

    @Nested
    class SaveAndRetrieve {
        static String testUuid;
        static ProcessReceiptRequest testRequest;

        @BeforeAll
        public static void setup() {
            testRequest = ProcessReceiptRequest.builder()
                    .retailer("M&M Corner Market")
                    .purchaseDate(LocalDate.of(2022, 03, 20))
                    .purchaseTime(LocalTime.of(14, 33))
                    .items(
                            List.of(
                                    new ReceiptRequestItem("Gatorade", new BigDecimal("2.25")),
                                    new ReceiptRequestItem("Gatorade", new BigDecimal("2.25")),
                                    new ReceiptRequestItem("Gatorade", new BigDecimal("2.25")),
                                    new ReceiptRequestItem("Gatorade", new BigDecimal("2.25"))
                            )
                    )
                    .total(new BigDecimal("9.00"))
                    .build();
        }

        @Test
        public void processReceiptShouldSaveReceipt() {
            testUuid = receiptService.processReceipt(testRequest);
            System.out.println("TEST UUID: " + testUuid);
            verify(receiptService, times(1)).convertReceiptRequestToEntity(testRequest);
            verify(receiptService, times(1)).applyPoints(any(Receipt.class), same(defaultPointRuleOptions));
            verify(receiptRepository, times(1)).saveReceipt(any(Receipt.class));
        }

        @Test
        public void getReceiptReturnsReceipt() {
            // When
            Receipt retrievedReceipt = receiptService.getReceipt(testUuid);

            // Then
            verify(receiptRepository, times(1)).getReceipt(same(testUuid));
            assertEquals(retrievedReceipt.getPoints(), 109);
            assertEquals(retrievedReceipt.getId(), testUuid);
            assertEquals(retrievedReceipt.getRetailer(), testRequest.getRetailer());
            assertEquals(retrievedReceipt.getTotal().setScale(2), testRequest.getTotal());
            assertEquals(retrievedReceipt.getPurchaseDateTime().toLocalDate(), testRequest.getPurchaseDate());
            assertEquals(retrievedReceipt.getPurchaseDateTime().toLocalTime().truncatedTo(ChronoUnit.MINUTES), testRequest.getPurchaseTime());


            // Verify array of items was saved correctly.  TODO: any better way to do this?
            HashMap<String, BigDecimal> requestItemsMap = new HashMap<>();
            testRequest.getItems().forEach(item -> requestItemsMap.put(item.getShortDescription(), item.getPrice()));
            retrievedReceipt.getItems().forEach(savedItem -> {
                String savedDescription = savedItem.getShortDescription();
                assertTrue(requestItemsMap.containsKey(savedDescription));
                assertEquals(savedItem.getPrice(), requestItemsMap.get(savedDescription));
            });

            assertTrue(testRequest.getItems().size() >= 1);
            assertEquals(testRequest.getItems().size(), retrievedReceipt.getItems().size());
        }
    }

    @Test
    public void getReceiptShouldReturnNullWhenNotFound() {
        // When
        String notFoundUuid = "this_id_is_not_found";
        Receipt retrievedReceipt = receiptService.getReceipt(notFoundUuid);

        assertEquals(retrievedReceipt, null);
        verify(receiptRepository, times(1)).getReceipt(notFoundUuid);
    }
}