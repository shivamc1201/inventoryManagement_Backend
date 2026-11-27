package com.nector.userservice.interceptors.order;

import com.nector.userservice.interceptors.distributor.model.Distributor;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.model.Cart;
import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.repository.CartRepository;
import com.nector.userservice.repository.SalesPersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HierarchyOrderService {

    /** Statuses that are real orders. ACTIVE / CHECKED_OUT are still shopping carts. */
    private static final List<Cart.CartStatus> ORDER_STATUSES = List.of(
            Cart.CartStatus.PLACED,
            Cart.CartStatus.APPROVED,
            Cart.CartStatus.PAYMENT_APPROVED,
            Cart.CartStatus.GDN_GENERATED,
            Cart.CartStatus.DISMISSED,
            Cart.CartStatus.GDN_REJECTED);

    private final CartRepository cartRepository;
    private final DistributorRepository distributorRepository;
    private final SalesPersonRepository salesPersonRepository;

    /** All orders with their owning salesperson. Every filter is optional. */
    @Transactional(readOnly = true)
    public List<HierarchyOrderResponse> getOrders(String zone, String role, Long salespersonId, String status) {
        List<Cart> carts = cartRepository.findByStatusIn(ORDER_STATUSES);

        Map<Long, Distributor> distributors = distributorRepository
                .findAllById(carts.stream().map(Cart::getDistributorId).filter(Objects::nonNull).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Distributor::getId, Function.identity()));

        Map<Long, SalesPerson> salesPersons = salesPersonRepository.findAll().stream()
                .collect(Collectors.toMap(SalesPerson::getId, Function.identity()));

        return carts.stream()
                .map(cart -> toResponse(cart, distributors.get(cart.getDistributorId()), salesPersons))
                .filter(o -> isBlank(zone) || zone.equalsIgnoreCase(o.getZone()))
                .filter(o -> isBlank(role) || role.equalsIgnoreCase(o.getSalespersonRole()))
                .filter(o -> salespersonId == null || salespersonId.equals(o.getSalespersonId()))
                .filter(o -> isBlank(status) || status.equalsIgnoreCase(o.getStatus()))
                .sorted(Comparator.comparing(HierarchyOrderResponse::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private HierarchyOrderResponse toResponse(Cart cart, Distributor distributor,
                                              Map<Long, SalesPerson> salesPersons) {
        // Carts only get salespersonId stamped on approval, so fall back to the distributor's salesperson.
        Long ownerId = cart.getSalespersonId() != null
                ? cart.getSalespersonId()
                : (distributor != null ? distributor.getSalespersonId() : null);
        SalesPerson owner = ownerId != null ? salesPersons.get(ownerId) : null;

        String distributorName = cart.getDistributorName() != null
                ? cart.getDistributorName()
                : (distributor != null ? distributor.getFirstName() : null);

        return HierarchyOrderResponse.builder()
                .orderId(cart.getId())
                .distributorId(cart.getDistributorId())
                .distributorName(distributorName)
                .amount(amountOf(cart))
                .status(toScreenStatus(cart.getStatus()))
                .createdAt(cart.getCreatedAt())
                .salespersonId(ownerId)
                .salespersonName(owner != null ? owner.getName() : cart.getSalespersonName())
                .salespersonRole(owner != null && owner.getRole() != null ? owner.getRole().name() : null)
                .zone(owner != null ? owner.getZone() : null)
                .build();
    }

    private static BigDecimal amountOf(Cart cart) {
        if (cart.getTotalCartAmount() != null) {
            return cart.getTotalCartAmount();
        }
        return cart.getCartItems().stream()
                .filter(i -> i.getPriceAtTime() != null && i.getQuantity() != null)
                .map(i -> i.getPriceAtTime().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Only PLACED maps to "pending", because it is the only status the approve endpoint accepts. */
    private static String toScreenStatus(Cart.CartStatus status) {
        return switch (status) {
            case PLACED -> "pending";
            case GDN_GENERATED -> "completed";
            case DISMISSED, GDN_REJECTED -> "rejected";
            default -> "approved";
        };
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
