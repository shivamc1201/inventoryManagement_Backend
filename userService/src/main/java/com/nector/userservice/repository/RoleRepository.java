package com.nector.userservice.repository;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleType(RoleType roleType);
    
    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.id IN :roleIds")
    Set<Role> findRolesWithPermissions(@Param("roleIds") Set<Long> roleIds);
}