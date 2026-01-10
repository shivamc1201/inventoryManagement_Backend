package com.nector.userservice.service;

import com.nector.userservice.dto.machinepart.MachinePartRequest;
import com.nector.userservice.dto.machinepart.MachinePartResponse;
import com.nector.userservice.exception.MachinePartNotFoundException;
import com.nector.userservice.model.MachinePart;
import com.nector.userservice.repository.MachinePartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MachinePartService {
    
    private final MachinePartRepository machinePartRepository;
    
    @Transactional
    public MachinePartResponse createMachinePart(MachinePartRequest request) {
        log.info("Creating machine part with part number: {}", request.getPartNumber());
        
        if (machinePartRepository.existsByPartNumber(request.getPartNumber())) {
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
        part.setCondition(request.getCondition());
        
        MachinePart savedPart = machinePartRepository.save(part);
        log.info("Machine part created successfully with ID: {}", savedPart.getId());
        
        return mapToResponse(savedPart);
    }
    
    @Transactional
    public MachinePartResponse updateMachinePart(Long id, MachinePartRequest request) {
        log.info("Updating machine part with ID: {}", id);
        
        MachinePart part = machinePartRepository.findById(id)
            .orElseThrow(() -> new MachinePartNotFoundException(id));
        
        part.setName(request.getName());
        part.setCategory(request.getCategory());
        part.setVendor(request.getVendor());
        part.setPurchaseDate(request.getPurchaseDate());
        part.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        part.setQuantity(request.getQuantity());
        part.setCondition(request.getCondition());
        
        MachinePart updatedPart = machinePartRepository.save(part);
        log.info("Machine part updated successfully with ID: {}", updatedPart.getId());
        
        return mapToResponse(updatedPart);
    }
    
    @Transactional
    public void deleteMachinePart(Long id) {
        log.info("Soft deleting machine part with ID: {}", id);
        
        MachinePart part = machinePartRepository.findById(id)
            .orElseThrow(() -> new MachinePartNotFoundException(id));
        
        part.setActive(false);
        machinePartRepository.save(part);
        
        log.info("Machine part soft deleted successfully with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public MachinePartResponse getMachinePartById(Long id) {
        log.info("Fetching machine part with ID: {}", id);
        
        MachinePart part = machinePartRepository.findById(id)
            .orElseThrow(() -> new MachinePartNotFoundException(id));
        
        return mapToResponse(part);
    }
    
    @Transactional(readOnly = true)
    public List<MachinePartResponse> getAllMachineParts() {
        log.info("Fetching all machine parts");
        
        return machinePartRepository.findByActiveTrue().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MachinePartResponse> getMachinePartsByCategory(MachinePart.Category category) {
        log.info("Fetching machine parts by category: {}", category);
        
        return machinePartRepository.findByCategory(category).stream()
            .filter(MachinePart::getActive)
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MachinePartResponse> getMachinePartsByCondition(MachinePart.Condition condition) {
        log.info("Fetching machine parts by condition: {}", condition);
        
        return machinePartRepository.findByCondition(condition).stream()
            .filter(MachinePart::getActive)
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public MachinePartResponse updateQuantity(Long id, Integer quantity) {
        log.info("Updating quantity for machine part ID: {} to quantity: {}", id, quantity);
        
        MachinePart part = machinePartRepository.findActiveById(id)
            .orElseThrow(() -> new MachinePartNotFoundException(id));
        
        part.setQuantity(quantity);
        MachinePart updatedPart = machinePartRepository.save(part);
        
        log.info("Quantity updated successfully for machine part ID: {}", id);
        return mapToResponse(updatedPart);
    }
    
    @Transactional
    public MachinePartResponse updateCondition(Long id, MachinePart.Condition condition) {
        log.info("Updating condition for machine part ID: {} to condition: {}", id, condition);
        
        MachinePart part = machinePartRepository.findActiveById(id)
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
        response.setCondition(part.getCondition());
        response.setActive(part.getActive());
        response.setCreatedAt(part.getCreatedAt());
        response.setUpdatedAt(part.getUpdatedAt());
        return response;
    }
}