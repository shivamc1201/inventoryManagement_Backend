package com.nector.userservice.service;

import com.nector.userservice.dto.LedgerTransactionRequest;
import com.nector.userservice.dto.LedgerTransactionResponse;
import com.nector.userservice.dto.LedgerSummaryResponse;
import com.nector.userservice.exception.BusinessException;
import com.nector.userservice.model.Dealer;
import com.nector.userservice.model.DealerLedgerTransaction;
import com.nector.userservice.repository.DealerLedgerTransactionRepository;
import com.nector.userservice.repository.DealerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealerLedgerService {

    private final DealerLedgerTransactionRepository ledgerTransactionRepository;
    private final DealerRepository dealerRepository;

    @Transactional
    public void initializeOpeningBalance(Long dealerId, Long distributorId, BigDecimal openingBalance) {
        log.info("Initializing opening balance for dealer: {} and distributor: {} with amount: {}", 
                dealerId, distributorId, openingBalance);

        // Verify dealer exists and belongs to distributor
        Dealer dealer = dealerRepository.findByIdAndDistributorId(dealerId, distributorId)
                .orElseThrow(() -> new BusinessException("Dealer not found or access denied"));

        // Check if opening balance already exists
        String openingReference = "OPB-" + dealerId;
        if (ledgerTransactionRepository.existsByDealerIdAndDistributorIdAndReference(
                dealerId, distributorId, openingReference)) {
            log.warn("Opening balance already exists for dealer: {}", dealerId);
            return;
        }

        // Create opening balance transaction
        DealerLedgerTransaction openingTransaction = new DealerLedgerTransaction();
        openingTransaction.setId("TXN-INIT-" + dealerId);
        openingTransaction.setDealerId(dealerId);
        openingTransaction.setDistributorId(distributorId);
        openingTransaction.setDate(LocalDate.now());
        openingTransaction.setDescription("Opening Balance");
        openingTransaction.setReference(openingReference);
        openingTransaction.setType(com.nector.userservice.enums.LedgerTransactionType.JV);
        openingTransaction.setDebit(BigDecimal.ZERO);
        openingTransaction.setCredit(BigDecimal.ZERO);
        openingTransaction.setBalance(openingBalance);
        openingTransaction.setCategory(com.nector.userservice.enums.LedgerTransactionCategory.Journal);

        ledgerTransactionRepository.save(openingTransaction);
        log.info("Created opening balance transaction for dealer: {} with balance: {}", dealerId, openingBalance);
    }

    @Transactional
    public LedgerTransactionResponse createManualTransaction(LedgerTransactionRequest request, Long distributorId) {
        log.info("Creating manual ledger transaction for dealer: {} and distributor: {}", request.getDealerId(), distributorId);

        // Verify dealer exists and belongs to distributor
        Dealer dealer = dealerRepository.findByIdAndDistributorId(request.getDealerId(), distributorId)
                .orElseThrow(() -> new BusinessException("Dealer not found or access denied"));

        // Check reference uniqueness
        if (ledgerTransactionRepository.existsByDealerIdAndDistributorIdAndReference(
                request.getDealerId(), distributorId, request.getReference())) {
            throw new BusinessException("Reference number already exists for this dealer");
        }

        // Get latest balance with pessimistic lock
        BigDecimal previousBalance = ledgerTransactionRepository
                .findLatestByDealerIdAndDistributorIdForUpdate(request.getDealerId(), distributorId)
                .map(DealerLedgerTransaction::getBalance)
                .orElse(BigDecimal.ZERO);

        // Calculate new balance
        BigDecimal newBalance = calculateNewBalance(previousBalance, request.getType(), request.getAmount());

        // Create ledger transaction
        DealerLedgerTransaction transaction = new DealerLedgerTransaction();
        transaction.setId(generateTransactionId());
        transaction.setDealerId(request.getDealerId());
        transaction.setDistributorId(distributorId);
        transaction.setDate(request.getDate());
        transaction.setDescription(request.getDescription());
        transaction.setReference(request.getReference());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());

        if (request.getType().name().equals("DEBIT")) {
            transaction.setDebit(request.getAmount());
            transaction.setCredit(BigDecimal.ZERO);
        } else {
            transaction.setDebit(BigDecimal.ZERO);
            transaction.setCredit(request.getAmount());
        }
        transaction.setBalance(newBalance);

        DealerLedgerTransaction savedTransaction = ledgerTransactionRepository.save(transaction);
        log.info("Created manual ledger transaction with ID: {}", savedTransaction.getId());

        return convertToResponse(savedTransaction);
    }

    @Transactional(readOnly = true)
    public Page<LedgerTransactionResponse> getDealerLedger(Long dealerId, Long distributorId, LocalDate dateFrom, 
                                                          LocalDate dateTo, Pageable pageable) {
        log.info("Fetching ledger for dealer: {} and distributor: {}", dealerId, distributorId);

        // Verify dealer exists and belongs to distributor
        dealerRepository.findByIdAndDistributorId(dealerId, distributorId)
                .orElseThrow(() -> new BusinessException("Dealer not found or access denied"));

        List<DealerLedgerTransaction> transactions;
        if (dateFrom != null && dateTo != null) {
            transactions = ledgerTransactionRepository.findByDealerIdAndDistributorIdAndDateBetweenOrderByDateDescCreatedAtDesc(
                    dealerId, distributorId, dateFrom, dateTo);
        } else {
            transactions = ledgerTransactionRepository.findByDealerIdAndDistributorIdOrderByDateDescCreatedAtDesc(
                    dealerId, distributorId);
        }

        List<LedgerTransactionResponse> responses = transactions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<LedgerTransactionResponse> pageContent = responses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    @Transactional(readOnly = true)
    public LedgerSummaryResponse getLedgerSummary(Long dealerId, Long distributorId) {
        log.info("Fetching ledger summary for dealer: {} and distributor: {}", dealerId, distributorId);

        // Verify dealer exists and belongs to distributor
        Dealer dealer = dealerRepository.findByIdAndDistributorId(dealerId, distributorId)
                .orElseThrow(() -> new BusinessException("Dealer not found or access denied"));

        BigDecimal totalDebits = ledgerTransactionRepository.sumDebitsByDealer(dealerId, distributorId);
        BigDecimal totalCredits = ledgerTransactionRepository.sumCreditsByDealer(dealerId, distributorId);
        BigDecimal closingBalance = ledgerTransactionRepository.getLatestBalanceByDealer(dealerId, distributorId);
        long transactionCount = ledgerTransactionRepository.countTransactionsByDealer(dealerId, distributorId);

        LedgerSummaryResponse response = new LedgerSummaryResponse();
        response.setDealerId(dealerId);
        response.setDistributorId(distributorId);
        response.setDealerName(dealer.getFullName());
        response.setTotalDebits(totalDebits != null ? totalDebits : BigDecimal.ZERO);
        response.setTotalCredits(totalCredits != null ? totalCredits : BigDecimal.ZERO);
        response.setClosingBalance(closingBalance != null ? closingBalance : BigDecimal.ZERO);
        response.setTransactionCount(transactionCount);

        return response;
    }

    private BigDecimal calculateNewBalance(BigDecimal previousBalance, 
                                          com.nector.userservice.enums.LedgerTransactionType type, 
                                          BigDecimal amount) {
        switch (type) {
            case DEBIT:
                return previousBalance.add(amount); // Dealer owes more to distributor
            case CREDIT:
                return previousBalance.subtract(amount); // Distributor owes more to dealer
            case JV:
                // Journal vouchers can be either credit or debit based on amount sign
                return previousBalance.add(amount);
            default:
                return previousBalance;
        }
    }

    private String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return "TXN-" + timestamp + "-" + System.currentTimeMillis() % 1000;
    }

    private LedgerTransactionResponse convertToResponse(DealerLedgerTransaction transaction) {
        LedgerTransactionResponse response = new LedgerTransactionResponse();
        response.setId(transaction.getId());
        response.setDealerId(transaction.getDealerId());
        response.setDistributorId(transaction.getDistributorId());
        response.setDate(transaction.getDate().toString());
        response.setDescription(transaction.getDescription());
        response.setReference(transaction.getReference());
        response.setType(transaction.getType().name());
        response.setDebit(transaction.getDebit());
        response.setCredit(transaction.getCredit());
        response.setBalance(transaction.getBalance());
        response.setCategory(transaction.getCategory().name());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }
}
