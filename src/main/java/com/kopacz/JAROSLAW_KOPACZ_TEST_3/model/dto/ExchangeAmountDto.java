package com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ExchangeAmountDto {
    private BigDecimal amount;
}
