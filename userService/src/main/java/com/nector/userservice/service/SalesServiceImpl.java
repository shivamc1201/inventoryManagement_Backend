package com.nector.userservice.service;

import com.nector.userservice.dto.sales.SalesRequest;
import com.nector.userservice.dto.sales.SalesResponse;
import com.nector.userservice.model.SalesEntity;
import com.nector.userservice.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SalesServiceImpl implements SalesService {

    private final SalesRepository salesRepository;


    @Override
    public SalesResponse createSalesEntry(SalesRequest request) {
        log.info("Entering createSalesEntry() - product: {}, customer: {}, quantity: {}", request.getProductName(), request.getCustomerName(), request.getQuantity());
        SalesEntity sales = new SalesEntity();
        sales.setProductName(request.getProductName());
        sales.setQuantity(request.getQuantity());
        sales.setPrice(request.getPrice());
        sales.setCustomerName(request.getCustomerName());
        sales.setSaleDate(request.getSaleDate());

        SalesEntity savedSales = salesRepository.save(sales);
        log.info("Exiting createSalesEntry() - created sales entry with ID: {}", savedSales.getId());
        return mapToResponse(savedSales);
    }

    private SalesResponse mapToResponse(SalesEntity savedSales) {
        SalesResponse response = new SalesResponse();
        response.setId(savedSales.getId());
        response.setProductName(savedSales.getProductName());
        response.setQuantity(savedSales.getQuantity());
        response.setPrice(savedSales.getPrice());
        response.setCustomerName(savedSales.getCustomerName());
        response.setSaleDate(savedSales.getSaleDate());
        response.setCreatedAt(savedSales.getCreatedAt());
        response.setUpdatedAt(savedSales.getUpdatedAt());
        return response;
    }

}
