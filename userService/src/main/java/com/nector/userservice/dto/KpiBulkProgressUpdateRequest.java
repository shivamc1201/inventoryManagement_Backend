package com.nector.userservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiBulkProgressUpdateRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotEmpty(message = "At least one assignment update is required")
    @Valid
    private List<KpiProgressUpdateItem> assignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiProgressUpdateItem {
        
        @NotNull(message = "Assignment ID is required")
        private Long assignmentId;

        @NotNull(message = "Achieved value is required")
        private java.math.BigDecimal achievedValue;
    }
}
