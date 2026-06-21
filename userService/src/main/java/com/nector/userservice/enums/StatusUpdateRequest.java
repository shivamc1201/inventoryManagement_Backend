package com.nector.userservice.enums;

import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {
    @NotNull(message = "Status is required")
    private Boolean status;

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
