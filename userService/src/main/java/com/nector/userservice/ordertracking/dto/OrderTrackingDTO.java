package com.nector.userservice.ordertracking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingDTO {
    private Long id;
    private String orderNumber;
    private String distributorName;
    private Long distributorId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate orderDate;

    private BigDecimal totalAmount;
    private List<OrderStepDTO> steps;   // always 11 items
}
