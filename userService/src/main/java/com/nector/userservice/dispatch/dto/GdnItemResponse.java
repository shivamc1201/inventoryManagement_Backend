package com.nector.userservice.dispatch.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GdnItemResponse {
    private Long id;
    private Long itemId;
    private String itemDescription;
    private String unitType;
    private Integer noOfUnitsDispatch;
    private BigDecimal weightPerUnit;
}