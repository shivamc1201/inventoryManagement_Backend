package com.nector.userservice.controller;

import com.nector.userservice.dto.OrderResponse;
import com.nector.userservice.dto.OrderSummaryResponse;
import com.nector.userservice.dto.SalesPersonCreateRequest;
import com.nector.userservice.dto.SalesPersonResponse;
import com.nector.userservice.dto.SalesPersonUpdateRequest;
import com.nector.userservice.enums.OrderStatus;
import com.nector.userservice.enums.SalesRole;
import com.nector.userservice.service.OrderService;
import com.nector.userservice.service.SalesPersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sales-hierarchy")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales Hierarchy Management", description = "Complete API for sales hierarchy and orders management")
public class SalesHierarchyController {

    private final SalesPersonService salesPersonService;
    private final OrderService orderService;

    @Operation(summary = "Get all salespersons", description = "Retrieve all salespersons in the hierarchy")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/list")
    public ResponseEntity<List<SalesPersonResponse>> getAllSalesPersons() {
        log.info("Getting all salespersons");
        List<SalesPersonResponse> salesPersons = salesPersonService.getAllSalesPersons();
        return ResponseEntity.ok(salesPersons);
    }

    @Operation(summary = "Create a new salesperson", description = "Create a new salesperson with role hierarchy validation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "409", description = "Conflict"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/create")
    public ResponseEntity<SalesPersonResponse> createSalesPerson(
            @Valid @RequestBody SalesPersonCreateRequest request) throws BadRequestException {
        log.info("Creating salesperson: {}", request.getEmployeeRollNo());
        SalesPersonResponse created = salesPersonService.createSalesPerson(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a salesperson", description = "Update an existing salesperson")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "409", description = "Conflict"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<SalesPersonResponse> updateSalesPerson(
            @Parameter(description = "Salesperson ID") @PathVariable Long id,
            @Valid @RequestBody SalesPersonUpdateRequest request) throws BadRequestException {
        log.info("Updating salesperson with ID: {}", id);
        SalesPersonResponse updated = salesPersonService.updateSalesPerson(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a salesperson", description = "Delete a salesperson (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted"),
        @ApiResponse(responseCode = "400", description = "Cannot delete"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteSalesPerson(
            @Parameter(description = "Salesperson ID") @PathVariable Long id) {
        log.info("Deleting salesperson with ID: {}", id);
        salesPersonService.deleteSalesPerson(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get salesperson by ID", description = "Retrieve a specific salesperson")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/details/{id}")
    public ResponseEntity<SalesPersonResponse> getSalesPersonById(
            @Parameter(description = "Salesperson ID") @PathVariable Long id) {
        log.info("Getting salesperson with ID: {}", id);
        SalesPersonResponse salesPerson = salesPersonService.getSalesPersonById(id);
        return ResponseEntity.ok(salesPerson);
    }

    @Operation(summary = "Search salespersons", description = "Search salespersons by name, employee code, or zone")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/search")
    public ResponseEntity<List<SalesPersonResponse>> searchSalesPersons(
            @Parameter(description = "Search term") @RequestParam String search) {
        log.info("Searching salespersons with term: {}", search);
        List<SalesPersonResponse> salesPersons = salesPersonService.searchSalesPersons(search);
        return ResponseEntity.ok(salesPersons);
    }

    @Operation(summary = "Get salespersons by role type", description = "Retrieve salespersons filtered by their role type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "400", description = "Invalid role type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/by-role")
    public ResponseEntity<List<SalesPersonResponse>> getSalesPersonsByRole(
            @Parameter(description = "Sales role type") @RequestParam SalesRole role) {
        log.info("Getting salespersons by role type: {}", role);
        List<SalesPersonResponse> salesPersons = salesPersonService.getSalesPersonsByRole(role);
        return ResponseEntity.ok(salesPersons);
    }

    // ==================== ROLES MANAGEMENT ====================

    @Operation(summary = "Get all sales roles", description = "Get available sales roles for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/roles/dropdown")
    public ResponseEntity<List<Map<String, String>>> getSalesRoles() {
        log.info("Getting all sales roles");
        List<Map<String, String>> roles = Arrays.stream(SalesRole.values())
                .map(role -> Map.of(
                    "value", role.name(),
                    "label", role.getLabel(),
                    "shortLabel", role.getShortLabel(),
                    "icon", role.getIcon()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    // ==================== ORDERS MANAGEMENT ====================

    @Operation(summary = "Get all orders", description = "Retrieve all orders with salesperson and distributor details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/orders/list")
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @Parameter(description = "Filter by order status") 
            @RequestParam(required = false) OrderStatus status,
            
            @Parameter(description = "Filter by salesperson ID") 
            @RequestParam(required = false) Long salespersonId,
            
            @Parameter(description = "Filter by distributor ID") 
            @RequestParam(required = false) Long distributorId,
            
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            
            @Parameter(description = "End date (ISO format)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        log.info("Getting orders with filters - status: {}, salespersonId: {}, distributorId: {}, dateFrom: {}, dateTo: {}", 
                status, salespersonId, distributorId, dateFrom, dateTo);
        
        List<OrderResponse> orders = orderService.getOrdersWithFilters(status, salespersonId, distributorId, dateFrom, dateTo);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get order summary", description = "Get order summary statistics (total count, total amount by zone/role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/orders/summary")
    public ResponseEntity<OrderSummaryResponse> getOrderSummary(
            @Parameter(description = "Filter by status") 
            @RequestParam(required = false) OrderStatus status) {
        
        log.info("Getting order summary - status: {}", status);
        
        OrderSummaryResponse summary = orderService.getOrderSummary(status);
        return ResponseEntity.ok(summary);
    }
}
