package com.nector.userservice.interceptors.content.repository;

import com.nector.userservice.interceptors.content.model.CompanyOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompanyOfferRepository extends JpaRepository<CompanyOffer, Long> {

    List<CompanyOffer> findAllByOrderByDisplayOrderAscCreatedAtDesc();

    @Query("SELECT o FROM CompanyOffer o WHERE o.isActive = true " +
           "AND (o.validFrom IS NULL OR o.validFrom <= :now) " +
           "AND (o.validTo IS NULL OR o.validTo >= :now) " +
           "ORDER BY o.displayOrder ASC, o.createdAt DESC")
    List<CompanyOffer> findAllActive(LocalDateTime now);
}
