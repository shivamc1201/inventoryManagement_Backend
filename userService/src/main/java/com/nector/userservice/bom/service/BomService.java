package com.nector.userservice.bom.service;

import com.nector.userservice.bom.dto.BomProductionRequestDto;
import com.nector.userservice.bom.dto.BomProductionResponseDto;
import com.nector.userservice.bom.dto.BomRequestDto;
import com.nector.userservice.bom.dto.BomResponseDto;
import com.nector.userservice.bom.dto.BomSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BomService {

    BomResponseDto create(BomRequestDto requestDto);

    BomResponseDto update(Long id, BomRequestDto requestDto);

    Page<BomResponseDto> getAll(Pageable pageable);

    BomResponseDto getById(Long id);

    void delete(Long id);

    Page<BomResponseDto> search(String keyword, Pageable pageable);

    BomSummaryDto getSummary();

    BomProductionResponseDto produceFinishedProduct(BomProductionRequestDto requestDto);
}
