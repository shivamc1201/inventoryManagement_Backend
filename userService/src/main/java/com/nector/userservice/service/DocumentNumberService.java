package com.nector.userservice.service;

import com.nector.userservice.model.DocumentNumberSequence;
import com.nector.userservice.repository.DocumentNumberSequenceRepository;
import com.nector.userservice.util.FinancialYearUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to generate sequential document numbers in format: PREFIX/YYYY-YY/NNNN
 * Example: SO/2026-27/0001, EI/2026-27/0002, DC/2026-27/0003, OP/2026-27/0004
 * 
 * Document Types:
 * - SO: Sales Order (Purchase Order)
 * - EI: Estimated Invoice (Proforma Invoice)
 * - DC: Delivery Challan (GDN)
 * - OP: Official Payment (Invoice)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentNumberService {
    
    private final DocumentNumberSequenceRepository sequenceRepository;
    
    /**
     * Generate next Sales Order number (SO/YYYY-YY/NNNN)
     */
    @Transactional
    public String generateSalesOrderNumber() {
        return generateDocumentNumber("SO");
    }
    
    /**
     * Generate next Proforma Invoice number (EI/YYYY-YY/NNNN)
     */
    @Transactional
    public String generateProformaInvoiceNumber() {
        return generateDocumentNumber("EI");
    }
    
    /**
     * Generate next GDN number (DC/YYYY-YY/NNNN)
     */
    @Transactional
    public String generateGdnNumber() {
        return generateDocumentNumber("DC");
    }
    
    /**
     * Generate next Invoice number (OP/YYYY-YY/NNNN)
     */
    @Transactional
    public String generateInvoiceNumber() {
        return generateDocumentNumber("OP");
    }
    
    /**
     * Core method to generate document number with thread-safe sequential numbering
     * 
     * @param documentType Document type prefix (SO, EI, DC, OP)
     * @return Generated document number in format PREFIX/YYYY-YY/NNNN
     */
    @Transactional
    public String generateDocumentNumber(String documentType) {
        String financialYear = FinancialYearUtil.getCurrentFinancialYear();
        
        log.info("Generating {} number for FY {}", documentType, financialYear);
        
        // Get or create sequence with pessimistic lock for thread safety
        DocumentNumberSequence sequence = sequenceRepository
                .findByDocumentTypeAndFinancialYearWithLock(documentType, financialYear)
                .orElseGet(() -> {
                    log.info("Creating new sequence for {} - FY {}", documentType, financialYear);
                    DocumentNumberSequence newSequence = new DocumentNumberSequence();
                    newSequence.setDocumentType(documentType);
                    newSequence.setFinancialYear(financialYear);
                    newSequence.setCurrentSequence(0L);
                    return sequenceRepository.save(newSequence);
                });
        
        // Increment sequence
        Long nextSequence = sequence.getCurrentSequence() + 1;
        sequence.setCurrentSequence(nextSequence);
        
        // Generate document number in format: PREFIX/YYYY-YY/NNNN
        String documentNumber = String.format("%s/%s/%04d", documentType, financialYear, nextSequence);
        sequence.setLastGeneratedNumber(documentNumber);
        
        // Save updated sequence
        sequenceRepository.save(sequence);
        
        log.info("Generated {} number: {} (Sequence: {})", documentType, documentNumber, nextSequence);
        
        return documentNumber;
    }
    
    /**
     * Get current sequence number (read-only)
     */
    @Transactional(readOnly = true)
    public Long getCurrentSequence(String documentType) {
        String financialYear = FinancialYearUtil.getCurrentFinancialYear();
        return sequenceRepository
                .findByDocumentTypeAndFinancialYear(documentType, financialYear)
                .map(DocumentNumberSequence::getCurrentSequence)
                .orElse(0L);
    }
    
    /**
     * Get last generated document number (read-only)
     */
    @Transactional(readOnly = true)
    public String getLastGeneratedNumber(String documentType) {
        String financialYear = FinancialYearUtil.getCurrentFinancialYear();
        return sequenceRepository
                .findByDocumentTypeAndFinancialYear(documentType, financialYear)
                .map(DocumentNumberSequence::getLastGeneratedNumber)
                .orElse(null);
    }
}

