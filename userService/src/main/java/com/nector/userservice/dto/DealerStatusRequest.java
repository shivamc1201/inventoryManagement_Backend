package com.nector.userservice.dto;

import com.nector.userservice.model.DealerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DealerStatusRequest {

    @NotNull(message = "Status is required")
    private DealerStatus status;
}
