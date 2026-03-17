# PowerShell script to fix the CartService.java file
$content = Get-Content "src\main\java\com\nector\userservice\service\CartService.java" -Raw

# Replace the problematic method section
$oldMethod = @'
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
'@

$newMethod = @'
    private CartItemResponse mapCartItemToResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        
        // Handle missing products gracefully
        try {
            if (cartItem.getItem() != null) {
                response.setItemId(cartItem.getItem().getId());
                response.setItemName(cartItem.getItem().getName());
                response.setItemSku(cartItem.getItem().getSku());
            } else {
                response.setItemId(null);
                response.setItemName(null);
                response.setItemSku(null);
            }
        } catch (Exception e) {
            // Product deleted or inaccessible
            response.setItemId(null);
            response.setItemName(null);
            response.setItemSku(null);
        }
        
        response.setQuantity(cartItem.getQuantity());
        response.setPriceAtTime(cartItem.getPriceAtTime());
        response.setTotalPrice(cartItem.getPriceAtTime().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        return response;
    }
'@

$content = $content.Replace($oldMethod, $newMethod)
Set-Content "src\main\java\com\nector\userservice\service\CartService.java" $content

Write-Host "File updated successfully"
