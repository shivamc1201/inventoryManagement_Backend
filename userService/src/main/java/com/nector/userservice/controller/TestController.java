package com.nector.userservice.controller;

import com.nector.userservice.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    
    private final SaleRepository saleRepository;
    
    @GetMapping("/sales-count")
    public String getSalesCount() {
        long count = saleRepository.count();
        return "Total sales records: " + count;
    }
}