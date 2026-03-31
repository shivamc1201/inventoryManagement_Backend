package com.nector.userservice.ordertracking.controller;

import com.nector.userservice.ordertracking.dto.*;
import com.nector.userservice.ordertracking.service.OrderTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order/tracking")
@RequiredArgsConstructor
@Tag(name = "Order Tracking", description = "APIs for order tracking workflow management")
@Slf4j
public class OrderTrackingController {

    private final OrderTrackingService service;

    /**
     * GET /api/order/tracking?status=pending&search=ORD&page=0&size=50
     * Replaces loadSampleOrders() — primary load call on page init.
     */
    @GetMapping
    @Operation(summary = "Get orders with tracking", description = "Retrieves orders with 11-step workflow tracking")
    public ResponseEntity<OrderTrackingListResponse> list(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(service.listOrders(search, status, page, size));
    }

    /**
     * GET /api/order/tracking/{id}
     * Get one order with full 11-step timeline.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves a single order with full 11-step timeline")
    public ResponseEntity<OrderTrackingDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * GET /api/order/tracking/stats
     * Stats widget: total / pending / completed counts.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get order statistics", description = "Retrieves order statistics for dashboard widgets")
    public ResponseEntity<OrderTrackingStatsDTO> stats() {
        return ResponseEntity.ok(service.getStats());
    }

    /**
     * POST /api/order/tracking
     * Create new order (auto-seeds all 11 steps).
     */
    @PostMapping
    @Operation(summary = "Create new order", description = "Creates a new order with all 11 workflow steps pre-seeded")
    public ResponseEntity<OrderTrackingDTO> create(
            @Valid @RequestBody CreateOrderTrackingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOrder(req));
    }

    /**
     * PUT /api/order/tracking/{orderId}/steps/{stepId}
     * Advance a step (Sales approves, Accounts confirms, etc.)
     */
    @PutMapping("/{orderId}/steps/{stepId}")
    @Operation(summary = "Update workflow step", description = "Updates a specific workflow step status")
    public ResponseEntity<OrderStepDTO> updateStep(
            @PathVariable Long orderId,
            @PathVariable Long stepId,
            @Valid @RequestBody UpdateStepRequest req) {

        return ResponseEntity.ok(service.updateStep(orderId, stepId, req));
    }

    /**
     * PUT /api/order/tracking/{id}/received
     * Distributor confirms delivery (YES/NO) — wires to onOrderReceivedAction().
     */
    @PutMapping("/{id}/received")
    @Operation(summary = "Mark order as received", description = "Distributor confirms order delivery (Step 11)")
    public ResponseEntity<OrderStepDTO> markReceived(
            @PathVariable Long id,
            @Valid @RequestBody OrderReceivedRequest req) {

        return ResponseEntity.ok(service.markReceived(id, req));
    }
}
