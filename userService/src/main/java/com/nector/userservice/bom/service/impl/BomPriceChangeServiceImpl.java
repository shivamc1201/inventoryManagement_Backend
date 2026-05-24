package com.nector.userservice.bom.service.impl;

import com.nector.userservice.bom.dto.BomPriceChangeResponseDto;
import com.nector.userservice.bom.dto.PriceChangeActionDto;
import com.nector.userservice.bom.dto.RawMaterialPriceHistoryDto;
import com.nector.userservice.bom.entity.*;
import com.nector.userservice.bom.repository.*;
import com.nector.userservice.bom.service.BomPriceChangeService;
import com.nector.userservice.bom.service.BomService;
import com.nector.userservice.exception.ResourceNotFoundException;
import com.nector.userservice.model.FinishedProduct;
import com.nector.userservice.model.RawProduct;
import com.nector.userservice.repository.FinishedProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BomPriceChangeServiceImpl implements BomPriceChangeService {

    private final BomPriceChangeRequestRepository priceChangeRequestRepository;
    private final RawMaterialPriceHistoryRepository priceHistoryRepository;
    private final BomComponentRepository bomComponentRepository;
    private final BillOfMaterialRepository billOfMaterialRepository;
    private final FinishedProductRepository finishedProductRepository;
    private final BomService bomService;

    @Override
    @Transactional
    public void handleRawMaterialPriceChange(RawProduct rawProduct, BigDecimal oldPrice, BigDecimal newPrice) {
        BigDecimal changePercent = computeChangePercent(oldPrice, newPrice);

        RawMaterialPriceHistory history = RawMaterialPriceHistory.builder()
                .rawMaterialId(rawProduct.getId())
                .rawMaterialName(rawProduct.getName())
                .materialCode(rawProduct.getMaterialCode())
                .oldPrice(oldPrice)
                .newPrice(newPrice)
                .priceChangePercent(changePercent)
                .build();
        history = priceHistoryRepository.save(history);
        log.info("Saved price history for raw material '{}' (ID {}): {} → {}",
                rawProduct.getName(), rawProduct.getId(), oldPrice, newPrice);

        List<BomComponent> affectedComponents = bomComponentRepository.findByRawMaterialId(rawProduct.getId());
        if (affectedComponents.isEmpty()) {
            log.info("No BOM components found for raw material ID {}, skipping BOM recompute", rawProduct.getId());
            return;
        }

        // Collect distinct BOM IDs and recompute each once
        affectedComponents.stream()
                .map(c -> c.getBom().getId())
                .distinct()
                .forEach(bomId -> {
                    bomService.recomputeBomCosts(bomId);

                    BillOfMaterial bom = billOfMaterialRepository.findById(bomId).orElse(null);
                    if (bom != null && bom.getEffectiveRatePerUnit() != null) {
                        finishedProductRepository.findById(bom.getFinishedProductId()).ifPresent(fp -> {
                            fp.setPrice(bom.getEffectiveRatePerUnit());
                            finishedProductRepository.save(fp);
                            log.info("Auto-updated finished product '{}' price to {} via FIFO recompute",
                                    fp.getName(), bom.getEffectiveRatePerUnit());
                        });
                    }
                    log.info("Auto-recomputed BOM costs for BOM ID {} after raw material '{}' price change {} → {}",
                            bomId, rawProduct.getName(), oldPrice, newPrice);
                });

        // Admin approval flow is disabled — BOM costs are updated automatically above.
        // To re-enable maker-checker approval, uncomment the block below and remove the auto-recompute above.
        /*
        final Long historyId = history.getId();
        for (BomComponent component : affectedComponents) {
            if (priceChangeRequestRepository.existsByBomComponentIdAndStatus(
                    component.getId(), BomPriceChangeStatus.PENDING)) {
                log.warn("Pending price change request already exists for BOM component ID {}, skipping", component.getId());
                continue;
            }
            BillOfMaterial bom = component.getBom();
            BomPriceChangeRequest request = BomPriceChangeRequest.builder()
                    .rawMaterialId(rawProduct.getId())
                    .rawMaterialName(rawProduct.getName())
                    .bomId(bom.getId())
                    .bomName(bom.getBomName())
                    .bomComponentId(component.getId())
                    .finishedProductId(bom.getFinishedProductId())
                    .finishedProductName(bom.getFinishedProductName())
                    .oldRate(component.getRate())
                    .newRate(newPrice)
                    .priceChangePercent(changePercent)
                    .status(BomPriceChangeStatus.PENDING)
                    .priceHistoryId(historyId)
                    .build();
            priceChangeRequestRepository.save(request);
            log.info("Created PENDING price change request for BOM '{}' component '{}': rate {} → {}",
                    bom.getBomName(), rawProduct.getName(), component.getRate(), newPrice);
        }
        */
    }

    @Override
    @Transactional(readOnly = true)
    public List<BomPriceChangeResponseDto> getPendingChanges() {
        return priceChangeRequestRepository
                .findByStatusOrderByRequestedAtDesc(BomPriceChangeStatus.PENDING)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BomPriceChangeResponseDto> getPendingChangesByRawMaterial(Long rawMaterialId) {
        return priceChangeRequestRepository
                .findByRawMaterialIdAndStatusOrderByRequestedAtDesc(rawMaterialId, BomPriceChangeStatus.PENDING)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BomPriceChangeResponseDto> getPendingChangesByBom(Long bomId) {
        return priceChangeRequestRepository
                .findByBomIdAndStatusOrderByRequestedAtDesc(bomId, BomPriceChangeStatus.PENDING)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BomPriceChangeResponseDto> getAllChanges() {
        return priceChangeRequestRepository.findAllByOrderByRequestedAtDesc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BomPriceChangeResponseDto approve(Long requestId, PriceChangeActionDto action) {
        BomPriceChangeRequest request = loadPending(requestId);
        applyPriceChange(request);
        resolveRequest(request, BomPriceChangeStatus.APPROVED, action);
        log.info("Approved price change request ID {} for BOM '{}'", requestId, request.getBomName());
        return toDto(request);
    }

    @Override
    @Transactional
    public BomPriceChangeResponseDto reject(Long requestId, PriceChangeActionDto action) {
        BomPriceChangeRequest request = loadPending(requestId);
        resolveRequest(request, BomPriceChangeStatus.REJECTED, action);
        log.info("Rejected price change request ID {} for BOM '{}'", requestId, request.getBomName());
        return toDto(request);
    }

    @Override
    @Transactional
    public List<BomPriceChangeResponseDto> bulkApprove(Long rawMaterialId, PriceChangeActionDto action) {
        List<BomPriceChangeRequest> pending = priceChangeRequestRepository
                .findByRawMaterialIdAndStatusOrderByRequestedAtDesc(rawMaterialId, BomPriceChangeStatus.PENDING);
        for (BomPriceChangeRequest request : pending) {
            applyPriceChange(request);
            resolveRequest(request, BomPriceChangeStatus.APPROVED, action);
        }
        log.info("Bulk-approved {} price change requests for raw material ID {}", pending.size(), rawMaterialId);
        return pending.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BomPriceChangeResponseDto> bulkReject(Long rawMaterialId, PriceChangeActionDto action) {
        List<BomPriceChangeRequest> pending = priceChangeRequestRepository
                .findByRawMaterialIdAndStatusOrderByRequestedAtDesc(rawMaterialId, BomPriceChangeStatus.PENDING);
        for (BomPriceChangeRequest request : pending) {
            resolveRequest(request, BomPriceChangeStatus.REJECTED, action);
        }
        log.info("Bulk-rejected {} price change requests for raw material ID {}", pending.size(), rawMaterialId);
        return pending.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RawMaterialPriceHistoryDto> getPriceHistory(Long rawMaterialId) {
        return priceHistoryRepository.findByRawMaterialIdOrderByChangedAtDesc(rawMaterialId)
                .stream().map(this::toHistoryDto).collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private BomPriceChangeRequest loadPending(Long requestId) {
        BomPriceChangeRequest request = priceChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Price change request not found with ID: " + requestId));
        if (request.getStatus() != BomPriceChangeStatus.PENDING) {
            throw new IllegalStateException("Request ID " + requestId + " is already " + request.getStatus());
        }
        return request;
    }

    private void applyPriceChange(BomPriceChangeRequest request) {
        BomComponent component = bomComponentRepository.findById(request.getBomComponentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BOM component not found with ID: " + request.getBomComponentId()));

        component.setRate(request.getNewRate());
        bomComponentRepository.save(component);

        // Recompute all BOM cost fields (totalComponentCost, effectiveCost, effectiveRatePerUnit, etc.)
        bomService.recomputeBomCosts(request.getBomId());

        // Sync the finished product's selling price to the new effective rate per unit
        BillOfMaterial bom = billOfMaterialRepository.findById(request.getBomId())
                .orElseThrow(() -> new ResourceNotFoundException("BOM not found with ID: " + request.getBomId()));

        if (bom.getEffectiveRatePerUnit() != null) {
            FinishedProduct fp = finishedProductRepository.findById(request.getFinishedProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Finished product not found with ID: " + request.getFinishedProductId()));
            fp.setPrice(bom.getEffectiveRatePerUnit());
            finishedProductRepository.save(fp);
            log.info("Updated finished product '{}' price to {} (new BOM effective rate per unit)",
                    fp.getName(), bom.getEffectiveRatePerUnit());
        }
    }

    private void resolveRequest(BomPriceChangeRequest request, BomPriceChangeStatus status, PriceChangeActionDto action) {
        request.setStatus(status);
        request.setResolvedAt(Instant.now());
        if (action != null) {
            request.setResolvedBy(action.getResolvedBy());
            request.setNotes(action.getNotes());
        }
        priceChangeRequestRepository.save(request);
    }

    private BigDecimal computeChangePercent(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice == null || oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return newPrice.subtract(oldPrice)
                .divide(oldPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BomPriceChangeResponseDto toDto(BomPriceChangeRequest r) {
        return BomPriceChangeResponseDto.builder()
                .id(r.getId())
                .rawMaterialId(r.getRawMaterialId())
                .rawMaterialName(r.getRawMaterialName())
                .bomId(r.getBomId())
                .bomName(r.getBomName())
                .bomComponentId(r.getBomComponentId())
                .finishedProductId(r.getFinishedProductId())
                .finishedProductName(r.getFinishedProductName())
                .oldRate(r.getOldRate())
                .newRate(r.getNewRate())
                .priceChangePercent(r.getPriceChangePercent())
                .status(r.getStatus())
                .priceHistoryId(r.getPriceHistoryId())
                .requestedAt(r.getRequestedAt())
                .resolvedAt(r.getResolvedAt())
                .resolvedBy(r.getResolvedBy())
                .notes(r.getNotes())
                .build();
    }

    private RawMaterialPriceHistoryDto toHistoryDto(RawMaterialPriceHistory h) {
        return RawMaterialPriceHistoryDto.builder()
                .id(h.getId())
                .rawMaterialId(h.getRawMaterialId())
                .rawMaterialName(h.getRawMaterialName())
                .materialCode(h.getMaterialCode())
                .oldPrice(h.getOldPrice())
                .newPrice(h.getNewPrice())
                .priceChangePercent(h.getPriceChangePercent())
                .changedAt(h.getChangedAt())
                .build();
    }
}
