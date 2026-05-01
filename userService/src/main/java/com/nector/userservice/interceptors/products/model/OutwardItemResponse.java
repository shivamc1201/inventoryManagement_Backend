package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.model.OutwardItemTransaction;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutwardItemResponse {

    private Long id;

    private OutwardItemTransaction.ItemType itemType;

    private OutwardItemTransaction.TransactionType transactionType;

    private String materialCode;

    private String materialName;

    private OutwardItemTransaction.Unit unit;

    private Integer quantity;

    private String comments;

    private java.math.BigDecimal quotedSellingPrice;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
