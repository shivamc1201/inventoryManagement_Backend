package com.nector.userservice.dispatch.repository;

import com.nector.userservice.dispatch.entity.Gdn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface GdnRepository extends JpaRepository<Gdn, Long> {

    Optional<Gdn> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    // --- Report queries ---

    @Query("SELECT g FROM Gdn g WHERE g.gdnDate BETWEEN :from AND :to ORDER BY g.gdnDate DESC")
    Page<Gdn> findByGdnDateBetween(@Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to,
                                    Pageable pageable);

    @Query("SELECT g FROM Gdn g WHERE g.gdnDate BETWEEN :from AND :to ORDER BY g.gdnDate DESC")
    List<Gdn> findAllByGdnDateBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(g), COALESCE(SUM(g.totalWeight), 0), COALESCE(SUM(g.totalPackages), 0) " +
           "FROM Gdn g WHERE g.gdnDate BETWEEN :from AND :to")
    List<Object[]> getDispatchSummary(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}