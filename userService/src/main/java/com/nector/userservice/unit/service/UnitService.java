package com.nector.userservice.unit.service;

import com.nector.userservice.unit.dto.UnitRequestDTO;
import com.nector.userservice.unit.dto.UnitResponseDTO;
import com.nector.userservice.enums.Unit;
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
    
    @Transactional
    public UnitResponseDTO createUnit(UnitRequestDTO request) {
        validateUnitCode(request.getUnitCode(), request.getCategory());
        
        if ("Raw Material".equalsIgnoreCase(request.getCategory())) {
            return createRawMaterialUnit(request);
        } else if ("Finished Product".equalsIgnoreCase(request.getCategory())) {
            return createFinishedProductUnit(request);
        } else {
            throw new IllegalArgumentException("Invalid category: " + request.getCategory());
        }
    }
    
    @Transactional(readOnly = true)
    public List<UnitResponseDTO> getAllUnits() {
        List<UnitResponseDTO> units = new ArrayList<>();
        
        rawProductRepository.findAll().forEach(unit -> 
            units.add(mapToResponse(unit, "Raw Material"))
        );
        
        finishedProductRepository.findAll().forEach(unit -> 
            units.add(mapToResponse(unit, "Finished Product"))
        );
        
        return units;
    }
    
    private void validateUnitCode(String unitCode, String category) {
        boolean exists = "Raw Material".equalsIgnoreCase(category) 
            ? rawProductRepository.existsByUnitCode(unitCode)
            : finishedProductRepository.existsByUnitCode(unitCode);
            
        if (exists) {
            throw new IllegalArgumentException("Unit code already exists: " + unitCode);
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
        unit.setUnitDescription(request.getDescription());
        unit.setUnitStatus(request.getStatus());

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
        dto.setUnitStatus(unit.getUnitStatus());
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
        dto.setPrice(unit.getPrice());
        dto.setQuantity(unit.getQuantity() != null ? new java.math.BigDecimal(unit.getQuantity()) : null);
        dto.setMinimumThreshold(unit.getMinimumThreshold() != null ? new java.math.BigDecimal(unit.getMinimumThreshold()) : null);
        dto.setActive(unit.getActive());
        dto.setCreatedAt(unit.getCreatedAt());
        dto.setUpdatedAt(unit.getUpdatedAt());
        return dto;
    }

}
