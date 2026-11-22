package com.nector.userservice.repository;

import com.nector.userservice.model.InvoiceLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, Long> {

    @Query("SELECT i.productId, i.productName, SUM(i.quantity), SUM(i.amount), COUNT(DISTINCT i.invoiceId) " +
           "FROM InvoiceLineItem i " +
           "JOIN Invoice inv ON inv.id = i.invoiceId " +
           "WHERE inv.invoiceDate BETWEEN :from AND :to " +
           "GROUP BY i.productId, i.productName " +
           "ORDER BY SUM(i.amount) DESC")
    List<Object[]> getProductSalesSummary(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT i.productId, i.productName, SUM(i.quantity), SUM(i.amount), COUNT(DISTINCT i.invoiceId) " +
           "FROM InvoiceLineItem i " +
           "JOIN Invoice inv ON inv.id = i.invoiceId " +
           "WHERE inv.invoiceDate BETWEEN :from AND :to " +
           "AND inv.distributorId IN :distributorIds " +
           "GROUP BY i.productId, i.productName " +
           "ORDER BY SUM(i.amount) DESC")
    List<Object[]> getProductSalesSummaryForDistributors(@Param("from") LocalDateTime from,
                                                          @Param("to") LocalDateTime to,
                                                          @Param("distributorIds") List<Long> distributorIds);

    @Query("SELECT i.productId, i.productName, SUM(i.quantity), SUM(i.amount) " +
           "FROM InvoiceLineItem i " +
           "JOIN Invoice inv ON inv.id = i.invoiceId " +
           "WHERE inv.invoiceDate BETWEEN :from AND :to " +
           "GROUP BY i.productId, i.productName " +
           "ORDER BY SUM(i.quantity) DESC")
    List<Object[]> getTopProductsByQuantity(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
