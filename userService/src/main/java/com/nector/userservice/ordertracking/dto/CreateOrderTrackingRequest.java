package com.nector.userservice.ordertracking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderTrackingRequest {
    @NotBlank
    private String orderNumber;
    
    @NotNull
    private Long distributorId;
    
    @NotBlank
    private String distributorName;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate orderDate;
    
    @NotNull
    @Positive
    private BigDecimal totalAmount;
    
    private Long createdBy;

    private Long salespersonId;

    private String deliveryBy;
}
