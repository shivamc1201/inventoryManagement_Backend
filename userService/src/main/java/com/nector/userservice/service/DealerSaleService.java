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

        // Check distributor's active cart for stock availability
        Cart cart = cartRepository.findActiveCartByDistributorId(distributorId)
                .orElseThrow(() -> new BusinessException("Distributor has no active cart. Cannot process order."));

        // Find cart item by SKU
        CartItem cartItem = cart.getCartItems().stream()
                .filter(ci -> ci.getItem() != null && request.getSku().equalsIgnoreCase(ci.getItem().getSku()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Product with SKU '" + request.getSku() + "' not found in distributor's cart. Distributor must order from company first."));

        // Check if sufficient quantity available
        if (cartItem.getQuantity() < request.getQuantity()) {
            throw new BusinessException(
                    "Insufficient stock for SKU '" + request.getSku() + "'. Available: " + cartItem.getQuantity() + ", Requested: " + request.getQuantity());
        }

        // Deduct quantity from cart
        int remainingQuantity = cartItem.getQuantity() - request.getQuantity();
        cartItem.setQuantity(remainingQuantity);
        cartItemRepository.save(cartItem);
        log.info("Deducted {} from cart item with SKU '{}'. Remaining quantity: {}", request.getQuantity(), request.getSku(), remainingQuantity);

        // Remove cart item if quantity becomes 0
        if (remainingQuantity == 0) {
            cartItemRepository.delete(cartItem);
            log.info("Removed cart item with SKU '{}' as quantity became 0", request.getSku());
        }

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
        transaction.setDescription("Sale of " + sale.getItemName() + ("SKU of " + sale.getSku()));
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
