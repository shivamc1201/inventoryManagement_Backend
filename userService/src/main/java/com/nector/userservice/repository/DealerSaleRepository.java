package com.nector.userservice.repository;

import com.nector.userservice.model.DealerSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DealerSaleRepository extends JpaRepository<DealerSale, Long> {

    // Tenant isolation methods
    Optional<DealerSale> findByIdAndDistributorId(Long id, Long distributorId);

    List<DealerSale> findByDealerIdAndDistributorId(Long dealerId, Long distributorId);

    List<DealerSale> findByDistributorIdAndDateBetween(Long distributorId, LocalDate startDate, LocalDate endDate);

    List<DealerSale> findByDealerIdAndDistributorIdAndDateBetween(Long dealerId, Long distributorId, LocalDate startDate, LocalDate endDate);

    // Statistics methods
    @Query("SELECT COUNT(ds) FROM DealerSale ds WHERE ds.dealerId = :dealerId AND ds.distributorId = :distributorId")
    long countSalesByDealer(@Param("dealerId") Long dealerId, @Param("distributorId") Long distributorId);

    @Query("SELECT SUM(ds.amount) FROM DealerSale ds WHERE ds.dealerId = :dealerId AND ds.distributorId = :distributorId")
    BigDecimal sumSalesAmountByDealer(@Param("dealerId") Long dealerId, @Param("distributorId") Long distributorId);

    @Query("SELECT SUM(ds.amount) FROM DealerSale ds WHERE ds.distributorId = :distributorId AND ds.date BETWEEN :startDate AND :endDate")
    BigDecimal sumSalesAmountByDistributorAndDateRange(@Param("distributorId") Long distributorId, 
                                                        @Param("startDate") LocalDate startDate, 
                                                        @Param("endDate") LocalDate endDate);

    // Search functionality
    @Query("SELECT ds FROM DealerSale ds WHERE ds.distributorId = :distributorId AND " +
           "(LOWER(ds.itemName) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<DealerSale> searchSalesByDistributor(@Param("distributorId") Long distributorId, @Param("search") String search);

    // New methods for getting all sales by dealer or distributor
    List<DealerSale> findByDealerIdOrderByDateDesc(Long dealerId);

    List<DealerSale> findByDistributorIdOrderByDateDesc(Long distributorId);

    // Check if product price already exists for dealer
    boolean existsByDealerIdAndSkuAndDistributorId(Long dealerId, String sku, Long distributorId);

    Optional<DealerSale> findByDealerIdAndSkuAndDistributorId(Long dealerId, String sku, Long distributorId);
}
