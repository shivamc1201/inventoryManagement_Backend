package com.nector.userservice.interceptors.distributor.service;

import com.nector.userservice.dto.cart.CartResponse;
import com.nector.userservice.interceptors.distributor.dto.DistributorStockResponse;
import com.nector.userservice.interceptors.distributor.model.*;
import com.nector.userservice.interceptors.userLogin.model.LoginRequest;
import jakarta.validation.Valid;

import java.util.List;

public interface DistributorService {
    DistributorResponseDTO createDistributor(DistributorRequestDTO request);
    DistributorResponseDTO getDistributorById(Long id);
    List<DistributorResponseDTO> getAllDistributors();
    DistributorResponseDTO updateDistributor(Long id, DistributorRequestDTO request);
    void deleteDistributor(Long id);
    
    OrderConfirmationResponse confirmOrderReceived(Long distributorId, OrderConfirmationRequest request);
    OrderConfirmationResponse getOrderConfirmation(Long orderId);
    List<OrderConfirmationResponse> getDistributorConfirmations(Long distributorId);

    List<CartResponse> getAllOrdersForDistributor(Long distributorId);

AddressResponse getDistributorAddress(Long distributorId);

    List<DistributorResponseDTO> getDistributorsBySalespersonId(Long salespersonId);

    DistributorStockResponse getDistributorStock(Long distributorId);
}