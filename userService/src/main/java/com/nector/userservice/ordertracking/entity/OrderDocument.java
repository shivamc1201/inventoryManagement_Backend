package com.nector.userservice.ordertracking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_documents", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "doc_type"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderTracking order;

    @Column(name = "doc_type", nullable = false)
    private String docType;        // 'PI' | 'GDN'

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "storage_path")
    private String storagePath;    // S3 key or absolute path

    @CreationTimestamp
    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;
}
