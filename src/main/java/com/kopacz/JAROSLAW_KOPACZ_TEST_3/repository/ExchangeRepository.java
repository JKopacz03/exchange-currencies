package com.kopacz.JAROSLAW_KOPACZ_TEST_3.repository;

import com.kopacz.JAROSLAW_KOPACZ_TEST_3.model.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ExchangeRepository extends JpaRepository<Exchange, Long>, JpaSpecificationExecutor<Exchange> {
    @Query("select a from Exchange a where a.dateTime >= :dateTime")
    List<Exchange> findAllWithCreationDateTimeAfter(
            @Param("dateTime") LocalDateTime localDateTime);
}
