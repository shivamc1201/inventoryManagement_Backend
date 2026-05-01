package com.nector.userservice.interceptors.products.service;

import com.nector.userservice.interceptors.products.model.OutwardItemRequest;
import com.nector.userservice.interceptors.products.model.OutwardItemResponse;
import com.nector.userservice.model.OutwardItemTransaction;

import java.util.List;

public interface OutwardItemService {

    OutwardItemResponse createOutwardItem(OutwardItemRequest request);

    OutwardItemResponse getOutwardItemById(Long id);

    List<OutwardItemResponse> getAllOutwardItems();

    List<OutwardItemResponse> getOutwardItemsByType(OutwardItemTransaction.ItemType itemType);

    List<OutwardItemResponse> getOutwardItemsByTransactionType(OutwardItemTransaction.TransactionType transactionType);
}
