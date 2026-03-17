package com.nector.userservice.service;

import com.nector.userservice.dto.cart.AddToCartRequest;
import com.nector.userservice.dto.cart.CartItemResponse;
import com.nector.userservice.dto.cart.CartResponse;

import com.nector.userservice.dto.cart.PlaceOrderRequest;
import com.nector.userservice.exception.CartItemNotFoundException;
import com.nector.userservice.exception.CartNotFoundException;
import com.nector.userservice.exception.InvalidCartStatusException;
import com.nector.userservice.exception.ItemNotFoundException;
import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.model.Cart;
import com.nector.userservice.model.CartItem;
import com.nector.userservice.model.FinishedProduct;
import com.nector.userservice.model.Item;
import com.nector.userservice.repository.CartItemRepository;
import com.nector.userservice.repository.CartRepository;
import com.nector.userservice.repository.FinishedProductRepository;
import com.nector.userservice.repository.ItemRepository;
import com.nector.userservice.interceptors.salesMapping.repository.SalesMappingRepository;
import com.nector.userservice.dto.invoice.ProformaInvoice;
import com.nector.userservice.service.HtmlToPdfService;
import com.nector.userservice.interceptors.salesMapping.model.SalespersonDistributorMapping;
import com.nector.userservice.interceptors.salesMapping.model.MappingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FinishedProductRepository finishedProductRepository;
    private final ProformaInvoiceService proformaInvoiceService;
    private final SalesMappingRepository salesMappingRepository;
    private final DistributorRepository distributorRepository;


    // TODO the dispatch team wil, check the real  quantity of the orders  while checking GDN

    private final HtmlToPdfService htmlToPdfService;


    @Transactional
    public CartResponse addItemsToCart(Long distributorId, List<AddToCartRequest> requests) {
        log.info("Adding {} items to cart for distributor {}", requests.size(), distributorId);
        Cart cart = getOrCreateActiveCart(distributorId);
        
        for (AddToCartRequest request : requests) {
            log.info("Processing item {} for distributor {}", request.getItemId(), distributorId);
            try {
                FinishedProduct finishedProduct = finishedProductRepository.findBySku(request.getItemId())
                        .filter(FinishedProduct::getActive)
                        .orElseThrow(() -> new ItemNotFoundException("Item with SKU '" + request.getItemId() + "' not found or inactive"));
                
                // Verify the finished product exists in database
                if (!finishedProductRepository.existsById(finishedProduct.getId())) {
                    throw new ItemNotFoundException("Item with ID " + finishedProduct.getId() + " does not exist");
                }

                Optional<CartItem> existingCartItem =
                        cartItemRepository.findByCartIdAndItemId(cart.getId(), finishedProduct.getId());
                        
                if (existingCartItem.isPresent()) {
                    CartItem cartItem = existingCartItem.get();
                    int newQuantity = cartItem.getQuantity() + request.getQuantity();
                    cartItem.setQuantity(newQuantity);
                    cartItemRepository.save(cartItem);
                } else {
                    CartItem cartItem = new CartItem();
                    cartItem.setCart(cart);
                    cartItem.setItem(finishedProduct);
                    cartItem.setQuantity(request.getQuantity());
                    cartItem.setPriceAtTime(finishedProduct.getPrice());
                    cartItemRepository.save(cartItem);
                    cart.getCartItems().add(cartItem);
                }
            } catch (Exception e) {
                log.error("Failed to add item {} to cart for distributor {}: {}", request.getItemId(), distributorId, e.getMessage());
                throw e;
            }
        }
        
        cart.setStatus(Cart.CartStatus.ACTIVE);
        Cart updatedCart = cartRepository.save(cart);
        log.info("All items added to cart successfully for distributor {}", distributorId);
        return mapToResponse(updatedCart);
    }



    @Transactional
    public CartResponse removeItemFromCart(Long cartItemId) {
        log.info("Removing cart item {}", cartItemId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new CartItemNotFoundException("Cart item with ID " + cartItemId + " not found"));
        
        Cart cart = cartItem.getCart();
        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        
        Cart updatedCart = cartRepository.save(cart);
        log.info("Cart item removed successfully");
        
        return mapToResponse(updatedCart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCartByDistributorId(Long distributorId) {
        log.info("Fetching cart for distributor {}", distributorId);

        List<Cart> placedCarts = cartRepository.findByStatus(Cart.CartStatus.PLACED);
        Cart cart = placedCarts.stream()
                .filter(c -> c.getDistributorId().equals(distributorId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("No placed cart found for distributor " + distributorId));

        return mapToResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getActiveCartByDistributorId(Long distributorId) {
        log.info("Fetching Active cart for distributor {}", distributorId);

        List<Cart> placedCarts = cartRepository.findByStatus(Cart.CartStatus.ACTIVE);
        Cart cart = placedCarts.stream()
                .filter(c -> c.getDistributorId().equals(distributorId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("No Active cart found for distributor " + distributorId));

        return mapToResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getPlacedCartByDistributorId(Long distributorId) {
        log.info("Fetching Active cart for distributor {}", distributorId);

        List<Cart> placedCarts = cartRepository.findByStatus(Cart.CartStatus.PLACED);
        Cart cart = placedCarts.stream()
                .filter(c -> c.getDistributorId().equals(distributorId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("No placed cart found for distributor " + distributorId));

        return mapToResponse(cart);
    }

    @Transactional(readOnly = true)
    public List<CartResponse> getDismissedCarts(Long distributorId) {
        List<Cart> pendingCarts = cartRepository.findByStatus(Cart.CartStatus.DISMISSED);
        return pendingCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CartResponse getCartById(Long cartId) {
        log.info("Fetching cart by ID {}", cartId);
        
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));
        
        return mapToResponse(cart);
    }
    
    private Cart getOrCreateActiveCart(Long distributorId) {
        Optional<Cart> existingCart = cartRepository.findByDistributorIdAndStatus(distributorId, Cart.CartStatus.ACTIVE);
        
        if (existingCart.isPresent()) {
            return existingCart.get();
        }
        
        Cart newCart = new Cart();
        newCart.setDistributorId(distributorId);
        newCart.setStatus(Cart.CartStatus.ACTIVE);
        
        return cartRepository.save(newCart);
    }
    
    private CartResponse mapToResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setStatus(cart.getStatus().name());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        // Fetch distributor and salesperson information
        if (cart.getDistributorId() != null) {
            response.setDistributorId(cart.getDistributorId());
            
            distributorRepository.findById(cart.getDistributorId()).ifPresent(distributor -> {
                response.setDistributorName(distributor.getName());
            });
            
            Optional<SalespersonDistributorMapping> mapping = salesMappingRepository
                .findByDistributorId(cart.getDistributorId())
                .stream()
                .filter(m -> m.getStatus() == MappingStatus.ACTIVE)
                .findFirst();
            
            if (mapping.isPresent()) {
                response.setSalespersonId(mapping.get().getSalespersonId());
                response.setSalespersonName("Salesperson-" + mapping.get().getSalespersonId());
            }
        }
        
        List<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
            .map(this::mapCartItemToResponse)
            .collect(Collectors.toList());
        
        response.setCartItems(cartItemResponses);
        
        // Calculate total cart amount
        BigDecimal totalAmount = cartItemResponses.stream()
            .map(CartItemResponse::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalCartAmount(totalAmount);
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public List<CartResponse> getPendingApprovalCarts() {
        List<Cart> pendingCarts = cartRepository.findByStatus(Cart.CartStatus.ACTIVE);
        return pendingCarts.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CartResponse> getPlacedCarts() {
        List<Cart> pendingCarts = cartRepository.findByStatus(Cart.CartStatus.PLACED);
        return pendingCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CartResponse> getActiveCarts() {
        List<Cart> pendingCarts = cartRepository.findByStatus(Cart.CartStatus.ACTIVE);
        return pendingCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<CartResponse> getDismissedCarts() {
        List<Cart> pendingCarts = cartRepository.findByStatus(Cart.CartStatus.DISMISSED);
        return pendingCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<CartResponse> getApprovedCarts() {
        List<Cart> pendingCarts = cartRepository.findByStatus(Cart.CartStatus.APPROVED);
        return pendingCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Transactional
    public CartResponse approveCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));
        
        // Check if cart is active before approval
        if (cart.getStatus() != Cart.CartStatus.PLACED) {
            throw new com.nector.userservice.exception.InvalidCartStatusException("Cannot approve cart with status: " + cart.getStatus());
        }
        
        cart.setStatus(Cart.CartStatus.APPROVED);
        Cart updatedCart = cartRepository.save(cart);
        
        // Generate Proforma Invoice after approval
        proformaInvoiceService.generateProformaInvoice(cartId);
        
        return mapToResponse(updatedCart);
    }
    
    @Transactional
    public void dismissCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));
        
        cart.setStatus(Cart.CartStatus.DISMISSED);
        cartRepository.save(cart);
        log.info("Cart {} dismissed successfully", cartId);
    }

    @Transactional
    public CartResponse placeOrder(Long distributorId, PlaceOrderRequest request) {
        log.info("Placing order for cart {} for distributor {}", request.getCartId(), distributorId);

        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new CartNotFoundException("Cart with ID " + request.getCartId() + " not found"));

        // Verify cart belongs to the distributor
        if (!cart.getDistributorId().equals(distributorId)) {
            throw new CartNotFoundException("Cart does not belong to distributor " + distributorId);
        }

        // Check if cart is active before placing order
        if (cart.getStatus() != Cart.CartStatus.ACTIVE) {
            throw new InvalidCartStatusException("Cannot place order for cart with status: " + cart.getStatus());
        }

        // Save the address and update status
        cart.setAddress(request.getAddress());
        cart.setStatus(Cart.CartStatus.PLACED);
        Cart updatedCart = cartRepository.save(cart);

        log.info("Order placed successfully for cart {} for distributor {}", request.getCartId(), distributorId);
        return mapToResponse(updatedCart);
    }



    @Transactional
    public CartResponse editCart(Long cartId, List<AddToCartRequest> requests) {
        log.info("Editing cart {} with {} items", cartId, requests.size());
        
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));
        
        for (AddToCartRequest request : requests) {
            FinishedProduct finishedProduct = finishedProductRepository.findBySku(request.getItemId())
                    .filter(FinishedProduct::getActive)
                    .orElseThrow(() -> new ItemNotFoundException("Item with SKU '" + request.getItemId() + "' not found or inactive"));
            
            Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), finishedProduct.getId());
            
            if (existingCartItem.isPresent()) {
                CartItem cartItem = existingCartItem.get();
                cartItem.setQuantity(request.getQuantity());
                cartItemRepository.save(cartItem);
            } else {
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setItem(finishedProduct);
                cartItem.setQuantity(request.getQuantity());
                cartItem.setPriceAtTime(finishedProduct.getPrice());
                cartItemRepository.save(cartItem);
                cart.getCartItems().add(cartItem);
            }
        }
        
        Cart updatedCart = cartRepository.save(cart);
        log.info("Cart edited successfully");
        return mapToResponse(updatedCart);
    }
    
    private CartItemResponse mapCartItemToResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        response.setItemId(cartItem.getItem().getId());
        response.setItemName(cartItem.getItem().getName());
        response.setItemSku(cartItem.getItem().getSku());
        response.setQuantity(cartItem.getQuantity());
        response.setPriceAtTime(cartItem.getPriceAtTime());
        response.setTotalPrice(cartItem.getPriceAtTime().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        return response;
    }



    public byte[] downloadProformaInvoice(Long cartId) {
        log.info("Downloading proforma invoice for cart ID: {}", cartId);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        ProformaInvoice invoice = createInvoiceFromCart(cart);
        String html = generateSimpleHtmlInvoice(invoice);
        byte[] pdfBytes = htmlToPdfService.convertHtmlToPdf(html);

        log.info("Proforma invoice PDF generated successfully for cart: {}", cartId);
        return pdfBytes;
    }

    private ProformaInvoice createInvoiceFromCart(Cart cart) {
        ProformaInvoice invoice = new ProformaInvoice();

        invoice.setPiNumber("PI-" + cart.getId() + "-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));
        invoice.setPiDate(java.time.LocalDate.now());
        invoice.setModeOfPayment("Bank Transfer");
        invoice.setCompanyName("Your Company Name");
        invoice.setCompanyAddress("Your Company Address");
        invoice.setGstin("Your GSTIN");
        invoice.setContactNumber("+91-XXXXXXXXXX");
        invoice.setEmail("sales@yourcompany.com");

        List<com.nector.userservice.dto.invoice.InvoiceItem> items = java.util.stream.IntStream.range(0, cart.getCartItems().size())
                .mapToObj(i -> {
                    CartItem cartItem = cart.getCartItems().get(i);
                    com.nector.userservice.dto.invoice.InvoiceItem item = new com.nector.userservice.dto.invoice.InvoiceItem();
                    item.setSrNo(i + 1);
                    item.setDescription(cartItem.getItem().getName());
                    item.setHsnCode("1234");
                    item.setQuantity(cartItem.getQuantity());
                    item.setRatePerUnit(cartItem.getPriceAtTime().doubleValue());
                    item.setUnit("Pcs");
                    item.setAmount(cartItem.getPriceAtTime().doubleValue() * cartItem.getQuantity());
                    return item;
                })
                .toList();

        invoice.setItems(items);

        double subtotal = items.stream().mapToDouble(com.nector.userservice.dto.invoice.InvoiceItem::getAmount).sum();
        double cgst = subtotal * 0.09;
        double sgst = subtotal * 0.09;
        double grandTotal = subtotal + cgst + sgst;

        invoice.setSubtotal(subtotal);
        invoice.setCgst(cgst);
        invoice.setSgst(sgst);
        invoice.setIgst(0.0);
        invoice.setGrandTotal(grandTotal);
        invoice.setAmountInWords(String.format("%.0f Rupees Only", grandTotal));

        return invoice;
    }

    private String generateSimpleHtmlInvoice(ProformaInvoice invoice) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Proforma Invoice</title>");
        html.append("<style>body{font-family:Arial;margin:20px;}");
        html.append(".header{text-align:center;margin-bottom:30px;}");
        html.append("table{width:100%;border-collapse:collapse;}");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;}");
        html.append("th{background-color:#f2f2f2;}</style></head><body>");

        html.append("<div class='header'><h2>PROFORMA INVOICE</h2>");
        html.append("<p>PI Number: ").append(invoice.getPiNumber()).append("</p>");
        html.append("<p>Date: ").append(invoice.getPiDate()).append("</p></div>");

        html.append("<table><tr><th>Sr No</th><th>Description</th><th>Quantity</th>");
        html.append("<th>Rate</th><th>Amount</th></tr>");

        for (com.nector.userservice.dto.invoice.InvoiceItem item : invoice.getItems()) {
            html.append("<tr><td>").append(item.getSrNo()).append("</td>");
            html.append("<td>").append(item.getDescription()).append("</td>");
            html.append("<td>").append(item.getQuantity()).append("</td>");
            html.append("<td>").append(item.getRatePerUnit()).append("</td>");
            html.append("<td>").append(item.getAmount()).append("</td></tr>");
        }

        html.append("</table>");
        html.append("<p>Subtotal: ").append(invoice.getSubtotal()).append("</p>");
        html.append("<p>CGST (9%): ").append(invoice.getCgst()).append("</p>");
        html.append("<p>SGST (9%): ").append(invoice.getSgst()).append("</p>");
        html.append("<p><strong>Grand Total: ").append(invoice.getGrandTotal()).append("</strong></p>");
        html.append("<p>Amount in words: ").append(invoice.getAmountInWords()).append("</p>");

        html.append("</body></html>");
        return html.toString();
    }

    @Transactional(readOnly = true)
    public List<CartResponse> getApprovedCart() {
        log.info("Fetching all carts with APPROVED status");
        List<Cart> approvedCarts = cartRepository.findByStatus(Cart.CartStatus.APPROVED);
        return approvedCarts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}