package com.nector.userservice.interceptors.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryConfirmationRowDto {
    private Long id;
    private String challanNo;
    private String customer;
    private LocalDateTime deliveredDate;
    private String receivedBy;
    private String podStatus;
}
