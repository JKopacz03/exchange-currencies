package com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeDto {
    private BigDecimal amount;
    private String currencyFrom;
    private String currencyTo;
    private LocalDateTime dateTime;
}
