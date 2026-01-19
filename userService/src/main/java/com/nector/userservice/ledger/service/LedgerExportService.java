package com.nector.userservice.ledger.service;

import com.nector.userservice.ledger.entity.LedgerTransaction;
import com.nector.userservice.ledger.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting ledger data to various formats
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerExportService {

    private final LedgerTransactionService ledgerTransactionService;

    /**
     * Export transactions to CSV format
     */
    public byte[] exportTransactionsToCSV(Long accountId, LocalDate startDate, 
                                        LocalDate endDate, TransactionType transactionType) {
        try {
            List<LedgerTransaction> transactions = ledgerTransactionService
                    .getTransactionsForExport(accountId, startDate, endDate, transactionType);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(outputStream);

            // Write CSV header
            writer.println("Date,Type,Reference,Description,Debit,Credit,Balance,Category,Notes,Created By,Created At");

            // Write transaction data
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (LedgerTransaction transaction : transactions) {
                writer.printf("%s,%s,%s,\"%s\",%s,%s,%s,%s,\"%s\",%s,%s%n",
                        transaction.getTransactionDate().format(dateFormatter),
                        transaction.getTransactionType().name(),
                        transaction.getReferenceNumber(),
                        escapeCSV(transaction.getDescription()),
                        transaction.getDebitAmount(),
                        transaction.getCreditAmount(),
                        transaction.getRunningBalance(),
                        transaction.getCategory() != null ? transaction.getCategory().getCategoryName() : "",
                        escapeCSV(transaction.getNotes() != null ? transaction.getNotes() : ""),
                        transaction.getCreatedBy(),
                        transaction.getCreatedAt().format(dateTimeFormatter)
                );
            }

            writer.flush();
            writer.close();

            log.info("Exported {} transactions to CSV for account {}", transactions.size(), accountId);
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error exporting transactions to CSV", e);
            throw new RuntimeException("Failed to export transactions to CSV", e);
        }
    }

    /**
     * Generate filename for export
     */
    public String generateExportFilename(Long accountId, String format, LocalDate startDate, LocalDate endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateRange = "";
        
        if (startDate != null && endDate != null) {
            dateRange = "_" + startDate.format(formatter) + "_to_" + endDate.format(formatter);
        } else if (startDate != null) {
            dateRange = "_from_" + startDate.format(formatter);
        } else if (endDate != null) {
            dateRange = "_until_" + endDate.format(formatter);
        }

        return String.format("ledger_account_%d%s.%s", accountId, dateRange, format.toLowerCase());
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}