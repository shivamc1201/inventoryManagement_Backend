package com.nector.userservice.service;

import com.nector.userservice.enums.SalesRole;
import com.nector.userservice.exception.BusinessException;
import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.repository.SalesPersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesHierarchyValidationService {

    private final SalesPersonRepository salesPersonRepository;

    public void validateManagerAssignment(SalesRole role, Long managerId) {
        if (role == SalesRole.NATIONAL_SALES_MGR && managerId != null) {
            throw new BusinessException("National Sales Manager must not have a manager");
        }

        if (role != SalesRole.NATIONAL_SALES_MGR && managerId == null) {
            throw new BusinessException("Manager is required for " + role.getLabel());
        }

        if (managerId != null) {
            Optional<SalesPerson> managerOpt = salesPersonRepository.findById(managerId);
            if (managerOpt.isEmpty()) {
                throw new BusinessException("Manager with ID " + managerId + " not found");
            }

            SalesPerson manager = managerOpt.get();
            if (!manager.getActive()) {
                throw new BusinessException("Manager is not active");
            }

            validateRoleHierarchy(role, manager.getRole());
        }
    }

    private void validateRoleHierarchy(SalesRole subordinateRole, SalesRole managerRole) {
        // Define the hierarchy levels (lower number = more senior)
        int subordinateLevel = getRoleLevel(subordinateRole);
        int managerLevel = getRoleLevel(managerRole);
        
        // National Sales Manager cannot have a manager
        if (subordinateRole == SalesRole.NATIONAL_SALES_MGR) {
            throw new BusinessException("National Sales Manager cannot have a manager");
        }
        
        // Manager must be more senior (lower level number) than subordinate
        if (managerLevel >= subordinateLevel) {
            throw new BusinessException(getInvalidManagerMessage(subordinateRole, managerRole));
        }
    }

    private int getRoleLevel(SalesRole role) {
        switch (role) {
            case NATIONAL_SALES_MGR: return 1;
            case STATE_SALES_MGR: return 2;
            case ZONAL_SALES_MGR: return 3;
            case REGIONAL_SALES_MGR: return 4;
            case AREA_SALES_MGR: return 5;
            case SALES_OFFICER: return 6;
            case SALES_EXECUTIVE: return 7;
            default: return 8;
        }
    }

    private String getInvalidManagerMessage(SalesRole subordinateRole, SalesRole managerRole) {
        List<SalesRole> validManagers = getValidManagers(subordinateRole);
        String validManagerNames = validManagers.stream()
                .map(SalesRole::getLabel)
                .collect(Collectors.joining(", "));
        
        return subordinateRole.getLabel() + " can only report to: " + validManagerNames;
    }

    private List<SalesRole> getValidManagers(SalesRole role) {
        List<SalesRole> validManagers = new ArrayList<>();
        int roleLevel = getRoleLevel(role);
        
        for (SalesRole possibleManager : SalesRole.values()) {
            if (getRoleLevel(possibleManager) < roleLevel) {
                validManagers.add(possibleManager);
            }
        }
        
        return validManagers;
    }

    public void validateDeletion(SalesPerson salesPerson) {
        long subordinateCount = salesPersonRepository.countActiveSubordinates(salesPerson.getId());
        if (subordinateCount > 0) {
            throw new BusinessException("Cannot delete salesperson with active subordinates");
        }

        if (salesPerson.getRole() == SalesRole.NATIONAL_SALES_MGR) {
            long totalActiveSalesPersons = salesPersonRepository.count();
            if (totalActiveSalesPersons > 1) {
                throw new BusinessException("Cannot delete National Sales Manager while other salespersons exist");
            }
        }
    }

    public void validateUsernameUniqueness(String username, Long excludeId) throws BadRequestException {
        if (salesPersonRepository.existsByUsernameAndIdNot(username, excludeId != null ? excludeId : -1L)) {
            throw new BadRequestException("Username '" + username + "' is already taken.");
        }
    }

    public void validateEmployeeRollNoUniqueness(String employeeRollNo, Long excludeId) {
        if (excludeId != null) {
            Optional<SalesPerson> existing = salesPersonRepository.findByEmployeeRollNo(employeeRollNo);
            if (existing.isPresent() && !existing.get().getId().equals(excludeId)) {
                throw new BusinessException("Employee roll number already exists");
            }
        } else {
            if (salesPersonRepository.existsByEmployeeRollNo(employeeRollNo)) {
                throw new BusinessException("Employee roll number already exists");
            }
        }
    }

    public void validatePhoneUniqueness(String phone, Long excludeId) {
        if (phone != null && !phone.trim().isEmpty()) {
            if (excludeId != null) {
                Optional<SalesPerson> existing = salesPersonRepository.findByPhone(phone);
                if (existing.isPresent() && !existing.get().getId().equals(excludeId)) {
                    throw new BusinessException("Phone number already exists");
                }
            } else {
                if (salesPersonRepository.existsByPhone(phone)) {
                    throw new BusinessException("Phone number already exists");
                }
            }
        }
    }

    public void validateEmailUniqueness(String email, Long excludeId) {
        if (email != null && !email.trim().isEmpty()) {
            if (excludeId != null) {
                Optional<SalesPerson> existing = salesPersonRepository.findByEmail(email);
                if (existing.isPresent() && !existing.get().getId().equals(excludeId)) {
                    throw new BusinessException("Email already exists");
                }
            } else {
                if (salesPersonRepository.existsByEmail(email)) {
                    throw new BusinessException("Email already exists");
                }
            }
        }
    }

    public List<Long> getAllSubordinateIds(Long managerSalespersonId) {
        List<Long> subordinateIds = new ArrayList<>();
        List<SalesPerson> directSubordinates = salesPersonRepository.findActiveByManagerId(managerSalespersonId);
        
        for (SalesPerson subordinate : directSubordinates) {
            subordinateIds.add(subordinate.getId());
            // Recursively get subordinates of this subordinate
            subordinateIds.addAll(getAllSubordinateIds(subordinate.getId()));
        }
        
        return subordinateIds;
    }
}
