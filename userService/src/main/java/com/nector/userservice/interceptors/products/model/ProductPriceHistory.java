package com.nector.userservice.interceptors.products.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product_price_history",
        indexes = {
                @Index(name = "idx_pph_product_type_id", columnList = "product_type, product_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceHistory {

    public enum ProductType {
        PROMOTIONAL_ITEM,
        SCRAP_ITEM,
        MACHINE_PART
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 30)
    private ProductType productType;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    /** itemCode / partNumber depending on product type */
    @Column(name = "product_code")
    private String productCode;

    @Column(name = "old_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal oldPrice;

    @Column(name = "new_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal newPrice;

    /** Positive = price hike, negative = price drop, expressed as percentage. */
    @Column(name = "price_change_percent", precision = 10, scale = 2)
    private BigDecimal priceChangePercent;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private Instant changedAt;
}
