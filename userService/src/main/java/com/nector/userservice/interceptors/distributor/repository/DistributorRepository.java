package com.nector.userservice.interceptors.distributor.repository;

import com.nector.userservice.interceptors.distributor.model.Distributor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistributorRepository extends JpaRepository<Distributor, Long> {
    boolean existsByContactEmail(String contactEmail);
    Optional<Distributor> findByUsername(String username);
    boolean existsByAadhaarNumber(String aadhaarNumber);
    boolean existsByPanNumber(String panNumber);
    boolean existsByGstNumber(String gstNumber);
}