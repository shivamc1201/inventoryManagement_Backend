package com.nector.userservice.ordertracking.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStepRequest {
    private String status;   // "completed" | "cancelled" | "pending" | "in-progress"
    private String date;     // "yyyy-MM-dd" or null
    private String remarks;
}
