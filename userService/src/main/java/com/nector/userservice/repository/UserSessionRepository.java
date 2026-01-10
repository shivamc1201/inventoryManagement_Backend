package com.nector.userservice.repository;

import com.nector.userservice.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    List<UserSession> findByUserIdAndActiveTrue(Long userId);
    
    Optional<UserSession> findBySessionTokenAndActiveTrue(String sessionToken);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.active = false WHERE s.userId = ?1")
    void deactivateAllUserSessions(Long userId);
    
    @Modifying
    @Query("UPDATE UserSession s SET s.active = false WHERE s.sessionToken = ?1")
    void deactivateSession(String sessionToken);
}