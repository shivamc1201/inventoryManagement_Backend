package com.nector.userservice.repository;

import com.nector.userservice.model.PriceMasterProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceMasterProductRepository extends JpaRepository<PriceMasterProduct, Long> {

    // Tenant isolation methods
    Optional<PriceMasterProduct> findByIdAndDistributorId(Long id, Long distributorId);

    List<PriceMasterProduct> findByDealerIdAndDistributorId(Long dealerId, Long distributorId);

    List<PriceMasterProduct> findByDistributorIdAndProductId(Long distributorId, Long productId);

    boolean existsByDealerIdAndDistributorIdAndProductId(Long dealerId, Long distributorId, Long productId);

    // Search functionality
    @Query("SELECT pmp FROM PriceMasterProduct pmp WHERE pmp.distributorId = :distributorId AND " +
           "LOWER(pmp.productName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<PriceMasterProduct> searchProductsByDistributor(@Param("distributorId") Long distributorId, @Param("search") String search);

    // Dealer-specific product pricing
    @Query("SELECT pmp FROM PriceMasterProduct pmp WHERE pmp.dealerId = :dealerId AND pmp.distributorId = :distributorId AND " +
           "LOWER(pmp.productName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<PriceMasterProduct> searchProductsByDealer(@Param("dealerId") Long dealerId, @Param("distributorId") Long distributorId, @Param("search") String search);
}
