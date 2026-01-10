package com.nector.userservice.controller;

import com.nector.userservice.dto.machinepart.MachinePartRequest;
import com.nector.userservice.dto.machinepart.MachinePartResponse;
import com.nector.userservice.model.MachinePart;
import com.nector.userservice.service.MachinePartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/machine-parts")
@RequiredArgsConstructor
@Slf4j
public class MachinePartController {
    
    private final MachinePartService machinePartService;
    
    @PostMapping("/create-part")
    public ResponseEntity<MachinePartResponse> createMachinePart(@Valid @RequestBody MachinePartRequest request) {
        log.info("Creating new machine part with part number: {}", request.getPartNumber());
        MachinePartResponse response = machinePartService.createMachinePart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/edit-part/{id}")
    public ResponseEntity<MachinePartResponse> updateMachinePart(@PathVariable Long id, @Valid @RequestBody MachinePartRequest request) {
        log.info("Updating machine part with ID: {}", id);
        MachinePartResponse response = machinePartService.updateMachinePart(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/delete-part/{id}")
    public ResponseEntity<Map<String, String>> deleteMachinePart(@PathVariable Long id) {
        log.info("Deleting machine part with ID: {}", id);
        machinePartService.deleteMachinePart(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Machine part deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get-part/{id}")
    public ResponseEntity<MachinePartResponse> getMachinePartById(@PathVariable Long id) {
        log.info("Fetching machine part with ID: {}", id);
        MachinePartResponse response = machinePartService.getMachinePartById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get-all-parts")
    public ResponseEntity<List<MachinePartResponse>> getAllMachineParts() {
        log.info("Fetching all machine parts");
        List<MachinePartResponse> response = machinePartService.getAllMachineParts();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get-parts-by-category/{category}")
    public ResponseEntity<List<MachinePartResponse>> getMachinePartsByCategory(@PathVariable MachinePart.Category category) {
        log.info("Fetching machine parts by category: {}", category);
        List<MachinePartResponse> response = machinePartService.getMachinePartsByCategory(category);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get-parts-by-condition/{condition}")
    public ResponseEntity<List<MachinePartResponse>> getMachinePartsByCondition(@PathVariable MachinePart.Condition condition) {
        log.info("Fetching machine parts by condition: {}", condition);
        List<MachinePartResponse> response = machinePartService.getMachinePartsByCondition(condition);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/update-quantity/{id}")
    public ResponseEntity<MachinePartResponse> updateQuantity(@PathVariable Long id, @RequestBody @Valid QuantityUpdateRequest request) {
        log.info("Updating quantity for machine part ID: {} to quantity: {}", id, request.getQuantity());
        MachinePartResponse response = machinePartService.updateQuantity(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/update-condition/{id}")
    public ResponseEntity<MachinePartResponse> updateCondition(@PathVariable Long id, @RequestBody @Valid ConditionUpdateRequest request) {
        log.info("Updating condition for machine part ID: {} to condition: {}", id, request.getCondition());
        MachinePartResponse response = machinePartService.updateCondition(id, request.getCondition());
        return ResponseEntity.ok(response);
    }
    
    public static class QuantityUpdateRequest {
        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be non-negative")
        private Integer quantity;
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
    
    public static class ConditionUpdateRequest {
        @NotNull(message = "Condition is required")
        private MachinePart.Condition condition;
        
        public MachinePart.Condition getCondition() { return condition; }
        public void setCondition(MachinePart.Condition condition) { this.condition = condition; }
    }
}