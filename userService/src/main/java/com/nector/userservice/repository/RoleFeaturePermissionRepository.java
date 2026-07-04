package com.nector.userservice.repository;

import com.nector.userservice.model.RoleFeaturePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleFeaturePermissionRepository extends JpaRepository<RoleFeaturePermission, Long> {

    @Query("SELECT rfp FROM RoleFeaturePermission rfp WHERE rfp.userId = :userId AND rfp.featureId = :featureId")
    List<RoleFeaturePermission> findByUserIdAndFeatureId(@Param("userId") Long userId,
                                                         @Param("featureId") Integer featureId);

    @Query("SELECT rfp FROM RoleFeaturePermission rfp WHERE rfp.userId = :userId")
    List<RoleFeaturePermission> findByUserId(@Param("userId") Long userId);

    @Query("SELECT rfp FROM RoleFeaturePermission rfp WHERE rfp.featureId = :featureId")
    List<RoleFeaturePermission> findByFeatureId(@Param("featureId") Integer featureId);
}
