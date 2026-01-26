package com.nector.userservice.interceptors.products.service;

import com.nector.userservice.interceptors.products.model.MachinePartRequest;
import com.nector.userservice.interceptors.products.model.MachinePartResponse;
import com.nector.userservice.model.MachinePart;

import java.util.List;

public interface MachinePartService {
    
    MachinePartResponse createMachinePart(MachinePartRequest request);
    
    MachinePartResponse updateMachinePart(Long id, MachinePartRequest request);
    
    void deleteMachinePart(Long id);
    
    MachinePartResponse getMachinePartById(Long id);
    
    List<MachinePartResponse> getAllMachineParts();
    
    List<MachinePartResponse> getMachinePartsByCategory(MachinePart.Category category);
    
    List<MachinePartResponse> getMachinePartsByCondition(MachinePart.Condition condition);
    
    MachinePartResponse updateQuantity(Long id, Integer quantity);
    
    MachinePartResponse updateCondition(Long id, MachinePart.Condition condition);
}