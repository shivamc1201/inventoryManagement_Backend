package com.nector.userservice.controller;

import com.nector.userservice.service.HrService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hr-master")
@RequiredArgsConstructor
@Tag(name = "HR Master", description = "Department and Designation management (HR Master only)")
public class HrController {
    
    private final HrService hrMasterService;
    
//    @PostMapping("/departments")
//    @Operation(summary = "Create department", description = "Creates a new department (HR Master only)")
//    @ApiResponse(responseCode = "200", description = "Department created successfully")
//    public ResponseEntity<Department> createDepartment(
//            @Valid @RequestBody DepartmentRequest request,
//            @RequestHeader("HrMaster-Username") String hrMasterUsername) {
//        Department department = hrMasterService.createDepartment(request, hrMasterUsername);
//        return ResponseEntity.ok(department);
//    }
//
//    @PostMapping("/designations")
//    @Operation(summary = "Create designation", description = "Creates a new designation (HR Master only)")
//    @ApiResponse(responseCode = "200", description = "Designation created successfully")
//    public ResponseEntity<Designation> createDesignation(
//            @Valid @RequestBody DesignationRequest request,
//            @RequestHeader("HrMaster-Username") String hrMasterUsername) {
//        Designation designation = hrMasterService.createDesignation(request, hrMasterUsername);
//        return ResponseEntity.ok(designation);
//    }
//
//    @GetMapping("/departments")
//    @Operation(summary = "Get all departments", description = "Retrieves all active departments")
//    @ApiResponse(responseCode = "200", description = "Departments retrieved successfully")
//    public ResponseEntity<List<Department>> getAllDepartments() {
//        List<Department> departments = hrMasterService.getAllDepartments();
//        return ResponseEntity.ok(departments);
//    }
//
//    @GetMapping("/designations")
//    @Operation(summary = "Get all designations", description = "Retrieves all active designations")
//    @ApiResponse(responseCode = "200", description = "Designations retrieved successfully")
//    public ResponseEntity<List<Designation>> getAllDesignations() {
//        List<Designation> designations = hrMasterService.getAllDesignations();
//        return ResponseEntity.ok(designations);
//    }
//
//    @GetMapping("/departments/{departmentId}/designations")
//    @Operation(summary = "Get designations by department", description = "Retrieves designations for a specific department")
//    @ApiResponse(responseCode = "200", description = "Designations retrieved successfully")
//    public ResponseEntity<List<Designation>> getDesignationsByDepartment(@PathVariable Long departmentId) {
//        List<Designation> designations = hrMasterService.getDesignationsByDepartment(departmentId);
//        return ResponseEntity.ok(designations);
//    }
}