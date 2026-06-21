package com.nector.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity to track document serial numbers for each document type per financial year
 * Ensures unique sequential numbering: SO/2026-27/0001, EI/2026-27/0001, etc.
 */
@Entity
@Table(name = "document_number_sequences",
       uniqueConstraints = @UniqueConstraint(columnNames = {"document_type", "financial_year"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentNumberSequence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Document type: SO, EI, DC, OP
     */
    @Column(name = "document_type", nullable = false, length = 2)
    private String documentType;
    
    /**
     * Financial year in format YYYY-YY (e.g., "2026-27")
     */
    @Column(name = "financial_year", nullable = false, length = 7)
    private String financialYear;
    
    /**
     * Current sequence number (auto-increments)
     */
    @Column(name = "current_sequence", nullable = false)
    private Long currentSequence;
    
    /**
     * Last generated document number for reference
     */
    @Column(name = "last_generated_number", length = 20)
    private String lastGeneratedNumber;
}

