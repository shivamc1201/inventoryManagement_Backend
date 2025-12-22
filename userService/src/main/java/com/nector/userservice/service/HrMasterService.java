package com.nector.userservice.service;

import com.nector.userservice.dto.hrmaster.DepartmentRequest;
import com.nector.userservice.dto.hrmaster.DesignationRequest;
import com.nector.userservice.model.Department;
import com.nector.userservice.model.Designation;
import com.nector.userservice.repository.DepartmentRepository;
import com.nector.userservice.repository.DesignationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HrMasterService {
    
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    
    public Department createDepartment(DepartmentRequest request, String createdBy) {
        log.info("Entering createDepartment() for name: {}, createdBy: {}", request.getName(), createdBy);
        
        if (departmentRepository.existsByNameIgnoreCase(request.getName())) {
            log.warn("Exiting createDepartment() - Department already exists: {}", request.getName());
            throw new RuntimeException("Department already exists");
        }
        
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setCreatedBy(createdBy);
        
        Department saved = departmentRepository.save(department);
        log.info("Exiting createDepartment() - Department created with ID: {}", saved.getId());
        return saved;
    }
    
    public Designation createDesignation(DesignationRequest request, String createdBy) {
        log.info("Entering createDesignation() for name: {}, departmentId: {}, createdBy: {}", 
                request.getName(), request.getDepartmentId(), createdBy);
        
        if (designationRepository.existsByNameIgnoreCase(request.getName())) {
            log.warn("Exiting createDesignation() - Designation already exists: {}", request.getName());
            throw new RuntimeException("Designation already exists");
        }
        
        Department department = departmentRepository.findById(request.getDepartmentId())
            .orElseThrow(() -> {
                log.warn("Exiting createDesignation() - Department not found: {}", request.getDepartmentId());
                return new RuntimeException("Department not found");
            });
        
        Designation designation = new Designation();
        designation.setName(request.getName());
        designation.setDescription(request.getDescription());
        designation.setDepartment(department);
        designation.setCreatedBy(createdBy);
        
        Designation saved = designationRepository.save(designation);
        log.info("Exiting createDesignation() - Designation created with ID: {}", saved.getId());
        return saved;
    }
    
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        log.info("Entering getAllDepartments()");
        List<Department> departments = departmentRepository.findByActiveTrue();
        log.info("Exiting getAllDepartments() with {} departments", departments.size());
        return departments;
    }
    
    @Transactional(readOnly = true)
    public List<Designation> getAllDesignations() {
        log.info("Entering getAllDesignations()");
        List<Designation> designations = designationRepository.findByActiveTrue();
        log.info("Exiting getAllDesignations() with {} designations", designations.size());
        return designations;
    }
    
    @Transactional(readOnly = true)
    public List<Designation> getDesignationsByDepartment(Long departmentId) {
        log.info("Entering getDesignationsByDepartment() for departmentId: {}", departmentId);
        List<Designation> designations = designationRepository.findByDepartmentIdAndActiveTrue(departmentId);
        log.info("Exiting getDesignationsByDepartment() with {} designations for departmentId: {}", 
                designations.size(), departmentId);
        return designations;
    }
}