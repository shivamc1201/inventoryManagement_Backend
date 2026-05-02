package com.nector.userservice.service;

import com.nector.userservice.dto.DealerOrderRequest;
import com.nector.userservice.dto.DealerSaleRequest;
import com.nector.userservice.enums.LedgerTransactionCategory;
import com.nector.userservice.enums.LedgerTransactionType;
import com.nector.userservice.exception.BusinessException;
import com.nector.userservice.model.Cart;
import com.nector.userservice.model.CartItem;
import com.nector.userservice.model.Dealer;
import com.nector.userservice.model.DealerLedgerTransaction;
import com.nector.userservice.model.DealerOrder;
import com.nector.userservice.model.DealerSale;
import com.nector.userservice.repository.CartItemRepository;
import com.nector.userservice.repository.CartRepository;
import com.nector.userservice.repository.DealerLedgerTransactionRepository;
import com.nector.userservice.repository.DealerOrderRepository;
import com.nector.userservice.repository.DealerRepository;
import com.nector.userservice.repository.DealerSaleRepository;
import com.nector.userservice.interceptors.distributor.model.OrderConfirmation;
import com.nector.userservice.interceptors.distributor.model.OrderConfirmationRequest;
import com.nector.userservice.interceptors.distributor.repository.OrderConfirmationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealerSaleService {

    private final DealerSaleRepository dealerSaleRepository;
    private final DealerLedgerTransactionRepository ledgerTransactionRepository;
    private final DealerRepository dealerRepository;
    private final DealerOrderRepository dealerOrderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderConfirmationRepository orderConfirmationRepository;

    @Transactional
    public DealerSale createDealerSale(DealerSaleRequest request, Long distributorId) {
        log.info("Creating dealer sale for dealer: {} and distributor: {}", request.getDealerId(), distributorId);

        // Verify dealer exists and belongs to distributor
        Dealer dealer = dealerRepository.findByIdAndDistributorId(request.getDealerId(), distributorId)
                .orElseThrow(() -> new BusinessException("Dealer not found or access denied"));

        // Check if price already exists for this product and dealer
        if (dealerSaleRepository.existsByDealerIdAndSkuAndDistributorId(request.getDealerId(), request.getSku(), distributorId)) {
            throw new BusinessException("Price already set for this product. Please update the existing price instead.");
        }

        // Create the sale
        DealerSale sale = new DealerSale();
        sale.setDealerId(request.getDealerId());
        sale.setDistributorId(distributorId);
        sale.setItemName(request.getItemName());
        sale.setSku(request.getSku());
        sale.setAmount(request.getAmount());
        sale.setDate(request.getDate());

        DealerSale savedSale = dealerSaleRepository.save(sale);
        log.info("Created dealer sale with ID: {}", savedSale.getId());

        // Create corresponding ledger entry
        createSaleLedgerEntry(savedSale, dealer);

        return savedSale;
    }

    @Transactional(readOnly = true)
    public List<DealerSale> getSalesByDealerId(Long dealerId) {
        log.info("Fetching sales for dealer: {}", dealerId);
        return dealerSaleRepository.findByDealerIdOrderByDateDesc(dealerId);
    }

    @Transactional(readOnly = true)
    public List<DealerSale> getSalesByDistributorId(Long distributorId) {
        log.info("Fetching sales for distributor: {}", distributorId);
        return dealerSaleRepository.findByDistributorIdOrderByDateDesc(distributorId);
    }

    @Transactional
    public DealerOrder createDealerOrder(DealerOrderRequest request, Long distributorId) {
        log.info("Creating dealer order for dealer: {} and distributor: {} with SKU: {} and quantity: {}",
                request.getDealerId(), distributorId, request.getSku(), request.getQuantity());

        // Verify dealer exists and belongs to distributor
        Dealer dealer = dealerRepository.findByIdAndDistributorId(request.getDealerId(), distributorId)
                .orElseThrow(() -> new BusinessException("Dealer not found or access denied"));

        // Check distributor's received complete orders for stock availability
        List<OrderConfirmation> distributorOrders = orderConfirmationRepository
                .findByDistributorIdOrderByConfirmedAtDesc(distributorId);

        List<OrderConfirmation> receivedCompleteOrders = distributorOrders.stream()
                .filter(order -> order.getStatus() == OrderConfirmationRequest.ConfirmationStatus.RECEIVED_COMPLETE)
                .toList();

        if (receivedCompleteOrders.isEmpty()) {
            throw new BusinessException("Distributor has no received complete orders. Cannot process dealer order.");
        }

        // Calculate available stock from received complete orders for the requested SKU
        int availableStock = receivedCompleteOrders.stream()
                .flatMap(order -> order.getItemConfirmations().stream())
                .filter(item -> request.getSku().equalsIgnoreCase(item.getSku()))
                .mapToInt(item -> item.getReceivedQuantity() != null ? item.getReceivedQuantity() : 0)
                .sum();

        if (availableStock <= 0) {
            throw new BusinessException(
                    "Product with SKU '" + request.getSku() + "' not found in distributor's received stock. Distributor must receive stock from company first.");
        }

        // Check if sufficient quantity available
        if (availableStock < request.getQuantity()) {
            throw new BusinessException(
                    "Insufficient stock for SKU '" + request.getSku() + "'. Available: " + availableStock + ", Requested: " + request.getQuantity());
        }

        log.info("Distributor {} has available stock for SKU '{}': {} units from {} received complete orders",
                distributorId, request.getSku(), availableStock, receivedCompleteOrders.size());

        // Create the order
        DealerOrder order = new DealerOrder();
        order.setDealerId(request.getDealerId());
        order.setDistributorId(distributorId);
        order.setSku(request.getSku());
        order.setItemName(request.getItemName());
        order.setQuantity(request.getQuantity());
        order.setAmount(request.getAmount());
        order.setDate(request.getDate());

        DealerOrder savedOrder = dealerOrderRepository.save(order);
        log.info("Created dealer order with ID: {}", savedOrder.getId());

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<DealerOrder> getOrdersByDealerId(Long dealerId) {
        log.info("Fetching orders for dealer: {}", dealerId);
        return dealerOrderRepository.findByDealerIdOrderByDateDesc(dealerId);
    }

    @Transactional(readOnly = true)
    public List<DealerOrder> getOrdersByDistributorId(Long distributorId) {
        log.info("Fetching orders for distributor: {}", distributorId);
        return dealerOrderRepository.findByDistributorIdOrderByDateDesc(distributorId);
    }

    @Transactional
    public void deleteDealerSale(Long saleId, Long distributorId) {
        log.info("Deleting dealer sale: {} for distributor: {}", saleId, distributorId);

        // Find the sale with tenant isolation
        DealerSale sale = dealerSaleRepository.findByIdAndDistributorId(saleId, distributorId)
                .orElseThrow(() -> new BusinessException("Sale not found or access denied"));

        // Delete the sale
        dealerSaleRepository.delete(sale);
        log.info("Deleted dealer sale with ID: {}", saleId);
    }

    private void  createSaleLedgerEntry(DealerSale sale, Dealer dealer) {
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
        transaction.setDescription("Sale of " + sale.getItemName() + " (SKU: " + sale.getSku() + ")");
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
