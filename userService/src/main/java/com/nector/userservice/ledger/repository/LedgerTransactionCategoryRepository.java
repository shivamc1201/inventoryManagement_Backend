package com.nector.userservice.ledger.repository;

import com.nector.userservice.ledger.entity.LedgerTransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for LedgerTransactionCategory entity
 */
@Repository
public interface LedgerTransactionCategoryRepository extends JpaRepository<LedgerTransactionCategory, Long> {

    /**
     * Find category by code
     */
    Optional<LedgerTransactionCategory> findByCategoryCode(String categoryCode);

    /**
     * Find all active categories
     */
    List<LedgerTransactionCategory> findByIsActiveTrue();

    /**
     * Check if category code exists
     */
    boolean existsByCategoryCode(String categoryCode);
}