package com.pantrypal;

public class CartItem {
    private int cartId;
    private int productId;
    private int quantity;
    private String productName;
    private double productPrice;
    private String imageUrl;
    private String userEmail;

    // Getters and Setters for all fields
    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getUserEmail() { return userEmail; } // ✅ add getter
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
