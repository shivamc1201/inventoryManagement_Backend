package com.nector.userservice.controller;

import com.nector.userservice.dto.hrmaster.DepartmentRequest;
import com.nector.userservice.dto.hrmaster.DesignationRequest;
import com.nector.userservice.model.Department;
import com.nector.userservice.model.Designation;
import com.nector.userservice.service.HrMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin/hr-master")
@RequiredArgsConstructor
@Tag(name = "HR Master", description = "Department and Designation management (Super Admin only)")
public class HrMasterController {
    
    private final HrMasterService hrMasterService;
    
    @PostMapping("/departments")
    @Operation(summary = "Create department", description = "Creates a new department (Super Admin only)")
    @ApiResponse(responseCode = "200", description = "Department created successfully")
    public ResponseEntity<Department> createDepartment(
            @Valid @RequestBody DepartmentRequest request,
            @RequestHeader("SuperAdmin-Username") String superAdminUsername) {
        Department department = hrMasterService.createDepartment(request, superAdminUsername);
        return ResponseEntity.ok(department);
    }
    
    @PostMapping("/designations")
    @Operation(summary = "Create designation", description = "Creates a new designation (Super Admin only)")
    @ApiResponse(responseCode = "200", description = "Designation created successfully")
    public ResponseEntity<Designation> createDesignation(
            @Valid @RequestBody DesignationRequest request,
            @RequestHeader("SuperAdmin-Username") String superAdminUsername) {
        Designation designation = hrMasterService.createDesignation(request, superAdminUsername);
        return ResponseEntity.ok(designation);
    }
    
    @GetMapping("/departments")
    @Operation(summary = "Get all departments", description = "Retrieves all active departments")
    @ApiResponse(responseCode = "200", description = "Departments retrieved successfully")
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = hrMasterService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }
    
    @GetMapping("/designations")
    @Operation(summary = "Get all designations", description = "Retrieves all active designations")
    @ApiResponse(responseCode = "200", description = "Designations retrieved successfully")
    public ResponseEntity<List<Designation>> getAllDesignations() {
        List<Designation> designations = hrMasterService.getAllDesignations();
        return ResponseEntity.ok(designations);
    }
    
    @GetMapping("/departments/{departmentId}/designations")
    @Operation(summary = "Get designations by department", description = "Retrieves designations for a specific department")
    @ApiResponse(responseCode = "200", description = "Designations retrieved successfully")
    public ResponseEntity<List<Designation>> getDesignationsByDepartment(@PathVariable Long departmentId) {
        List<Designation> designations = hrMasterService.getDesignationsByDepartment(departmentId);
        return ResponseEntity.ok(designations);
    }
}