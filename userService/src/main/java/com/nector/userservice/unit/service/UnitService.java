package com.nector.userservice.unit.service;

import com.nector.userservice.unit.dto.UnitRequestDTO;
import com.nector.userservice.unit.dto.UnitResponseDTO;
import com.nector.userservice.unit.dto.UnitUpdateRequestDTO;
import com.nector.userservice.enums.Unit;
import com.nector.userservice.exception.FinishedProductNotFoundException;
import com.nector.userservice.exception.RawProductNotFoundException;
import com.nector.userservice.interceptors.products.service.FinishedProductService;
import com.nector.userservice.interceptors.products.service.RawProductService;
import com.nector.userservice.model.FinishedProduct;
import com.nector.userservice.model.RawProduct;
import com.nector.userservice.repository.FinishedProductRepository;
import com.nector.userservice.repository.RawProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitService {
    
    private final RawProductRepository rawProductRepository;
    private final FinishedProductRepository finishedProductRepository;
    private final RawProductService rawProductService;
    private final FinishedProductService finishedProductService;

    private static final String CATEGORY_RAW = "Raw Material";
    private static final String CATEGORY_FINISHED = "Finished Product";

    @Transactional
    public UnitResponseDTO createUnit(UnitRequestDTO request) {
        validateUnitCode(request.getUnitCode(), request.getCategory());
        
        if ("Raw Material".equalsIgnoreCase(request.getCategory())) {
            return createRawMaterialUnit(request);
        } else if ("Finished Product".equalsIgnoreCase(request.getCategory())) {
            return createFinishedProductUnit(request);
        } else {
            throw new IllegalStateException("Invalid category: " + request.getCategory());
        }
    }
    
    @Transactional(readOnly = true)
    public List<UnitResponseDTO> getAllUnits() {
        List<UnitResponseDTO> units = new ArrayList<>();
        
        // Deleted units are soft-deleted (active = false), so they are not listed.
        rawProductRepository.findByActiveTrue().forEach(unit ->
            units.add(mapToResponse(unit, CATEGORY_RAW))
        );

        finishedProductRepository.findByActiveTrue().forEach(unit ->
            units.add(mapToResponse(unit, CATEGORY_FINISHED))
        );

        return units;
    }

    /**
     * Updates a unit. Ids are only unique per table, so the category selects the table.
     * Only fields present in the request are changed.
     */
    @Transactional
    public UnitResponseDTO updateUnit(Long id, UnitUpdateRequestDTO request) {
        if (CATEGORY_RAW.equalsIgnoreCase(request.getCategory())) {
            return updateRawMaterialUnit(id, request);
        } else if (CATEGORY_FINISHED.equalsIgnoreCase(request.getCategory())) {
            return updateFinishedProductUnit(id, request);
        }
        throw new IllegalStateException("Invalid category: " + request.getCategory());
    }

    /**
     * Deletes a unit (soft delete: active = false), reusing the same rules as the product APIs,
     * e.g. a raw material that is used in a BOM cannot be deleted.
     */
    @Transactional
    public void deleteUnit(Long id, String category) {
        if (CATEGORY_RAW.equalsIgnoreCase(category)) {
            rawProductService.deleteRawProduct(id);
        } else if (CATEGORY_FINISHED.equalsIgnoreCase(category)) {
            finishedProductService.deleteFinishedProduct(id);
        } else {
            throw new IllegalStateException("Invalid category: " + category);
        }
    }

    private UnitResponseDTO updateRawMaterialUnit(Long id, UnitUpdateRequestDTO request) {
        RawProduct unit = rawProductRepository.findById(id)
            .filter(p -> !Boolean.FALSE.equals(p.getActive()))
            .orElseThrow(() -> new RawProductNotFoundException(id));

        if (hasText(request.getUnitName())) {
            unit.setUnitName(request.getUnitName().trim());
            unit.setName(request.getUnitName().trim());
        }
        if (hasText(request.getUnitCode()) && !request.getUnitCode().trim().equals(unit.getUnitCode())) {
            if (rawProductRepository.existsByUnitCode(request.getUnitCode().trim())) {
                throw new IllegalStateException("Unit code already exists: " + request.getUnitCode().trim());
            }
            // Only the unit code changes: materialCode is the product's own business code.
            unit.setUnitCode(request.getUnitCode().trim());
        }
        if (hasText(request.getUnitType())) unit.setUnitType(request.getUnitType().trim());
        if (request.getProductSize() != null) unit.setProductSize(request.getProductSize().trim());
        if (request.getDescription() != null) unit.setUnitDescription(request.getDescription());
        if (hasText(request.getStatus())) unit.setUnitStatus(normalizeStatus(request.getStatus()));

        return mapToResponse(rawProductRepository.save(unit), CATEGORY_RAW);
    }

    private UnitResponseDTO updateFinishedProductUnit(Long id, UnitUpdateRequestDTO request) {
        FinishedProduct unit = finishedProductRepository.findById(id)
            .filter(p -> !Boolean.FALSE.equals(p.getActive()))
            .orElseThrow(() -> new FinishedProductNotFoundException(id));

        if (hasText(request.getName())) unit.setName(request.getName().trim());
        if (hasText(request.getSku()) && !request.getSku().trim().equals(unit.getSku())) {
            if (finishedProductRepository.existsBySku(request.getSku().trim())) {
                throw new IllegalStateException("SKU already exists: " + request.getSku().trim());
            }
            unit.setSku(request.getSku().trim());
        }
        if (request.getPrice() != null) unit.setPrice(request.getPrice());
        if (request.getQuantity() != null) unit.setQuantity(request.getQuantity());
        if (request.getMinimumThreshold() != null) unit.setMinimumThreshold(request.getMinimumThreshold());
        if (request.getDescription() != null) {
            unit.setDescription(request.getDescription());
            unit.setUnitDescription(request.getDescription());
        }
        if (hasText(request.getStatus())) unit.setUnitStatus(normalizeStatus(request.getStatus()));

        return mapToResponse(finishedProductRepository.save(unit), CATEGORY_FINISHED);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String normalizeStatus(String status) {
        String s = status.trim().toUpperCase();
        if (!s.equals("ACTIVE") && !s.equals("INACTIVE")) {
            throw new IllegalStateException("Invalid status: " + status + " (expected ACTIVE or INACTIVE)");
        }
        return s;
    }

    /** Unit status shown to clients: the explicit unit status, else derived from the active flag. */
    private static String statusOf(String unitStatus, Boolean active) {
        if (unitStatus != null && !unitStatus.isBlank()) return unitStatus;
        return Boolean.FALSE.equals(active) ? "INACTIVE" : "ACTIVE";
    }
    
    private void validateUnitCode(String unitCode, String category) {
        boolean exists = "Raw Material".equalsIgnoreCase(category) 
            ? rawProductRepository.existsByUnitCode(unitCode)
            : finishedProductRepository.existsByUnitCode(unitCode);
            
        if (exists) {
            // IllegalStateException is returned to the client as 400 with this message
            throw new IllegalStateException("Unit code already exists: " + unitCode);
        }
        // A finished product's code is also its SKU, which must be unique
        if (CATEGORY_FINISHED.equalsIgnoreCase(category) && finishedProductRepository.existsBySku(unitCode)) {
            throw new IllegalStateException("SKU already exists: " + unitCode);
        }
    }

    private UnitResponseDTO createRawMaterialUnit(UnitRequestDTO request) {
        RawProduct unit = new RawProduct();
        unit.setName(request.getUnitName());
        unit.setMaterialCode(request.getUnitCode());
        unit.setUnit(Unit.PIECES);
        unit.setUnitType(request.getUnitType());
        unit.setProductSize(request.getProductSize());
        unit.setUnitName(request.getUnitName());
        unit.setUnitCode(request.getUnitCode());
        unit.setUnitDescription(request.getDescription());
        unit.setUnitStatus(request.getStatus());

        RawProduct saved = rawProductRepository.save(unit);
        return mapToResponse(saved, "Raw Material");
    }

    private UnitResponseDTO createFinishedProductUnit(UnitRequestDTO request) {
        FinishedProduct unit = new FinishedProduct();
        unit.setName(request.getUnitName());
        unit.setSku(request.getUnitCode());
        unit.setUnitType(request.getUnitType());
        unit.setProductSize(request.getProductSize());
        unit.setUnitName(request.getUnitName());
        unit.setUnitCode(request.getUnitCode());
        unit.setDescription(request.getDescription());
        unit.setUnitDescription(request.getDescription());
        unit.setUnitStatus(request.getStatus());
        unit.setPrice(request.getPrice());
        unit.setQuantity(request.getQuantity());
        unit.setMinimumThreshold(request.getMinimumThreshold());

        FinishedProduct saved = finishedProductRepository.save(unit);
        return mapToResponse(saved, "Finished Product");
    }


    private UnitResponseDTO mapToResponse(RawProduct unit, String category) {
        UnitResponseDTO dto = new UnitResponseDTO();
        dto.setId(unit.getId());
        dto.setCategory(category);
        dto.setName(unit.getName());
        dto.setMaterialCode(unit.getMaterialCode());
        dto.setUnit(unit.getUnit() != null ? unit.getUnit().name() : null);
        dto.setUnitType(unit.getUnitType());
        dto.setProductSize(unit.getProductSize());
        dto.setUnitName(unit.getUnitName());
        dto.setUnitCode(unit.getUnitCode());
        dto.setUnitDescription(unit.getUnitDescription());
        dto.setDescription(unit.getUnitDescription());
        dto.setUnitStatus(unit.getUnitStatus());
        dto.setStatus(statusOf(unit.getUnitStatus(), unit.getActive()));
        dto.setQuantity(unit.getQuantity());
        dto.setMinimumThreshold(unit.getMinimumThreshold());
        dto.setActive(unit.getActive());
        dto.setCreatedAt(unit.getCreatedAt());
        dto.setUpdatedAt(unit.getUpdatedAt());
        return dto;
    }

    private UnitResponseDTO mapToResponse(FinishedProduct unit, String category) {
        UnitResponseDTO dto = new UnitResponseDTO();
        dto.setId(unit.getId());
        dto.setCategory(category);
        dto.setName(unit.getName());
        dto.setSku(unit.getSku());
        dto.setDescription(unit.getDescription());
        dto.setUnitType(unit.getUnitType());
        dto.setProductSize(unit.getProductSize());
        dto.setUnitName(unit.getUnitName());
        dto.setUnitCode(unit.getUnitCode());
        dto.setUnitDescription(unit.getUnitDescription());
        dto.setUnitStatus(unit.getUnitStatus());
        dto.setStatus(statusOf(unit.getUnitStatus(), unit.getActive()));
        dto.setPrice(unit.getPrice());
        dto.setQuantity(unit.getQuantity() != null ? new java.math.BigDecimal(unit.getQuantity()) : null);
        dto.setMinimumThreshold(unit.getMinimumThreshold() != null ? new java.math.BigDecimal(unit.getMinimumThreshold()) : null);
        dto.setActive(unit.getActive());
        dto.setCreatedAt(unit.getCreatedAt());
        dto.setUpdatedAt(unit.getUpdatedAt());
        return dto;
    }

}
