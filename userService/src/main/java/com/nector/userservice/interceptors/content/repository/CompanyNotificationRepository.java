package com.nector.userservice.interceptors.content.repository;

import com.nector.userservice.interceptors.content.model.CompanyNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompanyNotificationRepository extends JpaRepository<CompanyNotification, Long> {

    List<CompanyNotification> findAllByOrderByCreatedAtDesc();

    @Query("SELECT n FROM CompanyNotification n WHERE n.isActive = true " +
           "AND (n.expiresAt IS NULL OR n.expiresAt >= :now) " +
           "ORDER BY n.createdAt DESC")
    List<CompanyNotification> findAllActive(LocalDateTime now);
}
