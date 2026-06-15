package com.nector.userservice.transaction.repository;

import com.nector.userservice.transaction.entity.TransactionFund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionFundRepository extends JpaRepository<TransactionFund, Long> {

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM TransactionFund f WHERE f.date BETWEEN :from AND :to")
    BigDecimal sumAmountBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM TransactionFund f WHERE f.date < :before")
    BigDecimal sumAmountBefore(@Param("before") LocalDate before);
}
