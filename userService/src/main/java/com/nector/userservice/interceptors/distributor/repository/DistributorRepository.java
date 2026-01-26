package com.nector.userservice.interceptors.distributor.repository;

import com.nector.userservice.interceptors.distributor.model.Distributor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistributorRepository extends JpaRepository<Distributor, Long> {
    boolean existsByContactEmail(String contactEmail);
    boolean existsByAadhaarNumber(String aadhaarNumber);
    boolean existsByPanNumber(String panNumber);
    boolean existsByGstNumber(String gstNumber);
}