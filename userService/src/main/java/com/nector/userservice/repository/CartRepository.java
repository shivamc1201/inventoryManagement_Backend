package com.nector.userservice.repository;

import com.nector.userservice.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.item WHERE c.distributorId = :distributorId AND c.status = 'ACTIVE'")
    Optional<Cart> findActiveCartByDistributorId(Long distributorId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.item WHERE c.id = :id")
    Optional<Cart> findByIdWithItems(@Param("id") Long id);
    
    Optional<Cart> findByDistributorIdAndStatus(Long distributorId, Cart.CartStatus status);
    
    List<Cart> findAllByDistributorIdAndStatus(Long distributorId, Cart.CartStatus status);
    
    List<Cart> findByStatus(Cart.CartStatus status);

    @Query("SELECT c FROM Cart c WHERE c.status = :status")
    List<Cart> findCartsByStatus(@Param("status") Cart.CartStatus status);

    List<Cart> findBySalespersonIdInAndStatus(List<Long> salespersonIds, Cart.CartStatus status);

    List<Cart> findByDistributorIdAndStatusIn(Long distributorId, List<Cart.CartStatus> statuses);

    @Query("SELECT c.totalCartAmount FROM Cart c WHERE c.id = :id")
    BigDecimal findTotalCartAmountById(@Param("id") Long id);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.salespersonId = :salespersonId AND c.status = :status")
    Long countBySalespersonIdAndStatus(@Param("salespersonId") Long salespersonId, @Param("status") Cart.CartStatus status);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.salespersonId = :salespersonId AND c.status NOT IN :excludedStatuses")
    Long countBySalespersonIdAndStatusNotIn(@Param("salespersonId") Long salespersonId, @Param("excludedStatuses") List<Cart.CartStatus> excludedStatuses);
}
