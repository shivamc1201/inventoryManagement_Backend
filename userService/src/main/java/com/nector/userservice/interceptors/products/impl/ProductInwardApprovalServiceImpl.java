package com.nector.userservice.interceptors.products.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nector.userservice.exception.MachinePartNotFoundException;
import com.nector.userservice.exception.RawProductNotFoundException;
import com.nector.userservice.interceptors.products.model.PromotionalItemRequest;
import com.nector.userservice.interceptors.products.model.RawProductRequest;
import com.nector.userservice.interceptors.products.model.ScrapItemRequest;
import com.nector.userservice.interceptors.products.service.MachinePartService;
import com.nector.userservice.interceptors.products.service.ProductInwardApprovalService;
import com.nector.userservice.interceptors.products.service.PromotionalItemService;
import com.nector.userservice.interceptors.products.service.RawProductService;
import com.nector.userservice.interceptors.products.service.ScrapItemService;
import com.nector.userservice.model.MachinePart;
import com.nector.userservice.model.ProductInwardApproval;
import com.nector.userservice.model.PromotionalItem;
import com.nector.userservice.model.RawProduct;
import com.nector.userservice.model.ScrapItem;
import com.nector.userservice.repository.MachinePartRepository;
import com.nector.userservice.repository.ProductInwardApprovalRepository;
import com.nector.userservice.repository.PromotionalItemRepository;
import com.nector.userservice.repository.RawProductRepository;
import com.nector.userservice.repository.ScrapItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductInwardApprovalServiceImpl implements ProductInwardApprovalService {

    private final ProductInwardApprovalRepository approvalRepository;
    private final RawProductRepository rawProductRepository;
    private final PromotionalItemRepository promotionalItemRepository;
    private final ScrapItemRepository scrapItemRepository;
    private final MachinePartRepository machinePartRepository;
    private final RawProductService rawProductService;
    private final PromotionalItemService promotionalItemService;
    private final ScrapItemService scrapItemService;
    private final MachinePartService machinePartService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Map<String, Object> createRawProductInwardApproval(Long productId, RawProductRequest request) {
        RawProduct product = rawProductRepository.findById(productId)
                .orElseThrow(() -> new RawProductNotFoundException(productId));

        ProductInwardApproval approval = buildApproval(
                ProductInwardApproval.ProductType.RAW_PRODUCT, productId, product.getMaterialCode(), request);
        ProductInwardApproval saved = approvalRepository.save(approval);
        log.info("Created inward approval {} for raw product {}", saved.getId(), productId);

        return buildResponse(saved.getId(), productId, product.getMaterialCode(), "RAW_PRODUCT");
    }

    @Override
    @Transactional
    public Map<String, Object> createPromotionalItemInwardApproval(Long productId, PromotionalItemRequest request) {
        PromotionalItem product = promotionalItemRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Promotional item not found: " + productId));

        ProductInwardApproval approval = buildApproval(
                ProductInwardApproval.ProductType.PROMOTIONAL_ITEM, productId, product.getItemCode(), request);
        ProductInwardApproval saved = approvalRepository.save(approval);
        log.info("Created inward approval {} for promotional item {}", saved.getId(), productId);

        return buildResponse(saved.getId(), productId, product.getItemCode(), "PROMOTIONAL_ITEM");
    }

    @Override
    @Transactional
    public Map<String, Object> createScrapItemInwardApproval(Long productId, ScrapItemRequest request) {
        ScrapItem product = scrapItemRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Scrap item not found: " + productId));

        ProductInwardApproval approval = buildApproval(
                ProductInwardApproval.ProductType.SCRAP_ITEM, productId, product.getItemCode(), request);
        ProductInwardApproval saved = approvalRepository.save(approval);
        log.info("Created inward approval {} for scrap item {}", saved.getId(), productId);

        return buildResponse(saved.getId(), productId, product.getItemCode(), "SCRAP_ITEM");
    }

    @Override
    @Transactional
    public Map<String, Object> createMachinePartInwardApproval(Long productId, UpdateMachinePart request) {
        MachinePart product = machinePartRepository.findById(productId)
                .orElseThrow(() -> new MachinePartNotFoundException(productId));

        ProductInwardApproval approval = buildApproval(
                ProductInwardApproval.ProductType.MACHINE_PART, productId, product.getPartNumber(), request);
        ProductInwardApproval saved = approvalRepository.save(approval);
        log.info("Created inward approval {} for machine part {}", saved.getId(), productId);

        return buildResponse(saved.getId(), productId, product.getPartNumber(), "MACHINE_PART");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductInwardApproval> getPendingApprovals() {
        return approvalRepository.findByApprovalStatus("PENDING");
    }

    @Override
    @Transactional
    public Map<String, Object> processApproval(Long approvalId, String action, String adminUsername, String comments) {
        ProductInwardApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found: " + approvalId));

        if (!"PENDING".equals(approval.getApprovalStatus())) {
            throw new RuntimeException("Approval is already " + approval.getApprovalStatus());
        }

        Map<String, Object> response = new HashMap<>();

        if ("APPROVE".equals(action)) {
            applyProductUpdate(approval);
            approval.setApprovalStatus("APPROVED");
            response.put("message", "Inward approved and product updated successfully.");
        } else if ("REJECT".equals(action)) {
            approval.setApprovalStatus("REJECTED");
            response.put("message", "Inward request rejected.");
        } else {
            throw new RuntimeException("Invalid action: " + action + ". Must be APPROVE or REJECT.");
        }

        approval.setReviewedBy(adminUsername);
        approval.setReviewedOn(LocalDateTime.now());
        approval.setReviewComments(comments);
        approvalRepository.save(approval);

        response.put("approvalId", approvalId);
        response.put("action", action);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPendingApproval(ProductInwardApproval.ProductType productType, Long productId) {
        return approvalRepository.existsByProductTypeAndProductIdAndApprovalStatus(productType, productId, "PENDING");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPendingApprovalByProductCode(ProductInwardApproval.ProductType productType, String productCode) {
        return approvalRepository.existsByProductTypeAndProductCodeAndApprovalStatus(productType, productCode, "PENDING");
    }

    private void applyProductUpdate(ProductInwardApproval approval) {
        switch (approval.getProductType()) {
            case RAW_PRODUCT -> {
                RawProductRequest request = deserialize(approval.getRequestPayload(), RawProductRequest.class);
                rawProductService.updateRawProduct(approval.getProductId(), request);
            }
            case PROMOTIONAL_ITEM -> {
                PromotionalItemRequest request = deserialize(approval.getRequestPayload(), PromotionalItemRequest.class);
                promotionalItemService.updatePromotionalItem(approval.getProductId(), request);
            }
            case SCRAP_ITEM -> {
                ScrapItemRequest request = deserialize(approval.getRequestPayload(), ScrapItemRequest.class);
                scrapItemService.updateScrapItem(approval.getProductId(), request);
            }
            case MACHINE_PART -> {
                UpdateMachinePart request = deserialize(approval.getRequestPayload(), UpdateMachinePart.class);
                machinePartService.updateMachinePart(approval.getProductId(), request);
            }
        }
    }

    private ProductInwardApproval buildApproval(ProductInwardApproval.ProductType type, Long productId,
                                                 String productCode, Object request) {
        ProductInwardApproval approval = new ProductInwardApproval();
        approval.setProductType(type);
        approval.setProductId(productId);
        approval.setProductCode(productCode);
        approval.setRequestPayload(serialize(request));
        approval.setApprovalStatus("PENDING");
        return approval;
    }

    private Map<String, Object> buildResponse(Long approvalId, Long productId, String productCode, String productType) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Inward request submitted. Pending admin approval.");
        response.put("approvalId", approvalId);
        response.put("productId", productId);
        response.put("productCode", productCode);
        response.put("productType", productType);
        return response;
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request payload", e);
        }
    }

    private <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize request payload", e);
        }
    }
}
