package com.nector.userservice.interceptors.salesMapping.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Salesperson-distributor mapping response")
public class MappingResponseDTO {
    
    @Schema(description = "Mapping ID", example = "1")
    private Long id;
    
    @Schema(description = "Salesperson ID", example = "1")
    private Long salespersonId;
    
    @Schema(description = "Salesperson name", example = "John Doe")
    private String salespersonName;
    
    @Schema(description = "Distributor ID", example = "1")
    private Long distributorId;
    
    @Schema(description = "Distributor name", example = "ABC Distribution")
    private String distributorName;
    
    @Schema(description = "Company ID", example = "1")
    private Long companyId;
    
    @Schema(description = "Mapping status", example = "ACTIVE")
    private MappingStatus status;
    
    @Schema(description = "Ledger account ID", example = "1")
    private Long ledgerAccountId;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdOn;
    
    @Schema(description = "Created by", example = "admin")
    private String createdBy;
}