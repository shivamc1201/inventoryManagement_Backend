package com.nector.userservice.repository;

import com.nector.userservice.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.item WHERE c.userId = :userId AND c.status = 'ACTIVE'")
    Optional<Cart> findActiveCartByUserId(Long userId);
    
    Optional<Cart> findByUserIdAndStatus(Long userId, Cart.CartStatus status);
}