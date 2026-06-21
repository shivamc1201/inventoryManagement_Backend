package com.nector.userservice.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    
    private final S3ImageService imageService;
    private final ProductRepository productRepository;

    public ProductController(S3ImageService imageService, ProductRepository productRepository) {
        this.imageService = imageService;
        this.productRepository = productRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> createProduct(
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam("image") MultipartFile image
    ) throws IOException {
        
        log.info("Creating product: {}, price: {}", name, price);
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image is required");
        }

        String imageUrl = imageService.uploadProductImage(image);
        log.info("Image uploaded successfully: {}", imageUrl);

        Product product = new Product();
        product.setName(name.trim());
        product.setPrice(price);
        product.setImageUrl(imageUrl);

        Product savedProduct = productRepository.save(product);
        log.info("Product saved to database with ID: {}", savedProduct.getId());
        
        return ResponseEntity.ok(savedProduct);
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        log.info("Retrieved {} products", products.size());
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

