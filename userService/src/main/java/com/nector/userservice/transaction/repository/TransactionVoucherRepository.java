package com.nector.userservice.transaction.repository;

import com.nector.userservice.transaction.entity.TransactionVoucher;
import com.nector.userservice.transaction.enums.LedgerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionVoucherRepository extends JpaRepository<TransactionVoucher, Long> {

    Optional<TransactionVoucher> findByVoucherNo(String voucherNo);

    List<TransactionVoucher> findByLedgerName(String ledgerName);

    @Query("SELECT v.ledgerName AS ledgerName, "
            + "SUM(v.amount) AS grossAmount, "
            + "SUM(v.lessAdjustment) AS lessAdjustment "
            + "FROM TransactionVoucher v "
            + "WHERE v.date BETWEEN :from AND :to "
            + "GROUP BY v.ledgerName")
    List<LedgerCashbookAggregate> aggregateByLedgerBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(v.amount - v.lessAdjustment), 0) "
            + "FROM TransactionVoucher v "
            + "WHERE v.voucherType = :voucherType AND v.date < :before")
    BigDecimal sumNetAmountByTypeBefore(@Param("voucherType") LedgerType voucherType, @Param("before") LocalDate before);
}
