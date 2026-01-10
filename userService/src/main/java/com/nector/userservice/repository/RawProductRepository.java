package com.nector.userservice.repository;

import com.nector.userservice.model.RawProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RawProductRepository extends JpaRepository<RawProduct, Long> {
    
    Optional<RawProduct> findByMaterialCode(String materialCode);
    
    List<RawProduct> findByActiveTrue();
    
    @Query("SELECT rp FROM RawProduct rp WHERE rp.active = true AND rp.id = :id")
    Optional<RawProduct> findActiveById(Long id);
    
    @Query("SELECT rp FROM RawProduct rp WHERE rp.active = true AND rp.quantity <= rp.minimumThreshold")
    List<RawProduct> findLowStockItems();
    
    boolean existsByMaterialCode(String materialCode);
}