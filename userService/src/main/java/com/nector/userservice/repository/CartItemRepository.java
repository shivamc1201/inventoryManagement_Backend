package com.nector.userservice.repository;

import com.nector.userservice.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.item.id = :itemId")
    Optional<CartItem> findByCartIdAndItemId(Long cartId, Long itemId);
}