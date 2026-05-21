package com.nector.userservice.interceptors.products.impl;

import com.nector.userservice.exception.MachinePartNotFoundException;
import com.nector.userservice.interceptors.products.model.MachinePartRequest;
import com.nector.userservice.interceptors.products.model.MachinePartResponse;
import com.nector.userservice.interceptors.products.model.ProductPriceHistory;
import com.nector.userservice.interceptors.products.service.MachinePartService;
import com.nector.userservice.interceptors.products.service.ProductPriceHistoryService;
import com.nector.userservice.model.MachinePart;
import com.nector.userservice.repository.MachinePartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MachinePartServiceImpl implements MachinePartService {
    
    private final MachinePartRepository machinePartRepository;
    private final ProductPriceHistoryService productPriceHistoryService;
    
    @Override
    @Transactional
    public MachinePartResponse createMachinePart(MachinePartRequest request) {
        log.info("Creating machine part with part number: {}", request.getPartNumber());

        if (request.getPartNumber() != null && machinePartRepository.existsByPartNumber(request.getPartNumber())) {
            throw new DataIntegrityViolationException("Machine part with part number " + request.getPartNumber() + " already exists");
        }

        MachinePart part = new MachinePart();
        part.setName(request.getName());
        part.setPartNumber(request.getPartNumber());
        part.setCategory(request.getCategory());
        part.setVendor(request.getVendor());
        part.setPurchaseDate(request.getPurchaseDate());
        part.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        part.setQuantity(request.getQuantity());
        part.setUnit(request.getUnit());
        part.setPrice(request.getPrice());
        part.setCondition(request.getCondition());
        part.setHsn(request.getHsn());
        part.setTaxRate(parseTaxRate(request.getTaxRateCode()));
        if (request.getStatus() != null) part.setStatus(request.getStatus());

        MachinePart savedPart = machinePartRepository.save(part);
        log.info("Machine part created successfully with ID: {}", savedPart.getId());
        
        return mapToResponse(savedPart);
    }

    @Override
    @Transactional
    public MachinePartResponse updateMachinePart(Long id, UpdateMachinePart request) {
        log.info("Updating machine part with ID: {}", id);

        MachinePart part = machinePartRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new MachinePartNotFoundException(id));

        BigDecimal oldPrice = part.getPrice();

        part.setName(request.getName());
        part.setCategory(request.getCategory());
        part.setVendor(request.getVendor());
        part.setPurchaseDate(request.getPurchaseDate());
        part.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        part.setQuantity(request.getQuantity());
        part.setUnit(request.getUnit());
        part.setPrice(request.getPrice());
        part.setCondition(request.getCondition());
        part.setHsn(request.getHsn());
        part.setTaxRate(request.getTaxRate());

        MachinePart updatedPart = machinePartRepository.save(part);
        log.info("Machine part updated successfully with ID: {}", updatedPart.getId());

        BigDecimal newPrice = updatedPart.getPrice();
        if (oldPrice != null && newPrice != null && oldPrice.compareTo(newPrice) != 0) {
            productPriceHistoryService.record(ProductPriceHistory.ProductType.MACHINE_PART,
                    updatedPart.getId(), updatedPart.getName(), updatedPart.getPartNumber(),
                    oldPrice, newPrice);
        }

        return mapToResponse(updatedPart);
    }

    @Override
    @Transactional
    public void deleteMachinePart(Long id) {
        log.info("Soft deleting machine part with ID: {}", id);
        
        MachinePart part = machinePartRepository.findById(id)
            .orElseThrow(() -> new MachinePartNotFoundException(id));
        
        part.setActive(false);
        machinePartRepository.save(part);
        
        log.info("Machine part soft deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public MachinePartResponse getMachinePartById(Long id) {
        log.info("Fetching machine part with ID: {}", id);
        
        MachinePart part = machinePartRepository.findById(id)
            .orElseThrow(() -> new MachinePartNotFoundException(id));
        
        return mapToResponse(part);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MachinePartResponse> getAllMachineParts() {
        log.info("Fetching all machine parts");
        
        return machinePartRepository.findByActiveTrue().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MachinePartResponse> getMachinePartsByCategory(MachinePart.Category category) {
        log.info("Fetching machine parts by category: {}", category);
        
        return machinePartRepository.findByCategory(category).stream()
            .filter(p -> Boolean.TRUE.equals(p.getActive()))
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MachinePartResponse> getMachinePartsByCondition(MachinePart.Condition condition) {
        log.info("Fetching machine parts by condition: {}", condition);
        
        return machinePartRepository.findByCondition(condition).stream()
            .filter(p -> Boolean.TRUE.equals(p.getActive()))
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public MachinePartResponse updateQuantity(Long id, Integer quantity) {
        log.info("Updating quantity for machine part ID: {} to quantity: {}", id, quantity);
        
        MachinePart part = machinePartRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new MachinePartNotFoundException(id));
        
        part.setQuantity(quantity);
        MachinePart updatedPart = machinePartRepository.save(part);
        
        log.info("Quantity updated successfully for machine part ID: {}", id);
        return mapToResponse(updatedPart);
    }
    
    @Override
    @Transactional
    public MachinePartResponse updateCondition(Long id, MachinePart.Condition condition) {
        log.info("Updating condition for machine part ID: {} to condition: {}", id, condition);
        
        MachinePart part = machinePartRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new MachinePartNotFoundException(id));
        
        part.setCondition(condition);
        MachinePart updatedPart = machinePartRepository.save(part);
        
        log.info("Condition updated successfully for machine part ID: {}", id);
        return mapToResponse(updatedPart);
    }
    
    private MachinePartResponse mapToResponse(MachinePart part) {
        MachinePartResponse response = new MachinePartResponse();
        response.setId(part.getId());
        response.setName(part.getName());
        response.setPartNumber(part.getPartNumber());
        response.setCategory(part.getCategory());
        response.setVendor(part.getVendor());
        response.setPurchaseDate(part.getPurchaseDate());
        response.setWarrantyExpiryDate(part.getWarrantyExpiryDate());
        response.setQuantity(part.getQuantity());
        response.setUnit(part.getUnit());
        response.setPrice(part.getPrice());
        response.setCondition(part.getCondition());
        response.setActive(part.getActive());
        response.setCreatedAt(part.getCreatedAt());
        response.setUpdatedAt(part.getUpdatedAt());
        response.setStatus(part.getStatus());
        return response;
    }

    @Override
    @Transactional
    public MachinePartResponse updateByPartNumber(String partNumber, MachinePartRequest request) {
        log.info("Updating machine part by part number: {}", partNumber);

        MachinePart part = machinePartRepository.findByPartNumber(partNumber)
                .orElseThrow(() -> new MachinePartNotFoundException(0L));

        BigDecimal oldPriceByPart = part.getPrice();

        if (request.getName() != null) part.setName(request.getName());
        if (request.getCategory() != null) part.setCategory(request.getCategory());
        if (request.getVendor() != null) part.setVendor(request.getVendor());
        if (request.getPurchaseDate() != null) part.setPurchaseDate(request.getPurchaseDate());
        if (request.getWarrantyExpiryDate() != null) part.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        if (request.getQuantity() != null) part.setQuantity(request.getQuantity());
        if (request.getUnit() != null) part.setUnit(request.getUnit());
        if (request.getPrice() != null) part.setPrice(request.getPrice());
        if (request.getCondition() != null) part.setCondition(request.getCondition());
        if (request.getHsn() != null) part.setHsn(request.getHsn());
        if (request.getTaxRateCode() != null) part.setTaxRate(parseTaxRate(request.getTaxRateCode()));
        if (request.getStatus() != null) part.setStatus(request.getStatus());

        MachinePart updatedPart = machinePartRepository.save(part);
        log.info("Machine part updated by part number: {}", partNumber);

        BigDecimal newPriceByPart = updatedPart.getPrice();
        if (request.getPrice() != null && oldPriceByPart != null
                && oldPriceByPart.compareTo(newPriceByPart) != 0) {
            productPriceHistoryService.record(ProductPriceHistory.ProductType.MACHINE_PART,
                    updatedPart.getId(), updatedPart.getName(), updatedPart.getPartNumber(),
                    oldPriceByPart, newPriceByPart);
        }

        return mapToResponse(updatedPart);
    }

    private java.math.BigDecimal parseTaxRate(String taxRateCode) {
        if (taxRateCode == null || taxRateCode.trim().isEmpty()) {
            return null;
        }
        try {
            String cleanRate = taxRateCode.replace("%", "").trim();
            return new java.math.BigDecimal(cleanRate);
        } catch (NumberFormatException e) {
            log.warn("Invalid tax rate code: {}", taxRateCode);
            return null;
        }
    }
}