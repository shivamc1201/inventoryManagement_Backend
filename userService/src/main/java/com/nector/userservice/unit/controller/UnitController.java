package com.nector.userservice.unit.controller;

import com.nector.userservice.unit.dto.UnitRequestDTO;
import com.nector.userservice.unit.dto.UnitResponseDTO;
import com.nector.userservice.unit.dto.UnitUpdateRequestDTO;
import com.nector.userservice.unit.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
@Tag(name = "Unit Master", description = "APIs for managing units")
public class UnitController {
    
    private final UnitService unitService;
    
    @PostMapping
    @Operation(summary = "Create a new unit", description = "Creates a unit based on category (Raw Material or Finished Product)")
    public ResponseEntity<UnitResponseDTO> createUnit(@Valid @RequestBody UnitRequestDTO request) {
        UnitResponseDTO response = unitService.createUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all units", description = "Retrieves all units from both categories")
    public ResponseEntity<List<UnitResponseDTO>> getAllUnits() {
        List<UnitResponseDTO> units = unitService.getAllUnits();
        return ResponseEntity.ok(units);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a unit",
            description = "Updates a unit. The body's category (Raw Material or Finished Product) selects the table, "
                    + "because ids are only unique per category. Only the fields sent are changed.")
    public ResponseEntity<UnitResponseDTO> updateUnit(@PathVariable Long id,
                                                      @Valid @RequestBody UnitUpdateRequestDTO request) {
        return ResponseEntity.ok(unitService.updateUnit(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a unit",
            description = "Soft-deletes a unit (active = false). The category query parameter is required "
                    + "because ids are only unique per category. Fails with 400 if the unit is still in use "
                    + "(e.g. a raw material that is part of a BOM).")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id, @RequestParam String category) {
        unitService.deleteUnit(id, category);
        return ResponseEntity.noContent().build();
    }
}
