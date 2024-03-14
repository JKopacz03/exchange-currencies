package com.kopacz.JAROSLAW_KOPACZ_TEST_3.config;

import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.Exchange;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExchangeSpecification {

    public static Specification<Exchange> byCurrencyFrom(String currencyFrom) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("currencyFrom"), currencyFrom);
    }

    public static Specification<Exchange> byCurrencyTo(String currencyTo) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("currencyTo"), currencyTo);
    }

    public static Specification<Exchange> byDateFrom(LocalDateTime dateFrom) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("dateTime"), dateFrom);
    }

    public static Specification<Exchange> byDateTo(LocalDateTime dateTo) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("dateTime"), dateTo);
    }

    public static Specification<Exchange> byAmountFrom(BigDecimal amountFrom) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), amountFrom);
    }

    public static Specification<Exchange> byAmountTo(BigDecimal amountTo) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("amount"), amountTo);
    }
}
