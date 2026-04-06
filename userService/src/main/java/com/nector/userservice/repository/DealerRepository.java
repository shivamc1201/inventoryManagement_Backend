package com.nector.userservice.repository;

import com.nector.userservice.model.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {

    // Tenant isolation methods
    Optional<Dealer> findById(Long id);


    Optional<Dealer> findByIdAndDistributorId(Long id, Long distributorId);

    List<Dealer> findByDistributorIdAndIsActiveTrue(Long distributorId);

    boolean existsByPhoneAndDistributorId(String phone, Long distributorId);

    Optional<Dealer> findByPhoneAndDistributorId(String phone, Long distributorId);

    // Search functionality
    @Query("SELECT d FROM Dealer d WHERE d.distributorId = :distributorId AND d.isActive = true AND " +
           "(LOWER(d.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Dealer> searchActiveDealers(@Param("distributorId") Long distributorId, @Param("search") String search);

    // Pessimistic locking for balance calculation
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Dealer d WHERE d.id = :id AND d.distributorId = :distributorId")
    Optional<Dealer> findByIdAndDistributorIdForUpdate(@Param("id") Long id, @Param("distributorId") Long distributorId);

    // Count methods
    @Query("SELECT COUNT(d) FROM Dealer d WHERE d.distributorId = :distributorId AND d.isActive = true")
    long countActiveDealersByDistributor(@Param("distributorId") Long distributorId);

    // Salesperson related queries
    List<Dealer> findBySalespersonIdAndIsActiveTrue(Long salespersonId);

    @Query("SELECT d FROM Dealer d WHERE d.salespersonId = :salespersonId AND d.isActive = true AND " +
           "(LOWER(d.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.phone) LIKE LOWER(CONCAT('%', :search, '%'))) ")
    List<Dealer> searchActiveDealersBySalesperson(@Param("salespersonId") Long salespersonId, @Param("search") String search);

    @Query("SELECT COUNT(d) FROM Dealer d WHERE d.salespersonId = :salespersonId AND d.isActive = true")
    long countActiveDealersBySalesperson(@Param("salespersonId") Long salespersonId);

    // Find dealers without salesperson assignment
    @Query("SELECT d FROM Dealer d WHERE d.distributorId = :distributorId AND d.salespersonId IS NULL AND d.isActive = true")
    List<Dealer> findUnassignedDealersByDistributor(@Param("distributorId") Long distributorId);
}
