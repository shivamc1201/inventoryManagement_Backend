package com.nector.userservice.repository;

import com.nector.userservice.model.ProductInwardApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductInwardApprovalRepository extends JpaRepository<ProductInwardApproval, Long> {

    List<ProductInwardApproval> findByApprovalStatus(String approvalStatus);

    Optional<ProductInwardApproval> findByProductTypeAndProductIdAndApprovalStatus(
            ProductInwardApproval.ProductType productType, Long productId, String approvalStatus);

    boolean existsByProductTypeAndProductIdAndApprovalStatus(
            ProductInwardApproval.ProductType productType, Long productId, String approvalStatus);

    boolean existsByProductTypeAndProductCodeAndApprovalStatus(
            ProductInwardApproval.ProductType productType, String productCode, String approvalStatus);
}
