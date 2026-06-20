package com.nector.userservice.interceptors.distributor.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmationRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "GDN number is required")
    private String gdnNumber;

    @NotNull(message = "Confirmation status is required")
    @Schema(description = "Confirmation status of the order", allowableValues = {"RECEIVED_COMPLETE", "RECEIVED_PARTIAL", "DAMAGED", "REJECTED"})
    private ConfirmationStatus status;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer overallRating;

    private String feedback;
    private String remarks;

    private List<ItemConfirmation> itemConfirmations;

    @Data
    public static class ItemConfirmation {
        @NotNull(message = "Item ID is required")
        private Long itemId;

        @NotNull(message = "SKU is required")
        private String sku;

        @NotNull(message = "Dispatched quantity is required")
        private Integer dispatchedQuantity;

        @NotNull(message = "Received quantity is required")
        private Integer receivedQuantity;

        @NotNull(message = "Item condition is required")
        private ItemCondition condition;

        private String itemRemarks;
    }

    public enum ConfirmationStatus {
        RECEIVED_COMPLETE,
        RECEIVED_PARTIAL,
        DAMAGED,
        REJECTED
    }

    public enum ItemCondition {
        GOOD,
        DAMAGED,
        PARTIAL
    }
}
