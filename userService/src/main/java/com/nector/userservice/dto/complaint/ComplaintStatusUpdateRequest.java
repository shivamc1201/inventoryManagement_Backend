package com.nector.userservice.dto.complaint;

import com.nector.userservice.model.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintStatusUpdateRequest {
    
    @NotNull(message = "Status is required")
    private ComplaintStatus status;
}