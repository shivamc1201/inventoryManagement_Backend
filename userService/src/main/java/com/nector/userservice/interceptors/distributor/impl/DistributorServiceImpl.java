package com.nector.userservice.interceptors.distributor.impl;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.features.Features;
import com.nector.userservice.exception.ResourceNotFoundException;
import com.nector.userservice.interceptors.distributor.model.*;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.interceptors.distributor.repository.OrderConfirmationRepository;
import com.nector.userservice.interceptors.distributor.service.DistributorMapper;
import com.nector.userservice.interceptors.distributor.service.DistributorService;
import com.nector.userservice.interceptors.userLogin.model.LoginRequest;
import com.nector.userservice.interceptors.userLogin.model.LoginResponse;
import com.nector.userservice.ledger.dto.CreateLedgerAccountRequest;
import com.nector.userservice.ledger.service.LedgerAccountService;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.ItemRepository;
import com.nector.userservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nector.userservice.common.RoleType;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DistributorServiceImpl implements DistributorService {
    
    private final DistributorRepository distributorRepository;
    private final DistributorMapper distributorMapper;
    private final LedgerAccountService ledgerAccountService;
    private final OrderConfirmationRepository orderConfirmationRepository;
    private final ItemRepository itemRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    
    @Override
    public DistributorResponseDTO createDistributor(DistributorRequestDTO request) {
        log.info("Creating distributor with email: {}", request.getContactEmail());

        if (distributorRepository.existsByContactEmail(request.getContactEmail())) {
            throw new IllegalArgumentException(
                    "Distributor with email already exists: " + request.getContactEmail());
        }

        Distributor distributor = distributorMapper.toEntity(request);
        Distributor savedDistributor = distributorRepository.save(distributor);

        // Auto-create ledger account for the distributor
        try {
            CreateLedgerAccountRequest ledgerRequest = new CreateLedgerAccountRequest();
            ledgerRequest.setCompanyId(1L); // Default company ID
            ledgerRequest.setDistributorId(savedDistributor.getId());
            ledgerRequest.setAccountName(savedDistributor.getName() + " - Ledger Account");
            ledgerRequest.setCreditLimit(BigDecimal.ZERO);

            ledgerAccountService.createLedgerAccount(ledgerRequest, "system");
            log.info("Ledger account created for distributor: {}", savedDistributor.getId());
        } catch (Exception e) {
            log.warn("Failed to create ledger account for distributor: {}", savedDistributor.getId(), e);
        }

        log.info("Distributor created successfully with ID: {}", savedDistributor.getId());
        return distributorMapper.toResponseDTO(savedDistributor);
    }

    @Override
    @Transactional(readOnly = true)
    public DistributorResponseDTO getDistributorById(Long id) {
        log.info("Fetching distributor with ID: {}", id);
        
        Distributor distributor = distributorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Distributor not found with ID: " + id));
        
        return distributorMapper.toResponseDTO(distributor);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DistributorResponseDTO> getAllDistributors() {
        log.info("Fetching all distributors");
        
        return distributorRepository.findAll()
                .stream()
                .map(distributorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public DistributorResponseDTO updateDistributor(Long id, DistributorRequestDTO request) {
        log.info("Updating distributor with ID: {}", id);
        
        Distributor distributor = distributorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Distributor not found with ID: " + id));
        
        // Check if email is being changed and if new email already exists
        if (!distributor.getContactEmail().equals(request.getContactEmail()) && 
            distributorRepository.existsByContactEmail(request.getContactEmail())) {
            throw new IllegalArgumentException("Distributor with email already exists: " + request.getContactEmail());
        }
        
        distributorMapper.updateEntity(distributor, request);
        Distributor updatedDistributor = distributorRepository.save(distributor);
        
        log.info("Distributor updated successfully with ID: {}", updatedDistributor.getId());
        return distributorMapper.toResponseDTO(updatedDistributor);
    }
    
    @Override
    public void deleteDistributor(Long id) {
        log.info("Deleting distributor with ID: {}", id);
        
        if (!distributorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Distributor not found with ID: " + id);
        }
        
        distributorRepository.deleteById(id);
        log.info("Distributor deleted successfully with ID: {}", id);
    }
    
    @Override
    public OrderConfirmationResponse confirmOrderReceived(Long distributorId, OrderConfirmationRequest request) {
        log.info("Processing order confirmation for distributor: {} and order: {}", distributorId, request.getOrderId());
        
        if (orderConfirmationRepository.existsByOrderId(request.getOrderId())) {
            throw new IllegalArgumentException("Order already confirmed: " + request.getOrderId());
        }
        
        OrderConfirmation confirmation = new OrderConfirmation();
        confirmation.setOrderId(request.getOrderId());
        confirmation.setDistributorId(distributorId);
        confirmation.setGdnNumber(request.getGdnNumber());
        confirmation.setStatus(request.getStatus());
        confirmation.setOverallRating(request.getOverallRating());
        confirmation.setFeedback(request.getFeedback());
        confirmation.setRemarks(request.getRemarks());
        
        if (request.getItemConfirmations() != null) {
            List<ItemConfirmationEntity> itemConfirmations = request.getItemConfirmations().stream()
                .map(item -> {
                    ItemConfirmationEntity entity = new ItemConfirmationEntity();
                    entity.setOrderConfirmation(confirmation);
                    entity.setItemId(item.getItemId());
                    entity.setDispatchedQuantity(item.getDispatchedQuantity());
                    entity.setReceivedQuantity(item.getReceivedQuantity());
                    entity.setCondition(item.getCondition());
                    entity.setItemRemarks(item.getItemRemarks());
                    return entity;
                })
                .collect(Collectors.toList());
            confirmation.setItemConfirmations(itemConfirmations);
        }
        
        OrderConfirmation savedConfirmation = orderConfirmationRepository.save(confirmation);
        log.info("Order confirmation saved with ID: {}", savedConfirmation.getId());
        
        return mapToResponse(savedConfirmation);
    }
    
    @Override
    public OrderConfirmationResponse getOrderConfirmation(Long orderId) {
        OrderConfirmation confirmation = orderConfirmationRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order confirmation not found for order: " + orderId));
        return mapToResponse(confirmation);
    }
    
    @Override
    public List<OrderConfirmationResponse> getDistributorConfirmations(Long distributorId) {
        List<OrderConfirmation> confirmations = orderConfirmationRepository.findByDistributorIdOrderByConfirmedAtDesc(distributorId);
        return confirmations.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private OrderConfirmationResponse mapToResponse(OrderConfirmation confirmation) {
        OrderConfirmationResponse response = new OrderConfirmationResponse();
        response.setId(confirmation.getId());
        response.setOrderId(confirmation.getOrderId());
        response.setDistributorId(confirmation.getDistributorId());
        response.setGdnNumber(confirmation.getGdnNumber());
        response.setStatus(confirmation.getStatus());
        response.setOverallRating(confirmation.getOverallRating());
        response.setFeedback(confirmation.getFeedback());
        response.setRemarks(confirmation.getRemarks());
        response.setConfirmedAt(confirmation.getConfirmedAt());
        
        if (confirmation.getItemConfirmations() != null) {
            List<OrderConfirmationResponse.ItemConfirmationResponse> itemResponses = confirmation.getItemConfirmations().stream()
                .map(item -> {
                    OrderConfirmationResponse.ItemConfirmationResponse itemResponse = new OrderConfirmationResponse.ItemConfirmationResponse();
                    itemResponse.setItemId(item.getItemId());
                    itemResponse.setDispatchedQuantity(item.getDispatchedQuantity());
                    itemResponse.setReceivedQuantity(item.getReceivedQuantity());
                    itemResponse.setCondition(item.getCondition());
                    itemResponse.setItemRemarks(item.getItemRemarks());
                    
                    // Get item name from repository
                    itemRepository.findById(item.getItemId())
                        .ifPresent(itemEntity -> itemResponse.setItemName(itemEntity.getName()));
                    
                    return itemResponse;
                })
                .collect(Collectors.toList());
            response.setItemConfirmations(itemResponses);
        }
        
        return response;
    }
}