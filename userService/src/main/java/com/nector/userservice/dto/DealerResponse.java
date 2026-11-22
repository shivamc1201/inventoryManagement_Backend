package com.nector.userservice.dto;

import com.nector.userservice.model.DealerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealerResponse {

    private Long id;
    private Long distributorId;
    private String distributorName;
    private Long salespersonId;
    private String salespersonName;
    private String fullName;
    private String phone;
    private String address;
    private Boolean isActive;
    private DealerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
