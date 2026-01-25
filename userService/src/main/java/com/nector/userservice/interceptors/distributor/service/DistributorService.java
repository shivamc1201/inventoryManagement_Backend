package com.nector.userservice.interceptors.distributor.service;

import com.nector.userservice.interceptors.distributor.model.DistributorRequestDTO;
import com.nector.userservice.interceptors.distributor.model.DistributorResponseDTO;
import java.util.List;

public interface DistributorService {
    DistributorResponseDTO createDistributor(DistributorRequestDTO request);
    DistributorResponseDTO getDistributorById(Long id);
    List<DistributorResponseDTO> getAllDistributors();
    DistributorResponseDTO updateDistributor(Long id, DistributorRequestDTO request);
    void deleteDistributor(Long id);
}