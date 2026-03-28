package com.nector.userservice.dispatch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request for rejecting GDN")
public class GdnRejectionRequest {
    
    @Schema(description = "Reason for rejecting the GDN", example = "Inventory not available")
    private String reason;
}
