package com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.commands;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeCommand {
    @DecimalMin(message = "missing amount", value = "0.0", inclusive = false)
    private BigDecimal amount;
    @NotBlank(message = "missing currency")
    @Size(min = 3, max = 3, message = "invalid currencyFrom")
    private String currencyFrom;
    @NotBlank(message = "missing currency")
    @Size(min = 3, max = 3, message = "invalid currencyTo")
    private String currencyTo;
}
