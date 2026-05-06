package com.nector.userservice.repository;

import com.nector.userservice.model.DistributorStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistributorStockRepository extends JpaRepository<DistributorStock, Long> {

    List<DistributorStock> findByDistributorId(Long distributorId);

    Optional<DistributorStock> findByDistributorIdAndSku(Long distributorId, String sku);

    Optional<DistributorStock> findByDistributorIdAndItemId(Long distributorId, Long itemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ds FROM DistributorStock ds WHERE ds.distributorId = :distributorId AND ds.sku = :sku")
    Optional<DistributorStock> findByDistributorIdAndSkuForUpdate(
            @Param("distributorId") Long distributorId, 
            @Param("sku") String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ds FROM DistributorStock ds WHERE ds.distributorId = :distributorId AND ds.itemId = :itemId")
    Optional<DistributorStock> findByDistributorIdAndItemIdForUpdate(
            @Param("distributorId") Long distributorId, 
            @Param("itemId") Long itemId);

    boolean existsByDistributorIdAndSku(Long distributorId, String sku);

    @Query("SELECT COALESCE(SUM(ds.quantity), 0) FROM DistributorStock ds WHERE ds.distributorId = :distributorId")
    Integer getTotalQuantityByDistributorId(@Param("distributorId") Long distributorId);
}
