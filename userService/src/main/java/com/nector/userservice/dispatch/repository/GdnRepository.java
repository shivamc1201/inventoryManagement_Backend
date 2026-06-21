package com.nector.userservice.dispatch.repository;

import com.nector.userservice.dispatch.entity.Gdn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GdnRepository extends JpaRepository<Gdn, Long> {
    Optional<Gdn> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
}