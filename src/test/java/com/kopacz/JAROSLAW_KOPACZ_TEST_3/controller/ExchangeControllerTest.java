package com.kopacz.JAROSLAW_KOPACZ_TEST_3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.DatabaseCleaner;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.JaroslawKopaczTest3Application;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.commands.ExchangeCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(classes = JaroslawKopaczTest3Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ExchangeControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DatabaseCleaner databaseCleaner;
    private final RestTemplate restTemplate;

    @Autowired
    public ExchangeControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, DatabaseCleaner databaseCleaner, RestTemplate restTemplate) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.databaseCleaner = databaseCleaner;
        this.restTemplate = restTemplate;
    }

    @AfterEach
    void tearDown() throws Exception {
        databaseCleaner.cleanUp();
    }

    @Test
    void shouldExchangeAmount() throws Exception {
        String currencyFrom = "PLN";
        String currencyTo = "USD";
        BigDecimal amount = new BigDecimal("100.0");
        ExchangeCommand exchangeCommand = new ExchangeCommand(amount,
                currencyFrom, currencyTo);
        String json = objectMapper.writeValueAsString(exchangeCommand);

        BigDecimal expectedValue = getExpectedValue(currencyFrom, currencyTo, amount);

        mockMvc.perform(post("/api/v1/calculator/_exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(expectedValue));
    }

    @Test
    void shouldExchangeAmountWithLowerCaseCurrencyFrom() throws Exception {
        String currencyFrom = "pln";
        String currencyTo = "USD";
        BigDecimal amount = new BigDecimal("100.0");
        ExchangeCommand exchangeCommand = new ExchangeCommand(amount,
                currencyFrom, currencyTo);
        String json = objectMapper.writeValueAsString(exchangeCommand);

        BigDecimal expectedValue = getExpectedValue(currencyFrom, currencyTo, amount);

        mockMvc.perform(post("/api/v1/calculator/_exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(expectedValue));
    }

    @Test
    void shouldExchangeAmountWithLowerCaseCurrencyTo() throws Exception {
        String currencyFrom = "PLN";
        String currencyTo = "usd";
        BigDecimal amount = new BigDecimal("100.0");
        ExchangeCommand exchangeCommand = new ExchangeCommand(amount,
                currencyFrom, currencyTo);
        String json = objectMapper.writeValueAsString(exchangeCommand);

        BigDecimal expectedValue = getExpectedValue(currencyFrom, currencyTo, amount);

        mockMvc.perform(post("/api/v1/calculator/_exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(expectedValue));
    }

    @Test
    void invalidCurrencyFrom_shouldReturnBadRequest() throws Exception {
        String currencyFrom = "x";
        String currencyTo = "USD";
        BigDecimal amount = new BigDecimal("100.0");
        ExchangeCommand exchangeCommand = new ExchangeCommand(amount,
                currencyFrom, currencyTo);
        String json = objectMapper.writeValueAsString(exchangeCommand);

        mockMvc.perform(post("/api/v1/calculator/_exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("invalid currencyFrom"));
    }

    @Test
    void invalidCurrencyTo_shouldReturnBadRequest() throws Exception {
        String currencyFrom = "PLN";
        String currencyTo = "xxxx";
        BigDecimal amount = new BigDecimal("100.0");
        ExchangeCommand exchangeCommand = new ExchangeCommand(amount,
                currencyFrom, currencyTo);
        String json = objectMapper.writeValueAsString(exchangeCommand);

        mockMvc.perform(post("/api/v1/calculator/_exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("invalid currencyTo"));
    }

    @Test
    void negativeAmount_shouldReturnBadRequest() throws Exception {
        String currencyFrom = "PLN";
        String currencyTo = "USD";
        BigDecimal amount = new BigDecimal("-1.0");
        ExchangeCommand exchangeCommand = new ExchangeCommand(amount,
                currencyFrom, currencyTo);
        String json = objectMapper.writeValueAsString(exchangeCommand);

        mockMvc.perform(post("/api/v1/calculator/_exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("missing amount"));
    }

    @Test
    void zeroAmount_shouldReturnBadRequest() throws Exception {
        String currencyFrom = "PLN";
        String currencyTo = "USD";
        BigDecimal amount = new BigDecimal("0.0");
        ExchangeCommand exchangeCommand = new ExchangeCommand(amount,
                currencyFrom, currencyTo);
        String json = objectMapper.writeValueAsString(exchangeCommand);

        mockMvc.perform(post("/api/v1/calculator/_exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("missing amount"));
    }

    @Test
    void exchangeToSameCurrency_shouldReturnBadRequest() throws Exception {
        String currencyFrom = "PLN";
        String currencyTo = "PLN";
        BigDecimal amount = new BigDecimal("1.0");
        ExchangeCommand exchangeCommand = new ExchangeCommand(amount,
                currencyFrom, currencyTo);
        String json = objectMapper.writeValueAsString(exchangeCommand);

        mockMvc.perform(post("/api/v1/calculator/_exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CurrencyFrom is either currencyTo"));
    }

    private BigDecimal getExpectedValue(String currencyFrom, String currencyTo, BigDecimal amount) {
        String currency = restTemplate.getForObject(String.format("https://latest.currency-api.pages.dev/v1/currencies/%s.json", currencyFrom.toLowerCase()), String.class);
        Pattern pattern = Pattern.compile("\"" + currencyTo.toLowerCase() + "\":\\s(\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(Objects.requireNonNull(currency));
        matcher.find();
        BigDecimal currencyRate = new BigDecimal(matcher.group(1));
        BigDecimal expectedValue = amount.multiply(currencyRate).setScale(2, RoundingMode.HALF_UP);
        return expectedValue;
    }

    @Test
    void shouldGetExchangesWithPageSize5() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].amount").value(12000.00))
                .andExpect(jsonPath("$.[0].currencyFrom").value("USD"))
                .andExpect(jsonPath("$.[0].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[0].dateTime").value("2024-02-05T14:00:00"))
                .andExpect(jsonPath("$.[1].amount").value(16000.00))
                .andExpect(jsonPath("$.[1].currencyFrom").value("CHF"))
                .andExpect(jsonPath("$.[1].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[1].dateTime").value("2024-02-10T16:00:00"))
                .andExpect(jsonPath("$.[2].amount").value(8000.00))
                .andExpect(jsonPath("$.[2].currencyFrom").value("EUR"))
                .andExpect(jsonPath("$.[2].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[2].dateTime").value("2024-02-15T18:00:00"))
                .andExpect(jsonPath("$.[3].amount").value(11000.00))
                .andExpect(jsonPath("$.[3].currencyFrom").value("USD"))
                .andExpect(jsonPath("$.[3].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[3].dateTime").value("2024-02-20T20:00:00"))
                .andExpect(jsonPath("$.[4].amount").value(14000.00))
                .andExpect(jsonPath("$.[4].currencyFrom").value("CHF"))
                .andExpect(jsonPath("$.[4].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[4].dateTime").value("2024-02-25T22:00:00"));
    }


    @Test
    void shouldGetExchangesWithSecondPage() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?pageSize=5&pageNumber=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].amount").value(12000.00))
                .andExpect(jsonPath("$.[0].currencyFrom").value("USD"))
                .andExpect(jsonPath("$.[0].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[0].dateTime").value("2024-02-05T14:00:00"))
                .andExpect(jsonPath("$.[1].amount").value(16000.00))
                .andExpect(jsonPath("$.[1].currencyFrom").value("CHF"))
                .andExpect(jsonPath("$.[1].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[1].dateTime").value("2024-02-10T16:00:00"))
                .andExpect(jsonPath("$.[2].amount").value(8000.00))
                .andExpect(jsonPath("$.[2].currencyFrom").value("EUR"))
                .andExpect(jsonPath("$.[2].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[2].dateTime").value("2024-02-15T18:00:00"))
                .andExpect(jsonPath("$.[3].amount").value(11000.00))
                .andExpect(jsonPath("$.[3].currencyFrom").value("USD"))
                .andExpect(jsonPath("$.[3].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[3].dateTime").value("2024-02-20T20:00:00"))
                .andExpect(jsonPath("$.[4].amount").value(14000.00))
                .andExpect(jsonPath("$.[4].currencyFrom").value("CHF"))
                .andExpect(jsonPath("$.[4].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[4].dateTime").value("2024-02-25T22:00:00"));
    }

    @Test
    void shouldGetExchangesWithCurrencyFromUSD() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?currencyFrom=USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].amount").value(12000.00))
                .andExpect(jsonPath("$.[0].currencyFrom").value("USD"))
                .andExpect(jsonPath("$.[0].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[0].dateTime").value("2024-02-05T14:00:00"))
                .andExpect(jsonPath("$.[1].amount").value(11000.00))
                .andExpect(jsonPath("$.[1].currencyFrom").value("USD"))
                .andExpect(jsonPath("$.[1].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[1].dateTime").value("2024-02-20T20:00:00"))
                .andExpect(jsonPath("$.[2].amount").value(9000.00))
                .andExpect(jsonPath("$.[2].currencyFrom").value("USD"))
                .andExpect(jsonPath("$.[2].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[2].dateTime").value("2024-03-05T02:00:00"));
    }


    @Test
    void shouldGetExchangesWithCurrencyToEUR() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?currencyTo=EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].amount").value(1000.00))
                .andExpect(jsonPath("$.[0].currencyFrom").value("CHF"))
                .andExpect(jsonPath("$.[0].currencyTo").value("EUR"))
                .andExpect(jsonPath("$.[0].dateTime").value("2024-02-01T12:00:00"));
    }

    @Test
    void shouldGetExchangesWithDateTimeFrom() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?dateFrom=2024-03-09T14:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].amount").value(12000.00))
                .andExpect(jsonPath("$.[0].currencyFrom").value("CHF"))
                .andExpect(jsonPath("$.[0].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[0].dateTime").value("2024-03-10T04:00:00"))
                .andExpect(jsonPath("$.[1].amount").value(15000.00))
                .andExpect(jsonPath("$.[1].currencyFrom").value("EUR"))
                .andExpect(jsonPath("$.[1].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[1].dateTime").value("2024-03-15T06:00:00"));
    }

    @Test
    void shouldGetExchangesWithDateTimeTo() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?dateTo=2024-02-04T14:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].amount").value(10000.00))
                .andExpect(jsonPath("$.[0].currencyFrom").value("EUR"))
                .andExpect(jsonPath("$.[0].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[0].dateTime").value("2024-02-01T12:00:00"))
                .andExpect(jsonPath("$.[1].amount").value(1000.00))
                .andExpect(jsonPath("$.[1].currencyFrom").value("CHF"))
                .andExpect(jsonPath("$.[1].currencyTo").value("EUR"))
                .andExpect(jsonPath("$.[1].dateTime").value("2024-02-01T12:00:00"));
    }

    @Test
    void shouldGetExchangesWithAmountFrom() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?amountFrom=16500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].amount").value(17000.00))
                .andExpect(jsonPath("$.[0].currencyFrom").value("EUR"))
                .andExpect(jsonPath("$.[0].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[0].dateTime").value("2024-03-01T00:00:00"));
    }

    @Test
    void shouldGetExchangesWithAmountTo() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?amountTo=2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].amount").value(1000.00))
                .andExpect(jsonPath("$.[0].currencyFrom").value("CHF"))
                .andExpect(jsonPath("$.[0].currencyTo").value("EUR"))
                .andExpect(jsonPath("$.[0].dateTime").value("2024-02-01T12:00:00"));
    }

    @Test
    void shouldGetExchangesWithAmountFromDateFromCurrencyFrom() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?amountFrom=8000&currencyFrom=USD&dateFrom=2024-03-04T12:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].amount").value(9000.00))
                .andExpect(jsonPath("$.[0].currencyFrom").value("USD"))
                .andExpect(jsonPath("$.[0].currencyTo").value("PLN"))
                .andExpect(jsonPath("$.[0].dateTime").value("2024-03-05T02:00:00"));
    }

    @Test
    void invalidDateFrom_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?dateFrom=2024-002:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidDateTo_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?dateTo=2024-002:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidAmountFrom_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?amountFrom=-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidAmountTo_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/calculator/exchanges?amountTo=-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnGROUP_BY_CURRENCY_FROM_LAST_MONTH() throws Exception {
        mockMvc.perform(get("/api/v1/reports/GROUP_BY_CURRENCY_FROM_LAST_MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.CHF").value(2))
                .andExpect(jsonPath("$.EUR").value(3))
                .andExpect(jsonPath("$.USD").value(2));
    }

    @Test
    void shouldReturnGROUP_BY_CURRENCY_TO_LAST_MONTH() throws Exception {
        mockMvc.perform(get("/api/v1/reports/GROUP_BY_CURRENCY_TO_LAST_MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PLN").value(7));
    }

    @Test
    void shouldReturnHIGH_AMOUNT_LAST_MONTH() throws Exception {
        mockMvc.perform(get("/api/v1/reports/HIGH_AMOUNT_LAST_MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.EUR").value(1));
    }

    @Test
    void invalidReport_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/reports/INVALID_REPORT"))
                .andExpect(status().isBadRequest());
    }
}
