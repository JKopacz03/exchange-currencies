package com.kopacz.JAROSLAW_KOPACZ_TEST_3.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.exception.InvalidCurrencyException;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.*;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.commands.ExchangeCommand;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.dto.ExchangeAmountDto;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.repository.ExchangeRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.kopacz.JAROSLAW_KOPACZ_TEST_3.config.ExchangeSpecification.*;
import static com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.Reports.*;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRepository exchangeRepository;
    private final RestTemplate restTemplate;
    private final ModelMapper modelMapper;
    private final Cache<String, String> responseCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(100).build();;
    public void save(Exchange exchange){
        if(exchange.getCurrencyFrom().equalsIgnoreCase(exchange.getCurrencyTo())){
            throw new InvalidCurrencyException("CurrencyFrom is either currencyTo");
        }
        exchangeRepository.save(exchange);
    }

    public ExchangeAmountDto exchange(ExchangeCommand command) {
        if(command.getCurrencyFrom().equalsIgnoreCase(command.getCurrencyTo())){
            throw new InvalidCurrencyException("CurrencyFrom is either currencyTo");
        }

        BigDecimal currencyRate = getCurrencyRate(command);
        BigDecimal amountAfterExchange = command.getAmount().multiply(currencyRate).setScale(2, RoundingMode.HALF_UP);  ;

        save(modelMapper.map(command, Exchange.class));

        return new ExchangeAmountDto(amountAfterExchange);
    }

    private BigDecimal getCurrencyRate(ExchangeCommand command) {
        String currency = getCurrencyApi(command);

        Pattern pattern = Pattern.compile("\"" + command.getCurrencyTo().toLowerCase() + "\":\\s(\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(Objects.requireNonNull(currency));
        if(!matcher.find()){
            throw new InvalidCurrencyException("invalid currencyTo");
        }

        return new BigDecimal(matcher.group(1));
    }

    private String getCurrencyApi(ExchangeCommand command) {
        return responseCache.get(command.getCurrencyFrom().toLowerCase(), key -> {
            String url = String.format("https://latest.currency-api.pages.dev/v1/currencies/%s.json", key);
            try {
                return restTemplate.getForObject(url, String.class);
            }
            catch (final HttpClientErrorException e) {
                throw new InvalidCurrencyException("invalid currencyFrom");
            }
        });
    }

    public Page<Exchange> findAll(
            String currencyFrom,
            String currencyTo,
            String dateFrom,
            String dateTo,
            BigDecimal amountFrom,
            BigDecimal amountTo,
            Pageable pageable) {
        Specification<Exchange> filters = Specification.where(StringUtils.isBlank(currencyFrom) ? null : byCurrencyFrom(currencyFrom))
                .and(StringUtils.isBlank(currencyTo) ? null : byCurrencyTo(currencyTo))
                .and(Objects.isNull(dateFrom) ? null : byDateFrom(LocalDateTime.parse(dateFrom)))
                .and(Objects.isNull(dateTo) ? null : byDateTo(LocalDateTime.parse(dateTo)))
                .and(Objects.isNull(amountFrom) ? null : byAmountFrom(amountFrom))
                .and(Objects.isNull(amountTo) ? null : byAmountTo(amountTo));
        return exchangeRepository.findAll(filters, pageable);
    }

    public Map<String, Integer> getReport(Reports reportName) {
        List<Exchange> exchangesFromLastMonth = exchangeRepository
                .findAllWithCreationDateTimeAfter(LocalDateTime.now().minusMonths(1));

        if(reportName.equals(GROUP_BY_CURRENCY_FROM_LAST_MONTH)){
            return getGroupingByCurrency(exchangesFromLastMonth, Exchange::getCurrencyFrom);
        }
        if(reportName.equals(GROUP_BY_CURRENCY_TO_LAST_MONTH)){
            return getGroupingByCurrency(exchangesFromLastMonth, Exchange::getCurrencyTo);
        }
        if(reportName.equals(HIGH_AMOUNT_LAST_MONTH)){
            return exchangesFromLastMonth.stream()
                    .filter(this::isBiggerThen15000Euro)
                    .collect(Collectors.groupingBy(Exchange::getCurrencyFrom,
                            Collectors.collectingAndThen(Collectors.toList(), List::size)));
        }
        return Collections.emptyMap();
    }

    private static Map<String, Integer> getGroupingByCurrency(List<Exchange> exchanges, Function<Exchange, String> function) {
        return exchanges.stream()
                .collect(Collectors.groupingBy(function,
                        Collectors.collectingAndThen(Collectors.toList(), List::size)));
    }

    private boolean isBiggerThen15000Euro(Exchange e) {
        BigDecimal amount = e.getAmount();
        if(!e.getCurrencyFrom().equalsIgnoreCase("EUR")) {
            BigDecimal rate = getCurrencyRate(new ExchangeCommand(e.getAmount(), e.getCurrencyFrom(), "EUR"));
            amount = e.getAmount().multiply(rate);
        }
        return amount.compareTo(new BigDecimal("15000")) > 0;
    }
}
