package com.nector.userservice.interceptors.salesMapping.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request to create salesperson-distributor mapping")
public class CreateMappingRequestDTO {
    
    @NotNull(message = "Salesperson ID is required")
    @Schema(description = "Salesperson ID", example = "1")
    private Long salespersonId;
    
    @NotNull(message = "Distributor ID is required")
    @Schema(description = "Distributor ID", example = "1")
    private Long distributorId;
    
    @NotNull(message = "Company ID is required")
    @Schema(description = "Company ID", example = "1")
    private Long companyId;
}