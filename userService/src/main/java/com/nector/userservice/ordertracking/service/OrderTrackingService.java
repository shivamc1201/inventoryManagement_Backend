package com.nector.userservice.ordertracking.service;

import com.nector.userservice.ordertracking.dto.*;
import com.nector.userservice.ordertracking.entity.*;
import com.nector.userservice.ordertracking.repository.*;
import com.nector.userservice.repository.UserRepository;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderTrackingService {

    private final OrderTrackingRepository orderRepo;
    private final OrderTrackingStepRepository stepRepo;
    private final UserRepository userRepository;
    private final DistributorRepository distributorRepository;
    
    // Public method to get repository for external services
    public OrderTrackingRepository getOrderRepository() {
        return orderRepo;
    }

    // ── 1. List orders with filters ──────────────────────────────────────────

    public OrderTrackingListResponse listOrders(String search, String status,
                                              int page, int size) {
        String searchParam = (search == null || search.isBlank()) ? null : search.trim();
        String statusParam = (status == null || "all".equalsIgnoreCase(status)) ? null : status.trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        Page<OrderTracking> result = orderRepo.findFiltered(searchParam, statusParam, pageable);

        List<OrderTrackingDTO> orders = result.getContent()
            .stream().map(this::toDTO).collect(Collectors.toList());

        return OrderTrackingListResponse.builder()
            .orders(orders)
            .stats(buildStats())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .build();
    }

    // ── 2. Single order ───────────────────────────────────────────────────────

    public OrderTrackingDTO getById(Long id) {
        OrderTracking order = orderRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
        return toDTO(order);
    }

    // ── 3. Stats only ─────────────────────────────────────────────────────────

    public OrderTrackingStatsDTO getStats() {
        return buildStats();
    }

    // ── 4. Update a step ──────────────────────────────────────────────────────

    @Transactional
    public OrderStepDTO updateStep(Long orderId, Long stepId, UpdateStepRequest req) {
        OrderTrackingStep step = stepRepo.findByIdAndOrderId(stepId, orderId)
            .orElseThrow(() -> new EntityNotFoundException("Step not found"));

        return updateStepInternal(step, req);
    }

    @Transactional
    public OrderStepDTO updateStepBySequence(Long orderId, Integer stepSequence, UpdateStepRequest req) {
        OrderTrackingStep step = stepRepo.findByOrderIdAndStepSequence(orderId, stepSequence)
            .orElseThrow(() -> new EntityNotFoundException("Step not found with sequence: " + stepSequence));

        return updateStepInternal(step, req);
    }

    private OrderStepDTO updateStepInternal(OrderTrackingStep step, UpdateStepRequest req) {
        if (req.getStatus() != null)
            step.setStatus(StepStatus.from(req.getStatus()));
        if (req.getDate() != null)
            step.setStepDate(LocalDate.parse(req.getDate()));
        if (req.getRemarks() != null)
            step.setRemarks(req.getRemarks());
            
        // Update assigned person information
        if (req.getAssignedPersonId() != null)
            step.setAssignedPersonId(req.getAssignedPersonId());
        if (req.getAssignedPersonName() != null)
            step.setAssignedPersonName(req.getAssignedPersonName());
        if (req.getAssignedPersonRole() != null)
            step.setAssignedPersonRole(req.getAssignedPersonRole());
        if (req.getAssignedPersonPhone() != null)
            step.setAssignedPersonPhone(req.getAssignedPersonPhone());
        if (req.getAssignedPersonEmail() != null)
            step.setAssignedPersonEmail(req.getAssignedPersonEmail());
            
        // Update document information
        step.setHasDownload(req.isHasDownload());
        if (req.getDownloadLabel() != null)
            step.setDownloadLabel(req.getDownloadLabel());
        if (req.getDocumentPath() != null)
            step.setDocumentPath(req.getDocumentPath());
            
        // Update action response (for step 11)
        step.setHasAction(req.isHasAction());
        if (req.getActionResponse() != null)
            step.setActionResponse(req.getActionResponse());

        // If this step is now completed or cancelled,
        // advance the next step to IN_PROGRESS automatically
        if (step.getStatus() == StepStatus.COMPLETED) {
            stepRepo.findByOrderIdAndStepSequence(step.getOrder().getId(), step.getStepSequence() + 1)
                .ifPresent(next -> {
                    if (next.getStatus() == StepStatus.PENDING) {
                        next.setStatus(StepStatus.IN_PROGRESS);
                        stepRepo.save(next);
                    }
                });
            
            // Complete previous step if it was IN_PROGRESS
            if (step.getStepSequence() > 1) {
                stepRepo.findByOrderIdAndStepSequence(step.getOrder().getId(), step.getStepSequence() - 1)
                    .ifPresent(previous -> {
                        if (previous.getStatus() == StepStatus.IN_PROGRESS) {
                            previous.setStatus(StepStatus.COMPLETED);
                            stepRepo.save(previous);
                        }
                    });
            }
        }

        // If cancelled, propagate cancellation to all subsequent steps
        if (step.getStatus() == StepStatus.CANCELLED) {
            stepRepo.findByOrderIdOrderByStepSequenceAsc(step.getOrder().getId()).stream()
                .filter(s -> s.getStepSequence() > step.getStepSequence()
                            && s.getStatus() == StepStatus.PENDING)
                .forEach(s -> { s.setStatus(StepStatus.CANCELLED); stepRepo.save(s); });
        }

        return toStepDTO(stepRepo.save(step));
    }

    // ── 5. Mark order received (Step 11 YES/NO) ───────────────────────────────

    @Transactional
    public OrderStepDTO markReceived(Long orderId, OrderReceivedRequest req) {
        OrderTrackingStep step = stepRepo
            .findByOrderIdAndStepSequence(orderId, 11)
            .orElseThrow(() -> new EntityNotFoundException("Step 11 not found for order: " + orderId));

        step.setActionResponse(req.isReceived() ? "yes" : "no");
        step.setStatus(req.isReceived() ? StepStatus.COMPLETED : StepStatus.CANCELLED);
        step.setStepDate(LocalDate.now());
        if (req.getRemarks() != null) step.setRemarks(req.getRemarks());

        return toStepDTO(stepRepo.save(step));
    }

    // ── 6. Create a new order with all 11 steps pre-seeded ─────────────────--

    @Transactional
    public OrderTrackingDTO createOrder(CreateOrderTrackingRequest req) {
        OrderTracking order = OrderTracking.builder()
            .orderNumber(req.getOrderNumber())
            .distributorId(req.getDistributorId())
            .distributorName(req.getDistributorName())
            .orderDate(req.getOrderDate() != null ? req.getOrderDate() : LocalDate.now())
            .totalAmount(req.getTotalAmount())
            .createdBy(req.getCreatedBy())
            .build();

        order.setSteps(buildDefaultSteps(order));
        return toDTO(orderRepo.save(order));
    }

    // ── 7. Create OrderTracking from existing Cart ───────────────────────────

    @Transactional
    public OrderTrackingDTO createFromCart(Long cartId, String distributorName, Long distributorId, 
                                          String orderNumber, java.math.BigDecimal totalAmount) {
        log.info("Creating OrderTracking from cart {} for distributor {}", cartId, distributorId);
        
        OrderTracking order = OrderTracking.builder()
            .orderNumber(orderNumber)
            .distributorId(distributorId)
            .distributorName(distributorName)
            .orderDate(LocalDate.now())
            .totalAmount(totalAmount)
            .build();

        order.setSteps(buildDefaultSteps(order));
        return toDTO(orderRepo.save(order));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private OrderTrackingStatsDTO buildStats() {
        long total = orderRepo.count();
        long completed = orderRepo.countCompleted();
        long pending = orderRepo.countPending();
        return OrderTrackingStatsDTO.builder()
            .totalOrders(total)
            .completedOrders(completed)
            .pendingOrders(pending)
            .build();
    }

    private OrderTrackingDTO toDTO(OrderTracking o) {
        // Ensure steps are loaded (LAZY) in sorted order
        List<OrderTrackingStep> steps = stepRepo
            .findByOrderIdOrderByStepSequenceAsc(o.getId());

        return OrderTrackingDTO.builder()
            .id(o.getId())
            .orderNumber(o.getOrderNumber())
            .distributorName(o.getDistributorName())
            .distributorId(o.getDistributorId())
            .orderDate(o.getOrderDate())
            .totalAmount(o.getTotalAmount())
            .steps(steps.stream().map(this::toStepDTO).collect(Collectors.toList()))
            .build();
    }

    private OrderStepDTO toStepDTO(OrderTrackingStep s) {
        AssignedPersonDTO person = null;
        if (s.getAssignedPersonName() != null) {
            person = AssignedPersonDTO.builder()
                .name(s.getAssignedPersonName())
                .role(s.getAssignedPersonRole())
                .contact(s.getAssignedPersonPhone())
                .email(s.getAssignedPersonEmail())
                .build();
        }

        return OrderStepDTO.builder()
            .id(s.getId())
            .stepSequence(s.getStepSequence())
            .label(s.getLabel())
            .status(s.getStatus().getValue())      // lowercase for frontend
            .date(s.getStepDate() != null ? s.getStepDate().toString() : null)
            .remarks(s.getRemarks())
            .assignedPerson(person)
            .hasDownload(s.isHasDownload())
            .downloadLabel(s.getDownloadLabel())
            .hasAction(s.isHasAction())
            .actionResponse(s.getActionResponse())
            .build();
    }

    /**
     * Seeds all 11 workflow steps for a brand-new order.
     * Step 1 is auto-completed (order placement = immediate).
     * Step 4 (PI), Step 9 (GDN) get hasDownload = true.
     * Step 11 gets hasAction = true.
     */
    private List<OrderTrackingStep> buildDefaultSteps(OrderTracking order) {
        record StepDef(String label, boolean hasDownload, String downloadLabel, boolean hasAction) {}

        var defs = List.of(
            new StepDef("Order Placed",                           false, null,                        false),
            new StepDef("Pending Approval from Sales",            false, null,                        false),
            new StepDef("Approved from Sales",                    false, null,                        false),
            new StepDef("Proforma Invoice Generated",             true,  "Download Proforma Invoice",   false),
            new StepDef("Awaiting Payment Confirmation from Accounts", false, null,                    false),
            new StepDef("Approved from Accounts",                 false, null,                        false),
            new StepDef("Awaiting Confirmation from Logistics",   false, null,                        false),
            new StepDef("Approved from Logistics",                false, null,                        false),
            new StepDef("GDN Generated",                          true,  "Download GDN",                false),
            new StepDef("Order is On the Way",                    false, null,                        false),
            new StepDef("Order Received",                         false, null,                        true)
        );

        return java.util.stream.IntStream.range(0, defs.size()).mapToObj(i -> {
            StepDef d = defs.get(i);
            var stepBuilder = OrderTrackingStep.builder()
                .order(order)
                .stepSequence(i + 1)
                .label(d.label())
                .status(i == 0 ? StepStatus.COMPLETED : (i == 1 ? StepStatus.IN_PROGRESS : StepStatus.PENDING))
                .stepDate(i == 0 ? LocalDate.now() : null)
                .remarks(i == 0 ? "Order submitted by distributor" : null)
                .hasDownload(d.hasDownload())
                .downloadLabel(d.downloadLabel())
                .hasAction(d.hasAction());
            
            // Add assigned person information for Step 1 (Order Placed)
            if (i == 0) {
                stepBuilder
                    .assignedPersonId(order.getDistributorId())
                    .assignedPersonName(order.getDistributorName())
                    .assignedPersonRole("DISTRIBUTOR");
                    
                // Get distributor details
                distributorRepository.findById(order.getDistributorId()).ifPresent(distributor -> {
                    stepBuilder
                        .assignedPersonEmail(distributor.getContactEmail())
                        .assignedPersonPhone(distributor.getPhoneNumber());
                });
            }
            
            return stepBuilder.build();
        }).collect(Collectors.toList());
    }
}
