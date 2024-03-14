package com.kopacz.JAROSLAW_KOPACZ_TEST_3.service;

import com.kopacz.JAROSLAW_KOPACZ_TEST_3.exception.InvalidCurrencyException;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.Exchange;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.commands.ExchangeCommand;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.dto.ExchangeAmountDto;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.repository.ExchangeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.Reports.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExchangeServiceTest {
    @Mock
    ExchangeRepository exchangeRepository;
    @Mock
    ModelMapper modelMapper;
    @Mock
    RestTemplate restTemplate;
    @InjectMocks
    private ExchangeService exchangeService;
    private List<Exchange> LIST_OF_EXCHANGES = List.of(
            new Exchange(null, new BigDecimal("5000.0"), "PLN", "USD",
                    LocalDateTime.of(2024, 3, 5, 12, 0, 0)),
            new Exchange(null, new BigDecimal("100.0"), "USD", "PLN",
                    LocalDateTime.of(2024, 2, 16, 12, 0, 0)),
            new Exchange(null, new BigDecimal("100.0"), "CHF", "PLN",
                    LocalDateTime.of(2024, 2, 27, 12, 0, 0)),
            new Exchange(null, new BigDecimal("100.0"), "CHF", "USD",
                    LocalDateTime.of(2024, 3, 3, 12, 0, 0)),
            new Exchange(null, new BigDecimal("100.0"), "USD", "CHF",
                    LocalDateTime.of(2024, 3, 3, 12, 0, 0)),
            new Exchange(null, new BigDecimal("16000.0"), "EUR", "CHF",
                    LocalDateTime.of(2024, 3, 3, 12, 0, 0)),
            new Exchange(null, new BigDecimal("100.0"), "EUR", "PLN",
                    LocalDateTime.of(2024, 3, 3, 12, 0, 0)));

    @BeforeEach
    void setUp() {

    }

    @Test
    void save_savingExchange_exchangeSaved(){
        //given
        Exchange exchange = new Exchange(
                new BigDecimal("100.0"),
                "PLN",
                "USD");
        //when
        exchangeService.save(exchange);
        //then
        verify(exchangeRepository, times(1)).save(exchange);
    }

    @Test
    void save_sameCurrencyFromAndTo_throwInvalidCurrencyException(){
        //given
        Exchange exchange = new Exchange(
                new BigDecimal("100.0"),
                "PLN",
                "pln");
        //when/then
        Assertions.assertThrows(
                InvalidCurrencyException.class,
                () -> exchangeService.save(exchange));
    }

    @Test
    void exchange_allOk_returnsExchangedCurrency(){
        //given
        ExchangeCommand command = new ExchangeCommand(
                new BigDecimal("100.0"),
                "PLN",
                "USD");
        exchangeTesting(command);
    }

    @Test
    void exchange_currencyFromLowerCase_returnsExchangedCurrency(){
        //given
        ExchangeCommand command = new ExchangeCommand(
                new BigDecimal("100.0"),
                "pln",
                "USD");
        exchangeTesting(command);
    }

    @Test
    void exchange_currencyToLowerCase_returnsExchangedCurrency(){
        //given
        ExchangeCommand command = new ExchangeCommand(
                new BigDecimal("100.0"),
                "PLN",
                "usd");
        exchangeTesting(command);
    }
    private void exchangeTesting(ExchangeCommand command) {
        when(restTemplate.getForObject(any(String.class), any(Class.class)))
                .thenReturn("\"usd\": 0.25");
        when(modelMapper.map(command, Exchange.class)).thenReturn(new Exchange(
                new BigDecimal("100.0"),
                "PLN",
                "USD"));
        //when
        ExchangeAmountDto dto = exchangeService.exchange(command);
        //then
        Assertions.assertEquals(new BigDecimal("25.00"), dto.getAmount());
    }

    @Test
    void exchange_sameCurrencyFromAndTo_throwInvalidCurrencyException(){
        //given
        ExchangeCommand command = new ExchangeCommand(
                new BigDecimal("100.0"),
                "PLN",
                "PLN");
        //when/then
        Assertions.assertThrows(
                InvalidCurrencyException.class,
                () -> exchangeService.exchange(command));
    }

    @Test
    void getReport_GROUP_BY_CURRENCY_FROM_LAST_MONTH_returnsReport(){
        //given
        when(exchangeRepository.findAllWithCreationDateTimeAfter(any(LocalDateTime.class)))
                .thenReturn(LIST_OF_EXCHANGES);
        //when
        Map<String, Integer> actualReport = exchangeService.getReport(GROUP_BY_CURRENCY_FROM_LAST_MONTH);
        //then
        Map<String, Integer> expectedReport= Map.ofEntries(Map.entry("PLN", 1),
                Map.entry("USD", 2),
                Map.entry("CHF", 2),
                Map.entry("EUR", 2)
        );
        Assertions.assertEquals(expectedReport, actualReport);
    }

    @Test
    void getReport_empty_GROUP_BY_CURRENCY_FROM_LAST_MONTH_returnsEmptyReport(){
        //given/when
        Map<String, Integer> actualReport = exchangeService.getReport(GROUP_BY_CURRENCY_FROM_LAST_MONTH);
        //then
        Assertions.assertEquals(Collections.emptyMap(), actualReport);
    }
    @Test
    void getReport_GROUP_BY_CURRENCY_TO_LAST_MONTH_returnsReport(){
        //given
        when(exchangeRepository.findAllWithCreationDateTimeAfter(any(LocalDateTime.class)))
                .thenReturn(LIST_OF_EXCHANGES);

        //when
        Map<String, Integer> actualReport = exchangeService.getReport(GROUP_BY_CURRENCY_TO_LAST_MONTH);
        //then
        Map<String, Integer> expectedReport= Map.ofEntries(Map.entry("USD", 2)
                ,Map.entry("PLN", 3),
                Map.entry("CHF", 2)
        );
        Assertions.assertEquals(expectedReport, actualReport);
    }

    @Test
    void getReport_empty_GROUP_BY_CURRENCY_TO_LAST_MONTH_returnsEmptyReport(){
        //given/when
        Map<String, Integer> actualReport = exchangeService.getReport(GROUP_BY_CURRENCY_TO_LAST_MONTH);
        //then
        Assertions.assertEquals(Collections.emptyMap(), actualReport);
    }

    @Test
    void getReport_HIGH_AMOUNT_LAST_MONTH_whenCurrencyFromIsEUR_returnsReport(){
        //given
        when(exchangeRepository.findAllWithCreationDateTimeAfter(any(LocalDateTime.class)))
                .thenReturn(LIST_OF_EXCHANGES);
        when(restTemplate.getForObject(any(String.class), any(Class.class)))
                .thenReturn("\"eur\": 0.0");
        //when
        Map<String, Integer> actualReport = exchangeService.getReport(HIGH_AMOUNT_LAST_MONTH);
        //then
        Map<String, Integer> expectedReport= Map.ofEntries(Map.entry("EUR", 1));
        Assertions.assertEquals(expectedReport, actualReport);
    }

    @Test
    void getReport_HIGH_AMOUNT_LAST_MONTH_whenCurrencyFromIsPLN_returnsReport(){
        //given
        when(exchangeRepository.findAllWithCreationDateTimeAfter(any(LocalDateTime.class)))
                .thenReturn(LIST_OF_EXCHANGES);
        when(restTemplate.getForObject(any(String.class), any(Class.class)))
                .thenReturn("\"eur\": 4.28");
        //when
        Map<String, Integer> actualReport = exchangeService.getReport(HIGH_AMOUNT_LAST_MONTH);
        //then
        Map<String, Integer> expectedReport= Map.ofEntries(Map.entry("PLN", 1),
                Map.entry("EUR", 1));
        Assertions.assertEquals(expectedReport, actualReport);
    }

    @Test
    void getReport_empty_HIGH_AMOUNT_LAST_MONTH_returnsEmptyReport(){
        //given/when
        Map<String, Integer> actualReport = exchangeService.getReport(HIGH_AMOUNT_LAST_MONTH);
        //then
        Assertions.assertEquals(Collections.emptyMap(), actualReport);
    }


}
