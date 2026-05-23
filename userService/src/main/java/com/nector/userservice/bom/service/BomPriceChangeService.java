package com.nector.userservice.bom.service;

import com.nector.userservice.bom.dto.BomPriceChangeResponseDto;
import com.nector.userservice.bom.dto.PriceChangeActionDto;
import com.nector.userservice.bom.dto.RawMaterialPriceHistoryDto;
import com.nector.userservice.model.RawProduct;

import java.math.BigDecimal;
import java.util.List;

public interface BomPriceChangeService {

    /** Called by RawProductServiceImpl whenever a price change is detected. */
    void handleRawMaterialPriceChange(RawProduct rawProduct, BigDecimal oldPrice, BigDecimal newPrice);

    List<BomPriceChangeResponseDto> getPendingChanges();

    List<BomPriceChangeResponseDto> getPendingChangesByRawMaterial(Long rawMaterialId);

    List<BomPriceChangeResponseDto> getPendingChangesByBom(Long bomId);

    List<BomPriceChangeResponseDto> getAllChanges();

    BomPriceChangeResponseDto approve(Long requestId, PriceChangeActionDto action);

    BomPriceChangeResponseDto reject(Long requestId, PriceChangeActionDto action);

    List<BomPriceChangeResponseDto> bulkApprove(Long rawMaterialId, PriceChangeActionDto action);

    List<BomPriceChangeResponseDto> bulkReject(Long rawMaterialId, PriceChangeActionDto action);

    List<RawMaterialPriceHistoryDto> getPriceHistory(Long rawMaterialId);
}
