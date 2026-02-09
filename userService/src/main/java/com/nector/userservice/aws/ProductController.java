package com.nector.userservice.aws;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final S3ImageService imageService;
    private final ProductRepository productRepository;

    public ProductController(S3ImageService imageService,
                             ProductRepository productRepository) {
        this.imageService = imageService;
        this.productRepository = productRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product createProduct(
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam("image") MultipartFile image
    ) throws IOException {

        String imageUrl = imageService.uploadProductImage(image);

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setImageUrl(imageUrl);

        return productRepository.save(product);
    }
}

