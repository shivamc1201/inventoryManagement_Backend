package com.nector.userservice.service;

import com.nector.userservice.exception.BusinessException;
import com.nector.userservice.model.PriceMasterProduct;
import com.nector.userservice.repository.DealerRepository;
import com.nector.userservice.repository.PriceMasterProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceMasterService {

    private final PriceMasterProductRepository priceMasterProductRepository;
    private final DealerRepository dealerRepository;

    @Transactional(readOnly = true)
    public List<PriceMasterProduct> getPriceMasterForDealer(Long dealerId, Long distributorId) {
        log.info("Fetching price master for dealer: {} and distributor: {}", dealerId, distributorId);

        // Verify dealer exists and belongs to distributor
        dealerRepository.findByIdAndDistributorId(dealerId, distributorId)
                .orElseThrow(() -> new BusinessException("Dealer not found or access denied"));

        return priceMasterProductRepository.findByDealerIdAndDistributorId(dealerId, distributorId);
    }

    @Transactional(readOnly = true)
    public List<PriceMasterProduct> searchPriceMaster(Long distributorId, String search) {
        log.info("Searching price master for distributor: {} with search: {}", distributorId, search);

        if (search != null && !search.trim().isEmpty()) {
            return priceMasterProductRepository.searchProductsByDistributor(distributorId, search.trim());
        } else {
            // Return all products for distributor if no search term
            // This would need a new repository method if needed
            return List.of();
        }
    }

    @Transactional
    public PriceMasterProduct addProductToPriceMaster(PriceMasterProduct product, Long distributorId) {
        log.info("Adding product to price master for dealer: {} and distributor: {}", product.getDealerId(), distributorId);

        // Verify dealer exists and belongs to distributor
        dealerRepository.findByIdAndDistributorId(product.getDealerId(), distributorId)
                .orElseThrow(() -> new BusinessException("Dealer not found or access denied"));

        // Check if product already exists for this dealer
        if (priceMasterProductRepository.existsByDealerIdAndDistributorIdAndProductId(
                product.getDealerId(), distributorId, product.getProductId())) {
            throw new BusinessException("Product already exists in price master for this dealer");
        }

        product.setDistributorId(distributorId);
        return priceMasterProductRepository.save(product);
    }

    @Transactional
    public List<PriceMasterProduct> bulkAddToPriceMaster(List<PriceMasterProduct> products, Long distributorId) {
        log.info("Bulk adding {} products to price master for distributor: {}", products.size(), distributorId);

        // Verify all dealers exist and belong to distributor
        for (PriceMasterProduct product : products) {
            dealerRepository.findByIdAndDistributorId(product.getDealerId(), distributorId)
                    .orElseThrow(() -> new BusinessException("Dealer not found or access denied for dealer ID: " + product.getDealerId()));
        }

        return priceMasterProductRepository.saveAll(products);
    }
}
