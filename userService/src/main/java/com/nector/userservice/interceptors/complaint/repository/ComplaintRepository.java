package com.nector.userservice.interceptors.complaint.repository;

import com.nector.userservice.model.ComplaintEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<ComplaintEntity, Long> {

    Page<ComplaintEntity> findBySalespersonId(Long salespersonId, Pageable pageable);

    Page<ComplaintEntity> findByDistributorId(Long distributorId, Pageable pageable);

    Page<ComplaintEntity> findBySalespersonIdAndDistributorId(Long salespersonId, Long distributorId, Pageable pageable);
}