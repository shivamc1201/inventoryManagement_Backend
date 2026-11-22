package com.nector.userservice.interceptors.content.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateOfferRequest {
    private String title;
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer displayOrder;
}
