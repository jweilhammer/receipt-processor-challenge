package com.fetch.jake.receiptprocessor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fetch.jake.receiptprocessor.domain.ProcessReceiptRequest;
import com.fetch.jake.receiptprocessor.domain.ReceiptRequestItem;
import com.fetch.jake.receiptprocessor.model.Receipt;
import com.fetch.jake.receiptprocessor.service.ReceiptService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceiptsApiController.class)
public class ReceiptsApiControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceiptService receiptService;

    private JSONObject processReceiptRequestJson;

    @Test
    public void processReturnsId() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        ProcessReceiptRequest request = ProcessReceiptRequest.builder()
                .retailer("retailer")
                .purchaseDate(LocalDate.now())
                .purchaseTime(LocalTime.now())
                .total(new BigDecimal("1.12"))
                .items(List.of(new ReceiptRequestItem("description", new BigDecimal("1.23"))))
                .build();

        String json = objectMapper.writeValueAsString(request);
        System.out.println(json);

        String mockReceiptId = UUID.randomUUID().toString();
        when(receiptService.processReceipt(any(ProcessReceiptRequest.class))).thenReturn(mockReceiptId);

        mockMvc.perform(post("/receipts/process").contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("id")))
                .andExpect(content().json("{ id: " + mockReceiptId + "}"));

        verify(receiptService, times(1)).processReceipt(any(ProcessReceiptRequest.class));
    }

    @Test
    public void getReceiptPointsReturnPoints() throws Exception {
        int expectedPoints = 123;
        String mockUuid = UUID.randomUUID().toString();
        Receipt mockReceipt = new Receipt();
        mockReceipt.setPoints(expectedPoints);

        when(receiptService.getReceipt(mockUuid)).thenReturn(mockReceipt);
        mockMvc.perform(get("/receipts/" + mockUuid + "/points"))
                .andExpect(status().isOk())
                .andExpect(content().json("{ points: " + expectedPoints + "}"));

        verify(receiptService, times(1)).getReceipt(mockUuid);
    }


    @Test
    public void getReceiptPointsReturnsNotFound() throws Exception {
        String notFoundUuid = "this_id_is_not_found";

        when(receiptService.getReceipt(notFoundUuid)).thenReturn(null);
        mockMvc.perform(get("/receipts/" + notFoundUuid + "/points"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());

        verify(receiptService, times(1)).getReceipt(notFoundUuid);
    }

    @Test
    public void processReturns4xxOnMissingRequestBody() throws Exception {
        mockMvc.perform(post("/receipts/process"))
                .andExpect(status().isBadRequest());
    }

    @BeforeEach
    public void setup() throws JSONException {

        String requestJson = "{" +
                "    'retailer': 'Walgreens'," +
                "    'purchaseDate': '2023-01-03'," +
                "    'purchaseTime': '16:04'," +
                "    'total': '12.34'," +
                "    'items': [" +
                "        {'shortDescription': 'Pepsi - 12-oz', 'price': '1.25'}," +
                "        {'shortDescription': 'Dasani', 'price': '5.1'}," +
                "    ]" +
                "}";
        processReceiptRequestJson = new JSONObject(requestJson);
    }

    @ParameterizedTest
    @MethodSource(value = "invalidRequestFields")
    public void processReturns4xxOnInvalidRequestFields(String field, String value) throws Exception {
        processReceiptRequestJson.put(field, value);
        System.out.println(processReceiptRequestJson);
        mockMvc.perform(post("/receipts/process").contentType(MediaType.APPLICATION_JSON)
                        .content(processReceiptRequestJson.toString()))
                .andExpect(status().isBadRequest());
    }

    public static List<Arguments> invalidRequestFields() throws JSONException {
        return List.of(
                Arguments.of("retailer", ""),
                Arguments.of("retailer", "           "),

                // Bad date text
                Arguments.of("purchaseDate", ""),
                Arguments.of("purchaseDate", "      "),
                Arguments.of("purchaseDate", "2023--01-03"),
                Arguments.of("purchaseDate", "2023-01--03"),
                Arguments.of("purchaseDate", "20232-01-03"),
                Arguments.of("purchaseDate", "2023-011-03"),
                Arguments.of("purchaseDate", "2023-01-033"),
                Arguments.of("purchaseDate", "2023-01-03 badText"),
                Arguments.of("purchaseDate", "badText 2023-01-03"),
                Arguments.of("purchaseDate", "2023-badtext01-03"),
                Arguments.of("purchaseDate", "cool_date_here"),

                // Bad formats, only accept yyyy:mm:dd (definitely more that could be tested)
                Arguments.of("purchaseDate", ""),
                Arguments.of("purchaseDate", "    "),
                Arguments.of("purchaseDate", "01-03-23"),
                Arguments.of("purchaseDate", "30-01-2023"),
                Arguments.of("purchaseDate", "30-01-23"),
                Arguments.of("purchaseDate", "01/03/2023"),
                Arguments.of("purchaseDate", "01/03/23"),
                Arguments.of("purchaseDate", "30/01/2023"),
                Arguments.of("purchaseDate", "30/01/23"),

                // Invalid times
                Arguments.of("purchaseTime", ""),
                Arguments.of("purchaseTime", "    "),
                Arguments.of("purchaseTime", "-01:12"),
                Arguments.of("purchaseTime", "24:00"),
                Arguments.of("purchaseTime", "00:000"),
                Arguments.of("purchaseTime", "000:00"),
                Arguments.of("purchaseTime", "111:00"),
                Arguments.of("purchaseTime", "badText"),

                Arguments.of("total", "-1.00"),
                Arguments.of("total", "2147483648.00"),
                Arguments.of("total", "1.234"),
                Arguments.of("total", "1.001"),
                Arguments.of("total", "1.23 badText"),
                Arguments.of("total", "badText 1.23"),
                Arguments.of("total", "badText")
        );
    }

    @ParameterizedTest
    @MethodSource(value = "invalidRequestItems")
    public void processReturns4xxOnInvalidRequestItems(String field) throws Exception {
        processReceiptRequestJson.remove(field);
        System.out.println(processReceiptRequestJson);
        mockMvc.perform(post("/receipts/process").contentType(MediaType.APPLICATION_JSON)
                        .content(processReceiptRequestJson.toString()))
                .andExpect(status().isBadRequest());
    }

    public static List<Arguments> invalidRequestItems() throws JSONException {
        return List.of(
                Arguments.of("retailer"),
                Arguments.of("purchaseDate"),
                Arguments.of("purchaseTime"),
                Arguments.of("total"),
                Arguments.of("items")
        );
    }


    @ParameterizedTest
    @MethodSource(value = "missingFields")
    public void processReturns4xxOnInvalidRequestMissingFields(JSONArray itemsJson) throws Exception {
        processReceiptRequestJson.put("items", itemsJson);
        System.out.println(processReceiptRequestJson);
        mockMvc.perform(post("/receipts/process").contentType(MediaType.APPLICATION_JSON)
                        .content(processReceiptRequestJson.toString()))
                .andExpect(status().isBadRequest());
    }

    public static List<Arguments> missingFields() throws JSONException {
        return List.of(
                Arguments.of(new JSONArray("[]")),
                Arguments.of(new JSONArray("[ {'price': '0.00'} ]")),
                Arguments.of(new JSONArray("[ {shortDescription: 'blah'} ]")),
                Arguments.of(new JSONArray("[ {shortDescription: 'blah', price: '-1.00'} ]")),
                Arguments.of(new JSONArray("[ {shortDescription: 'blah', price: '2147483648.00'} ]")),
                Arguments.of(new JSONArray("[ {shortDescription: '', price: '1.23'} ]")),
                Arguments.of(new JSONArray("[ {shortDescription: '   ', price: '1.23'} ]")),
                Arguments.of(new JSONArray("[ {shortDescription: 'blah', price: ''} ]")),
                Arguments.of(new JSONArray("[ {shortDescription: 'blah', price: '     '} ]"))
        );
    }
}