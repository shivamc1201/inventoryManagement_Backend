package com.nector.userservice.service;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.enums.RoleCategory;
import com.nector.userservice.model.Role;
import com.nector.userservice.model.User;
import com.nector.userservice.model.UserApproval;
import com.nector.userservice.repository.RoleRepository;
import com.nector.userservice.repository.UserApprovalRepository;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserApprovalRepository userApprovalRepository;

    @Transactional
    public void assignRoleToUser(Long userId, String roleType) {
        log.info("Entering assignRoleToUser() for userId: {}, roleType: {}", userId, roleType);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByRoleType(roleType)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleType));

        user.getRoles().add(role);
        user.setStatus(UserStatus.ACTIVE);

        UserApproval approval = userApprovalRepository.findByUser(user)
            .orElseGet(() -> {
                UserApproval newApproval = new UserApproval();
                newApproval.setUser(user);
                return newApproval;
            });
        approval.setApprovalStatus("APPROVED");

        userApprovalRepository.save(approval);
        userRepository.save(user);

        log.info("Exiting assignRoleToUser() - Role assigned successfully for userId: {}, roleType: {}", userId, roleType);
    }

    @Transactional
    public void removeRoleFromUser(Long userId, String roleType) {
        log.info("Entering removeRoleFromUser() for userId: {}, roleType: {}", userId, roleType);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByRoleType(roleType)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleType));

        user.getRoles().remove(role);
        userRepository.save(user);

        log.info("Exiting removeRoleFromUser() - Role removed successfully for userId: {}, roleType: {}", userId, roleType);
    }

    @Transactional
    public Role createRole(String roleType, String name, String description, RoleCategory roleCategory) {
        log.info("Creating new role: {}", roleType);

        if (roleRepository.existsByRoleType(roleType)) {
            throw new RuntimeException("Role already exists: " + roleType);
        }

        RoleCategory category = roleCategory != null ? roleCategory : RoleCategory.USER;
        Role role = new Role(roleType, name != null ? name : roleType, description, category);
        Role saved = roleRepository.save(role);

        log.info("Created new role: {}", roleType);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        log.info("Entering getAllRoles()");
        return roleRepository.findAll();
    }
}
