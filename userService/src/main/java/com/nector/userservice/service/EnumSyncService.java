package com.nector.userservice.service;

import com.nector.userservice.model.Role;
import com.nector.userservice.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnumSyncService {

    private final RoleRepository roleRepository;

    // Initial set of role types seeded into DB on first boot.
    // New roles can be added via POST /api/admin/roles/create.
    private static final List<String[]> INITIAL_ROLES = List.of(
        new String[]{"SUPER_ADMIN",             "Super Admin",              "Administration"},
        new String[]{"ADMIN",                   "Admin",                    "Administration"},
        new String[]{"BUSINESS_DEV_MGR",        "Business Dev Manager",     "BusinessDevelopment"},
        new String[]{"PLANT_MGR",               "Plant Manager",            "Plant"},
        new String[]{"HR_MGR",                  "HR Manager",               "HR"},
        new String[]{"LOGISTICS_MGR",           "Logistics Manager",        "Logistics"},
        new String[]{"ACCOUNT_MGR",             "Account Manager",          "Accounts"},
        new String[]{"ACCOUNT_OFFICER",         "Account Officer",          "Accounts"},
        new String[]{"ACCOUNT_EXECUTIVE",       "Account Executive",        "Accounts"},
        new String[]{"NATIONAL_SALES_MGR",      "National Sales Manager",   "Sales"},
        new String[]{"STATE_SALES_MGR",         "State Sales Manager",      "Sales"},
        new String[]{"ZONAL_SALES_MGR",         "Zonal Sales Manager",      "Sales"},
        new String[]{"REGIONAL_SALES_MGR",      "Regional Sales Manager",   "Sales"},
        new String[]{"AREA_SALES_MGR",          "Area Sales Manager",       "Sales"},
        new String[]{"SALES_OFFICER",           "Sales Officer",            "Sales"},
        new String[]{"SALES_EXECUTIVE",         "Sales Executive",          "Sales"},
        new String[]{"LOGISTICS_OFFICER",       "Logistics Officer",        "Logistics"},
        new String[]{"DISPATCH",                "Dispatch",                 "Logistics"},
        new String[]{"HR_EXECUTIVE",            "HR Executive",             "HR"},
        new String[]{"PLANT_OFFICER",           "Plant Officer",            "Plant"},
        new String[]{"PLANT_EXECUTIVE",         "Plant Executive",          "Plant"},
        new String[]{"Distributor",             "Distributor",              "Sales"}
    );

    @PostConstruct
    @Transactional
    public void syncEnumsWithDatabase() {
        log.info("Entering syncEnumsWithDatabase()");
        seedInitialRoles();
        log.info("Exiting syncEnumsWithDatabase()");
    }

    private void seedInitialRoles() {
        for (String[] entry : INITIAL_ROLES) {
            String roleType = entry[0];
            String name = entry[1];
            String description = "Role for " + entry[2] + " department";
            roleRepository.findByRoleType(roleType)
                .orElseGet(() -> roleRepository.save(new Role(roleType, name, description)));
        }
    }
}
