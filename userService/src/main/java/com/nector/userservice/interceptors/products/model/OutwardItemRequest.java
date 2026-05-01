package com.nector.userservice.interceptors.products.model;

import com.nector.userservice.model.OutwardItemTransaction;
import lombok.Data;

@Data
public class OutwardItemRequest {

    private OutwardItemTransaction.ItemType itemType;

    private OutwardItemTransaction.TransactionType transactionType;

    private String materialCode;

    private String materialName;

    private OutwardItemTransaction.Unit unit;

    private Integer quantity;

    private String comments;

    private java.math.BigDecimal quotedSellingPrice;
}
