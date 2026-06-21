package com.nector.userservice.interceptors.products.impl;

import com.nector.userservice.exception.ResourceNotFoundException;
import com.nector.userservice.interceptors.products.model.OutwardItemRequest;
import com.nector.userservice.interceptors.products.model.OutwardItemResponse;
import com.nector.userservice.interceptors.products.service.OutwardItemService;
import com.nector.userservice.model.OutwardItemTransaction;
import com.nector.userservice.repository.OutwardItemTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutwardItemServiceImpl implements OutwardItemService {

    private final OutwardItemTransactionRepository outwardItemRepository;

    @Override
    @Transactional
    public OutwardItemResponse createOutwardItem(OutwardItemRequest request) {
        log.info("Creating outward item transaction for material code: {}", request.getMaterialCode());

        OutwardItemTransaction transaction = new OutwardItemTransaction();
        transaction.setItemType(request.getItemType());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setMaterialCode(request.getMaterialCode());
        transaction.setMaterialName(request.getMaterialName());
        transaction.setUnit(request.getUnit());
        transaction.setQuantity(request.getQuantity());
        transaction.setComments(request.getComments());
        transaction.setQuotedSellingPrice(request.getQuotedSellingPrice());
        transaction.setReferenceNumber(request.getReferenceNumber());
        transaction.setIssuedTo(request.getIssuedTo());

        OutwardItemTransaction savedTransaction = outwardItemRepository.save(transaction);
        log.info("Outward item transaction created successfully with ID: {}", savedTransaction.getId());

        return mapToResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public OutwardItemResponse getOutwardItemById(Long id) {
        log.info("Fetching outward item transaction with ID: {}", id);

        OutwardItemTransaction transaction = outwardItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Outward item transaction not found with id: " + id));

        return mapToResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutwardItemResponse> getAllOutwardItems() {
        log.info("Fetching all outward item transactions");

        return outwardItemRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutwardItemResponse> getOutwardItemsByType(OutwardItemTransaction.ItemType itemType) {
        log.info("Fetching outward item transactions by type: {}", itemType);

        return outwardItemRepository.findByItemType(itemType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutwardItemResponse> getOutwardItemsByTransactionType(OutwardItemTransaction.TransactionType transactionType) {
        log.info("Fetching outward item transactions by transaction type: {}", transactionType);

        return outwardItemRepository.findByTransactionType(transactionType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutwardItemResponse> getOutwardItemsByTypeAndTransaction(
            OutwardItemTransaction.ItemType itemType,
            OutwardItemTransaction.TransactionType transactionType) {
        log.info("Fetching outward item transactions by type: {} and transaction type: {}", itemType, transactionType);

        return outwardItemRepository.findByItemTypeAndTransactionType(itemType, transactionType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OutwardItemResponse mapToResponse(OutwardItemTransaction transaction) {
        OutwardItemResponse response = new OutwardItemResponse();
        response.setId(transaction.getId());
        response.setItemType(transaction.getItemType());
        response.setTransactionType(transaction.getTransactionType());
        response.setMaterialCode(transaction.getMaterialCode());
        response.setMaterialName(transaction.getMaterialName());
        response.setUnit(transaction.getUnit());
        response.setQuantity(transaction.getQuantity());
        response.setComments(transaction.getComments());
        response.setQuotedSellingPrice(transaction.getQuotedSellingPrice());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        response.setReferenceNumber(transaction.getReferenceNumber());
        response.setIssuedTo(transaction.getIssuedTo());
        return response;
    }
}
