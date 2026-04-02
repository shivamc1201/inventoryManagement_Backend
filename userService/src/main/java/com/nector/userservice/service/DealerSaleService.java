package com.nector.userservice.service;

import com.nector.userservice.dto.DealerSaleRequest;
import com.nector.userservice.enums.LedgerTransactionCategory;
import com.nector.userservice.enums.LedgerTransactionType;
import com.nector.userservice.exception.BusinessException;
import com.nector.userservice.model.Dealer;
import com.nector.userservice.model.DealerLedgerTransaction;
import com.nector.userservice.model.DealerSale;
import com.nector.userservice.repository.DealerLedgerTransactionRepository;
import com.nector.userservice.repository.DealerRepository;
import com.nector.userservice.repository.DealerSaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealerSaleService {

    private final DealerSaleRepository dealerSaleRepository;
    private final DealerLedgerTransactionRepository ledgerTransactionRepository;
    private final DealerRepository dealerRepository;

    @Transactional
    public DealerSale createDealerSale(DealerSaleRequest request, Long distributorId) {
        log.info("Creating dealer sale for dealer: {} and distributor: {}", request.getDealerId(), distributorId);

        // Verify dealer exists and belongs to distributor
        Dealer dealer = dealerRepository.findByIdAndDistributorId(request.getDealerId(), distributorId)
                .orElseThrow(() -> new BusinessException("Dealer not found or access denied"));

        // Create the sale
        DealerSale sale = new DealerSale();
        sale.setDealerId(request.getDealerId());
        sale.setDistributorId(distributorId);
        sale.setItemName(request.getItemName());
        sale.setQuantity(request.getQuantity());
        sale.setAmount(request.getAmount());
        sale.setDate(request.getDate());

        DealerSale savedSale = dealerSaleRepository.save(sale);
        log.info("Created dealer sale with ID: {}", savedSale.getId());

        // Create corresponding ledger entry
        createSaleLedgerEntry(savedSale, dealer);

        return savedSale;
    }

    private void createSaleLedgerEntry(DealerSale sale, Dealer dealer) {
        log.info("Creating ledger entry for sale ID: {}", sale.getId());

        // Get latest balance with pessimistic lock
        BigDecimal previousBalance = ledgerTransactionRepository
                .findLatestByDealerIdAndDistributorIdForUpdate(sale.getDealerId(), sale.getDistributorId())
                .map(DealerLedgerTransaction::getBalance)
                .orElse(BigDecimal.ZERO);

        // Calculate new balance (sale amount increases dealer's debt to distributor)
        BigDecimal newBalance = previousBalance.add(sale.getAmount());

        // Create ledger transaction
        DealerLedgerTransaction transaction = new DealerLedgerTransaction();
        transaction.setId(generateTransactionId());
        transaction.setDealerId(sale.getDealerId());
        transaction.setDistributorId(sale.getDistributorId());
        transaction.setDate(sale.getDate());
        transaction.setDescription("Sale of " + sale.getItemName() + " (Qty: " + sale.getQuantity() + ")");
        transaction.setReference("SALE-" + sale.getId());
        transaction.setType(LedgerTransactionType.DEBIT);
        transaction.setDebit(sale.getAmount());
        transaction.setCredit(BigDecimal.ZERO);
        transaction.setBalance(newBalance);
        transaction.setCategory(LedgerTransactionCategory.Purchase);

        ledgerTransactionRepository.save(transaction);
        log.info("Created ledger transaction with ID: {} for sale ID: {}", transaction.getId(), sale.getId());
    }

    private String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return "TXN-" + timestamp + "-" + System.currentTimeMillis() % 1000;
    }
}
