package com.nector.userservice.controller;


import com.nector.userservice.dto.ApiResponse;
import com.nector.userservice.dto.sales.SalesRequest;
import com.nector.userservice.dto.sales.SalesResponse;
import com.nector.userservice.service.SalesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Complaint Management", description = "APIs for managing sales roles and permissions")
public class SalesController {

    private final SalesService salesService;

    @PostMapping("/create_complaint")
    public ResponseEntity<ApiResponse<SalesResponse>> createComplaint(
            @Valid @RequestBody SalesRequest request) {
        log.info("Entering createComplaint() for customer: {}, product: {}", request.getCustomerName(), request.getProductName());
        SalesResponse response = salesService.createSalesEntry(request);
        log.info("Exiting createComplaint() - created entry with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint submitted successfully with ID: " + response.getId(), response));
    }


}
