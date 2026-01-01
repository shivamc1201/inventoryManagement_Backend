package com.nector.userservice.repository;

import com.nector.userservice.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    Optional<Item> findBySku(String sku);
    
    List<Item> findByActiveTrue();
    
    @Query("SELECT i FROM Item i WHERE i.active = true AND i.id = :id")
    Optional<Item> findActiveById(Long id);
    
    boolean existsBySku(String sku);
}