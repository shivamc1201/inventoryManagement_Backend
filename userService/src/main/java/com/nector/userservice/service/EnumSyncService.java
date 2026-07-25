package com.nector.userservice.service;

import com.nector.userservice.enums.RoleCategory;
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

    // {roleType, displayName, department, roleCategory}
    private static final List<String[]> INITIAL_ROLES = List.of(
        new String[]{"SUPER_ADMIN",             "Super Admin",              "Administration",       "USER"},
        new String[]{"ADMIN",                   "Admin",                    "Administration",       "USER"},
        new String[]{"BUSINESS_DEV_MGR",        "Business Dev Manager",     "BusinessDevelopment",  "USER"},
        new String[]{"PLANT_MGR",               "Plant Manager",            "Plant",                "USER"},
        new String[]{"HR_MGR",                  "HR Manager",               "HR",                   "USER"},
        new String[]{"LOGISTICS_MGR",           "Logistics Manager",        "Logistics",            "USER"},
        new String[]{"ACCOUNT_MGR",             "Account Manager",          "Accounts",             "USER"},
        new String[]{"ACCOUNT_OFFICER",         "Account Officer",          "Accounts",             "USER"},
        new String[]{"ACCOUNT_EXECUTIVE",       "Account Executive",        "Accounts",             "USER"},
        new String[]{"NATIONAL_SALES_MGR",      "National Sales Manager",   "Sales",                "SALES"},
        new String[]{"STATE_SALES_MGR",         "State Sales Manager",      "Sales",                "SALES"},
        new String[]{"ZONAL_SALES_MGR",         "Zonal Sales Manager",      "Sales",                "SALES"},
        new String[]{"REGIONAL_SALES_MGR",      "Regional Sales Manager",   "Sales",                "SALES"},
        new String[]{"AREA_SALES_MGR",          "Area Sales Manager",       "Sales",                "SALES"},
        new String[]{"SALES_OFFICER",           "Sales Officer",            "Sales",                "SALES"},
        new String[]{"SALES_EXECUTIVE",         "Sales Executive",          "Sales",                "SALES"},
        new String[]{"LOGISTICS_OFFICER",       "Logistics Officer",        "Logistics",            "USER"},
        new String[]{"DISPATCH",                "Dispatch",                 "Logistics",            "USER"},
        new String[]{"HR_EXECUTIVE",            "HR Executive",             "HR",                   "USER"},
        new String[]{"PLANT_OFFICER",           "Plant Officer",            "Plant",                "USER"},
        new String[]{"PLANT_EXECUTIVE",         "Plant Executive",          "Plant",                "USER"},
        new String[]{"Distributor",             "Distributor",              "Sales",                "SALES"}
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
            RoleCategory category = RoleCategory.valueOf(entry[3]);
            roleRepository.findByRoleType(roleType)
                .orElseGet(() -> roleRepository.save(new Role(roleType, name, description, category)));
        }
    }
}
