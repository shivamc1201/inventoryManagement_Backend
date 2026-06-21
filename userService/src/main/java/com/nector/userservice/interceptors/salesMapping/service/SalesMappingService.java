package com.nector.userservice.interceptors.salesMapping.service;

import com.nector.userservice.interceptors.salesMapping.model.CreateMappingRequestDTO;
import com.nector.userservice.interceptors.salesMapping.model.MappingResponseDTO;
import java.util.List;

public interface SalesMappingService {
    MappingResponseDTO createMapping(CreateMappingRequestDTO request, String createdBy);
    List<MappingResponseDTO> getMappingsBySalesperson(Long salespersonId);
    List<MappingResponseDTO> getMappingsByDistributor(Long distributorId);
    List<MappingResponseDTO> getAllMappings();
    void deactivateMapping(Long mappingId);
}