package com.nector.userservice.repository;

import com.nector.userservice.model.RoleFeaturePermission;
import com.nector.userservice.common.features.Features;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleFeaturePermissionRepository extends JpaRepository<RoleFeaturePermission, Long> {
    
    @Query("SELECT rfp FROM RoleFeaturePermission rfp WHERE rfp.roleId = :roleId AND rfp.feature = :feature")
    Optional<RoleFeaturePermission> findByRoleIdAndFeature(@Param("roleId") Integer roleId,
                                                          @Param("feature") Features feature);

    @Query("SELECT rfp FROM RoleFeaturePermission rfp WHERE rfp.roleId = :roleId AND rfp.featureId = :featureId")
    Optional<RoleFeaturePermission> findByRoleIdAndFeatureId(@Param("roleId") Integer roleId,
                                                             @Param("featureId") Integer featureId);

    @Query("SELECT rfp FROM RoleFeaturePermission rfp WHERE rfp.roleId = :roleId")
    List<RoleFeaturePermission> findByRoleId(@Param("roleId") Integer roleId);
    
    @Query("SELECT rfp FROM RoleFeaturePermission rfp WHERE rfp.featureId = :featureId")
    List<RoleFeaturePermission> findByFeatureId(@Param("featureId") Integer featureId);
    
    @Query("SELECT rfp FROM RoleFeaturePermission rfp WHERE rfp.roleId = :roleId AND rfp.canCreate = true")
    List<RoleFeaturePermission> findCreatableFeaturesByRoleId(@Param("roleId") Integer roleId);
    
    @Query("SELECT rfp FROM RoleFeaturePermission rfp WHERE rfp.roleId = :roleId AND rfp.canRead = true")
    List<RoleFeaturePermission> findReadableFeaturesByRoleId(@Param("roleId") Integer roleId);
    
    @Query("SELECT rfp FROM RoleFeaturePermission rfp WHERE rfp.roleId = :roleId AND rfp.canUpdate = true")
    List<RoleFeaturePermission> findUpdatableFeaturesByRoleId(@Param("roleId") Integer roleId);
    
    @Query("SELECT rfp FROM RoleFeaturePermission rfp WHERE rfp.roleId = :roleId AND rfp.canDelete = true")
    List<RoleFeaturePermission> findDeletableFeaturesByRoleId(@Param("roleId") Integer roleId);
}
