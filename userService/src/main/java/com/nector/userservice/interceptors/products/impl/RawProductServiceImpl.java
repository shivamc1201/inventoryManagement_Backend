package com.nector.userservice.interceptors.products.impl;

import com.nector.userservice.bom.entity.RawMaterialInventoryLot;
import com.nector.userservice.bom.repository.RawMaterialInventoryLotRepository;
import com.nector.userservice.bom.service.BomPriceChangeService;
import com.nector.userservice.cloudinary.CloudinaryService;
import com.nector.userservice.exception.InsufficientStockException;
import com.nector.userservice.exception.RawProductNotFoundException;
import com.nector.userservice.interceptors.products.model.RawProductRequest;
import com.nector.userservice.interceptors.products.model.RawProductResponse;
import com.nector.userservice.repository.RawProductRepository;
import com.nector.userservice.interceptors.products.service.RawProductService;
import com.nector.userservice.model.RawProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RawProductServiceImpl implements RawProductService {

    private final RawProductRepository rawProductRepository;
    private final CloudinaryService cloudinaryService;
    private final BomPriceChangeService bomPriceChangeService;
    private final RawMaterialInventoryLotRepository inventoryLotRepository;

    @Override
    @Transactional
    public RawProductResponse createRawProduct(RawProductRequest request) {
        return createRawProduct(request, null);
    }

    @Override
    @Transactional
    public RawProductResponse createRawProduct(RawProductRequest request, MultipartFile image) {
        log.info("Creating raw product with material code: {}", request.getMaterialCode());

        if (request.getMaterialCode() != null && rawProductRepository.existsByMaterialCode(request.getMaterialCode())) {
            throw new DataIntegrityViolationException("Raw product with material code " + request.getMaterialCode() + " already exists");
        }

        RawProduct product = new RawProduct();
        product.setName(request.getName());
        product.setMaterialCode(request.getMaterialCode());
        product.setUnit(request.getUnit());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setMinimumThreshold(request.getMinimumThreshold());
        product.setVendorId(request.getVendorId());
        product.setVendorName(request.getVendorName());
        product.setTransportName(request.getTransportName());
        product.setDriverName(request.getDriverName());
        product.setDriverMobile(request.getDriverMobile());
        product.setRate(request.getRate());
        product.setGst(request.getGst());
        product.setGrossAmount(request.getGrossAmount());
        product.setActive(true);
        if (request.getStatus() != null) product.setStatus(request.getStatus());

        if (image != null && !image.isEmpty()) {
            log.info("Uploading image for raw product with material code: {}", request.getMaterialCode());
            String imageUrl = cloudinaryService.uploadImage(image);
            product.setImageUrl(imageUrl);
        }

        RawProduct savedProduct = rawProductRepository.save(product);
        log.info("Raw product created successfully with ID: {}", savedProduct.getId());

        if (request.getQuantity() != null && request.getQuantity().compareTo(BigDecimal.ZERO) > 0
                && request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            createInventoryLot(savedProduct.getId(), request.getQuantity(), request.getPrice());
        }

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional
    public RawProductResponse updateRawProduct(Long id, RawProductRequest request) {
        return updateRawProduct(id, request, null);
    }

    @Override
    @Transactional
    public RawProductResponse updateRawProduct(Long id, RawProductRequest request, MultipartFile image) {
        log.info("Updating raw product with ID: {}", id);

        RawProduct product = rawProductRepository.findById(id)
                .orElseThrow(() -> new RawProductNotFoundException(id));

        BigDecimal oldPrice = product.getPrice();

        product.setName(request.getName());
        product.setUnit(request.getUnit());
        product.setPrice(request.getPrice());

        BigDecimal qtyAdded = null;
        if (request.getQuantity() != null && request.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentQty = product.getQuantity() != null ? product.getQuantity() : BigDecimal.ZERO;
            product.setQuantity(currentQty.add(request.getQuantity()));
            qtyAdded = request.getQuantity();
        }

        product.setMinimumThreshold(request.getMinimumThreshold());
        product.setHsn(request.getHsn());
        product.setTaxRate(request.getTaxRate());
        product.setVendorId(request.getVendorId());
        product.setVendorName(request.getVendorName());
        product.setTransportName(request.getTransportName());
        product.setDriverName(request.getDriverName());
        product.setDriverMobile(request.getDriverMobile());
        product.setRate(request.getRate());
        product.setGst(request.getGst());
        product.setGrossAmount(request.getGrossAmount());
        if (request.getStatus() != null) product.setStatus(request.getStatus());

        if (image != null && !image.isEmpty()) {
            log.info("Uploading new image for raw product with ID: {}", id);
            String imageUrl = cloudinaryService.uploadImage(image);
            product.setImageUrl(imageUrl);
        }

        RawProduct updatedProduct = rawProductRepository.save(product);
        log.info("Raw product updated successfully with ID: {}", updatedProduct.getId());

        if (qtyAdded != null) {
            BigDecimal lotPrice = updatedProduct.getPrice() != null ? updatedProduct.getPrice() : BigDecimal.ZERO;
            if (lotPrice.compareTo(BigDecimal.ZERO) > 0) {
                createInventoryLot(updatedProduct.getId(), qtyAdded, lotPrice);
            }
        }

        BigDecimal newPrice = updatedProduct.getPrice();
        if (oldPrice != null && newPrice != null && oldPrice.compareTo(newPrice) != 0) {
            bomPriceChangeService.handleRawMaterialPriceChange(updatedProduct, oldPrice, newPrice);
        }

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteRawProduct(Long id) {
        log.info("Soft deleting raw product with ID: {}", id);

        RawProduct product = rawProductRepository.findById(id)
                .orElseThrow(() -> new RawProductNotFoundException(id));

        product.setActive(false);
        rawProductRepository.save(product);

        log.info("Raw product soft deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public RawProductResponse getRawProductById(Long id) {
        log.info("Fetching raw product with ID: {}", id);

        RawProduct product = rawProductRepository.findById(id)
                .orElseThrow(() -> new RawProductNotFoundException(id));

        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RawProductResponse> getAllRawProducts() {
        log.info("Fetching all raw products");

        return rawProductRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RawProductResponse increaseStock(Long id, BigDecimal quantity) {
        log.info("Increasing stock for raw product ID: {} by quantity: {}", id, quantity);

        RawProduct product = rawProductRepository.findActiveById(id)
                .orElseThrow(() -> new RawProductNotFoundException(id));

        BigDecimal currentQty = product.getQuantity() != null ? product.getQuantity() : BigDecimal.ZERO;
        product.setQuantity(currentQty.add(quantity));
        RawProduct updatedProduct = rawProductRepository.save(product);

        BigDecimal price = updatedProduct.getPrice() != null ? updatedProduct.getPrice() : BigDecimal.ZERO;
        if (price.compareTo(BigDecimal.ZERO) > 0) {
            createInventoryLot(updatedProduct.getId(), quantity, price);
        }

        log.info("Stock increased successfully for raw product ID: {}", id);
        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public RawProductResponse decreaseStock(Long id, BigDecimal quantity) {
        log.info("Decreasing stock for raw product ID: {} by quantity: {}", id, quantity);

        RawProduct product = rawProductRepository.findActiveById(id)
                .orElseThrow(() -> new RawProductNotFoundException(id));

        BigDecimal currentQty = product.getQuantity() != null ? product.getQuantity() : BigDecimal.ZERO;
        if (currentQty.compareTo(quantity) < 0) {
            throw new InsufficientStockException(product.getMaterialCode(), quantity, currentQty);
        }

        product.setQuantity(currentQty.subtract(quantity));
        RawProduct updatedProduct = rawProductRepository.save(product);

        BigDecimal updatedQty = updatedProduct.getQuantity() != null ? updatedProduct.getQuantity() : BigDecimal.ZERO;
        BigDecimal threshold = updatedProduct.getMinimumThreshold() != null ? updatedProduct.getMinimumThreshold() : BigDecimal.ZERO;
        if (updatedQty.compareTo(threshold) <= 0) {
            log.warn("ALERT: Raw product {} (ID: {}) stock is below minimum threshold. Current: {}, Threshold: {}",
                    updatedProduct.getMaterialCode(), id, updatedProduct.getQuantity(), updatedProduct.getMinimumThreshold());
        }

        log.info("Stock decreased successfully for raw product ID: {}", id);
        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RawProductResponse> getLowStockItems() {
        log.info("Fetching low stock raw products");

        return rawProductRepository.findLowStockItems().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RawProductResponse mapToResponse(RawProduct product) {
        RawProductResponse response = new RawProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setMaterialCode(product.getMaterialCode());
        response.setUnit(product.getUnit());
        response.setQuantity(product.getQuantity());
        response.setPrice(product.getPrice());

        // Set per item price same as price
        BigDecimal perItemPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        response.setPerItemPrice(perItemPrice);

        response.setMinimumThreshold(product.getMinimumThreshold());
        response.setHsn(product.getHsn());
        response.setTaxRate(product.getTaxRate());
        response.setActive(product.getActive());
        BigDecimal qty = product.getQuantity() != null ? product.getQuantity() : BigDecimal.ZERO;
        BigDecimal minThreshold = product.getMinimumThreshold() != null ? product.getMinimumThreshold() : BigDecimal.ZERO;
        response.setLowStock(qty.compareTo(minThreshold) <= 0);
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        response.setVendorId(product.getVendorId());
        response.setVendorName(product.getVendorName());
        response.setTransportName(product.getTransportName());
        response.setDriverName(product.getDriverName());
        response.setDriverMobile(product.getDriverMobile());
        response.setStatus(product.getStatus());
        response.setImageUrl(product.getImageUrl());
        response.setRate(product.getRate());
        response.setGst(product.getGst());
        response.setGrossAmount(product.getGrossAmount());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public String getRawProductImageUrl(Long id) {
        RawProduct product = rawProductRepository.findById(id)
                .orElseThrow(() -> new RawProductNotFoundException(id));
        return product.getImageUrl();
    }

    @Override
    @Transactional(readOnly = true)
    public String getRawProductImageUrlByMaterialCode(String materialCode) {
        RawProduct product = rawProductRepository.findByMaterialCode(materialCode)
                .orElseThrow(() -> new RawProductNotFoundException(0L));
        return product.getImageUrl();
    }

    @Override
    @Transactional
    public RawProductResponse updateByMaterialCode(String materialCode, RawProductRequest request) {
        log.info("Updating raw product by material code: {}", materialCode);

        RawProduct product = rawProductRepository.findByMaterialCode(materialCode)
                .orElseThrow(() -> new RawProductNotFoundException(0L));

        BigDecimal oldPriceByCode = product.getPrice();
        BigDecimal oldQtyByCode = product.getQuantity() != null ? product.getQuantity() : BigDecimal.ZERO;

        if (request.getName() != null) product.setName(request.getName());
        if (request.getUnit() != null) product.setUnit(request.getUnit());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getQuantity() != null) product.setQuantity(request.getQuantity());
        if (request.getMinimumThreshold() != null) product.setMinimumThreshold(request.getMinimumThreshold());
        if (request.getHsn() != null) product.setHsn(request.getHsn());
        if (request.getTaxRate() != null) product.setTaxRate(request.getTaxRate());
        if (request.getVendorId() != null) product.setVendorId(request.getVendorId());
        if (request.getVendorName() != null) product.setVendorName(request.getVendorName());
        if (request.getTransportName() != null) product.setTransportName(request.getTransportName());
        if (request.getDriverName() != null) product.setDriverName(request.getDriverName());
        if (request.getDriverMobile() != null) product.setDriverMobile(request.getDriverMobile());
        if (request.getRate() != null) product.setRate(request.getRate());
        if (request.getGst() != null) product.setGst(request.getGst());
        if (request.getGrossAmount() != null) product.setGrossAmount(request.getGrossAmount());
        if (request.getStatus() != null) product.setStatus(request.getStatus());

        RawProduct updatedProduct = rawProductRepository.save(product);
        log.info("Raw product updated by material code: {}", materialCode);

        if (request.getQuantity() != null) {
            BigDecimal newQtyByCode = updatedProduct.getQuantity() != null ? updatedProduct.getQuantity() : BigDecimal.ZERO;
            BigDecimal diff = newQtyByCode.subtract(oldQtyByCode);
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal lotPrice = updatedProduct.getPrice() != null ? updatedProduct.getPrice() : BigDecimal.ZERO;
                if (lotPrice.compareTo(BigDecimal.ZERO) > 0) {
                    createInventoryLot(updatedProduct.getId(), diff, lotPrice);
                }
            }
        }

        BigDecimal newPriceByCode = updatedProduct.getPrice();
        if (request.getPrice() != null && oldPriceByCode != null
                && oldPriceByCode.compareTo(newPriceByCode) != 0) {
            bomPriceChangeService.handleRawMaterialPriceChange(updatedProduct, oldPriceByCode, newPriceByCode);
        }

        return mapToResponse(updatedProduct);
    }

    private void createInventoryLot(Long rawMaterialId, BigDecimal quantity, BigDecimal price) {
        RawMaterialInventoryLot lot = RawMaterialInventoryLot.builder()
                .rawMaterialId(rawMaterialId)
                .quantityOriginal(quantity)
                .quantityRemaining(quantity)
                .pricePerUnit(price)
                .build();
        inventoryLotRepository.save(lot);
        log.info("Created FIFO inventory lot for raw material ID {}: {} units @ {}", rawMaterialId, quantity, price);
    }
}