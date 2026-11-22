package com.nector.userservice.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions", indexes = {
    @Index(name = "idx_st_item_type_id",  columnList = "item_type,item_id"),
    @Index(name = "idx_st_txn_date",      columnList = "txn_date"),
    @Index(name = "idx_st_txn_type",      columnList = "txn_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransaction {

    public enum ItemType {
        RAW_MATERIAL, SPARE_PART, PROMOTIONAL, SCRAP, FINISHED_PRODUCT
    }

    public enum TxnType {
        INWARD, OUTWARD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_code", length = 100)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false, length = 10)
    private TxnType txnType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "party_name", length = 200)
    private String partyName;

    @Column(name = "voucher_no", length = 50)
    private String voucherNo;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(name = "txn_date", nullable = false)
    private LocalDate txnDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
