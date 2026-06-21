package com.nector.userservice.interceptors.products.service;

import com.nector.userservice.interceptors.products.impl.UpdateMachinePart;
import com.nector.userservice.interceptors.products.model.MachinePartRequest;
import com.nector.userservice.interceptors.products.model.PromotionalItemRequest;
import com.nector.userservice.interceptors.products.model.RawProductRequest;
import com.nector.userservice.interceptors.products.model.ScrapItemRequest;
import com.nector.userservice.model.ProductInwardApproval;

import java.util.List;
import java.util.Map;

public interface ProductInwardApprovalService {

    Map<String, Object> createRawProductInwardApproval(Long productId, RawProductRequest request);

    Map<String, Object> createPromotionalItemInwardApproval(Long productId, PromotionalItemRequest request);

    Map<String, Object> createScrapItemInwardApproval(Long productId, ScrapItemRequest request);

    Map<String, Object> createMachinePartInwardApproval(Long productId, UpdateMachinePart request);

    List<ProductInwardApproval> getPendingApprovals();

    Map<String, Object> processApproval(Long approvalId, String action, String adminUsername, String comments);

    boolean hasPendingApproval(ProductInwardApproval.ProductType productType, Long productId);

    boolean hasPendingApprovalByProductCode(ProductInwardApproval.ProductType productType, String productCode);
}
