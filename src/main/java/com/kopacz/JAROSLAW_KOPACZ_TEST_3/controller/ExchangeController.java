package com.kopacz.JAROSLAW_KOPACZ_TEST_3.controller;

import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.*;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.commands.ExchangeCommand;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.dto.ExchangeAmountDto;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.dto.ExchangeDto;
import com.kopacz.JAROSLAW_KOPACZ_TEST_3.service.ExchangeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ExchangeController {
    private final ExchangeService exchangeService;
    private final ModelMapper modelMapper;

    @PostMapping("/calculator/_exchange")
    public ResponseEntity<ExchangeAmountDto> exchange(@Valid @RequestBody ExchangeCommand command){
        return ResponseEntity.ok(exchangeService.exchange(command));
    }

    @GetMapping("/calculator/exchanges")
    public ResponseEntity<List<ExchangeDto>> getExchanges(@PageableDefault Pageable pageable,
                                                          @Size(min = 3, max = 3)
                                                          @RequestParam(required = false) String currencyFrom,
                                                          @Size(min = 3, max = 3)
                                                          @RequestParam(required = false) String currencyTo,
                                                          @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")
                                                          @RequestParam(required = false) String dateFrom,
                                                          @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")
                                                          @RequestParam(required = false) String dateTo,
                                                          @DecimalMin(value = "0.0", inclusive = false)
                                                          @RequestParam(required = false) BigDecimal amountFrom,
                                                          @DecimalMin(value = "0.0", inclusive = false)
                                                          @RequestParam(required = false) BigDecimal amountTo
                                                          ) {
        List<ExchangeDto> exchanges = exchangeService.findAll(currencyFrom,
                        currencyTo,
                        dateFrom,
                        dateTo,
                        amountFrom,
                        amountTo,
                        pageable).stream()
                .map(exchange -> modelMapper.map(exchange, ExchangeDto.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(exchanges);
    }

    @GetMapping("reports/{reportName}")
    public ResponseEntity<Map<String, Integer>> getReports(@PathVariable("reportName")
                                                           @Pattern(regexp = "HIGH_AMOUNT_LAST_MONTH|" +
                                                                   "GROUP_BY_CURRENCY_FROM_LAST_MONTH|" +
                                                                   "GROUP_BY_CURRENCY_TO_LAST_MONTH")
                                                           String reportName){
        return ResponseEntity.ok(exchangeService.getReport(Reports.valueOf(reportName)));
    }
}
