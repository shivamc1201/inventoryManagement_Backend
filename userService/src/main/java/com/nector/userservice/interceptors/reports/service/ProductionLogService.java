package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.ProductionLogDto;

public interface ProductionLogService {
    ProductionLogDto create(ProductionLogDto dto);
    ProductionLogDto getById(Long id);
    ProductionLogDto update(Long id, ProductionLogDto dto);
    void delete(Long id);
}
