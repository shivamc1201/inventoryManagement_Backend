package com.nector.userservice.test;

import com.nector.userservice.dto.inventory.ItemRequest;
import com.nector.userservice.dto.cart.AddToCartRequest;
import com.nector.userservice.model.Item;
import com.nector.userservice.model.Cart;

import java.math.BigDecimal;

/**
 * Simple compilation test for inventory and cart management
 */
public class CompilationTest {
    
    public void testInventoryCreation() {
        // Test DTO creation
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setName("Test Item");
        itemRequest.setSku("TEST-001");
        itemRequest.setPrice(new BigDecimal("99.99"));
        itemRequest.setQuantity(10);
        
        // Test Entity creation
        Item item = new Item();
        item.setName("Test Item");
        item.setSku("TEST-001");
        item.setPrice(new BigDecimal("99.99"));
        item.setQuantity(10);
    }
    
    public void testCartCreation() {
        // Test DTO creation
        AddToCartRequest cartRequest = new AddToCartRequest();
        cartRequest.setItemId(String.valueOf(1L));
        cartRequest.setQuantity(2);
        
        // Test Entity creation
        Cart cart = new Cart();
        cart.setUserId(123L);
        cart.setStatus(Cart.CartStatus.ACTIVE);
    }
}