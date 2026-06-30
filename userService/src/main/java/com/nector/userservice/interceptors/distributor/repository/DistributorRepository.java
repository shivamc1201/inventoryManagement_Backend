package com.nector.userservice.interceptors.distributor.repository;

import com.nector.userservice.interceptors.distributor.model.Distributor;
import com.nector.userservice.interceptors.distributor.model.DistributorStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistributorRepository extends JpaRepository<Distributor, Long> {
    boolean existsByContactEmail(String contactEmail);
    Optional<Distributor> findByUsername(String username);
    boolean existsByAadhaarNumber(String aadhaarNumber);
    boolean existsByPanNumber(String panNumber);
    boolean existsByGstNumber(String gstNumber);
    
    @Query("SELECT COUNT(d) FROM Distributor d WHERE MONTH(d.createdOn) = :month AND YEAR(d.createdOn) = :year")
    long countByMonthAndYear(@Param("month") int month, @Param("year") int year);

    List<Distributor> findBySalespersonId(Long salespersonId);
    List<Distributor> findBySalespersonIdIn(List<Long> salespersonIds);

    Long countBySalespersonIdAndStatus(Long salespersonId, DistributorStatus status);

    @Query("SELECT COUNT(d) FROM Distributor d WHERE d.salespersonId = :salespersonId AND d.status = 'ACTIVE'")
    Long countActiveBySalespersonId(@Param("salespersonId") Long salespersonId);

    @Query("SELECT COUNT(d) FROM Distributor d WHERE d.salespersonId = :salespersonId AND d.status = 'INACTIVE'")
    Long countInactiveBySalespersonId(@Param("salespersonId") Long salespersonId);

    @Query("SELECT COUNT(d) FROM Distributor d")
    long countAllDistributors();
}