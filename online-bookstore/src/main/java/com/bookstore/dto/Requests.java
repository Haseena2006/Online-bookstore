package com.bookstore.dto;

/** Request bodies for the REST API. */
public final class Requests {
    private Requests() {}

    public record RegisterRequest(String fullName, String email, String password, String phone) {}
    public record LoginRequest(String email, String password) {}
    public record AddToCartRequest(Long bookId, Integer quantity) {}
    public record UpdateCartRequest(Integer quantity) {}
    public record CheckoutRequest(String fullName, String email, String phone,
                                  String address, String city, String pincode) {}
}
