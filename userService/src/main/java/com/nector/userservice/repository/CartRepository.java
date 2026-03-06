package com.nector.userservice.repository;

import com.nector.userservice.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.item WHERE c.distributorId = :distributorId AND c.status = 'ACTIVE'")
    Optional<Cart> findActiveCartByDistributorId(Long distributorId);
    
    Optional<Cart> findByDistributorIdAndStatus(Long distributorId, Cart.CartStatus status);
    
    List<Cart> findByStatus(Cart.CartStatus status);
}