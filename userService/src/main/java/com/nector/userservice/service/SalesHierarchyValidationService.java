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
        switch (subordinateRole) {
            case STATE_SALES_MGR:
                if (managerRole != SalesRole.NATIONAL_SALES_MGR) {
                    throw new BusinessException("State Sales Manager must report to National Sales Manager");
                }
                break;
            case ZONAL_SALES_MGR:
                if (managerRole != SalesRole.STATE_SALES_MGR) {
                    throw new BusinessException("Zonal Sales Manager must report to State Sales Manager");
                }
                break;
            case REGIONAL_SALES_MGR:
                if (managerRole != SalesRole.ZONAL_SALES_MGR) {
                    throw new BusinessException("Regional Sales Manager must report to Zonal Sales Manager");
                }
                break;
            case AREA_SALES_MGR:
                if (managerRole != SalesRole.REGIONAL_SALES_MGR) {
                    throw new BusinessException("Area Sales Manager must report to Regional Sales Manager");
                }
                break;
            case SALES_OFFICER:
                if (managerRole != SalesRole.AREA_SALES_MGR) {
                    throw new BusinessException("Sales Officer must report to Area Sales Manager");
                }
                break;
            case SALES_EXECUTIVE:
                if (managerRole != SalesRole.SALES_OFFICER) {
                    throw new BusinessException("Sales Executive must report to Sales Officer");
                }
                break;
            case NATIONAL_SALES_MGR:
                throw new BusinessException("National Sales Manager cannot have a manager");
        }
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
