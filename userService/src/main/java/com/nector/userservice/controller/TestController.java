package com.nector.userservice.controller;

import com.nector.userservice.repository.SaleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "Test", description = "APIs for testing and debugging purposes")
public class TestController {
    
    private final SaleRepository saleRepository;
    
    @GetMapping("/sales-count")
    @Operation(summary = "Get sales count", description = "Returns the total count of sales records for testing purposes")
    @ApiResponse(responseCode = "200", description = "Sales count retrieved successfully")
    public String getSalesCount() {
        long count = saleRepository.count();
        return "Total sales records: " + count;
    }
}