package com.bookstore.dto;

import com.bookstore.model.Cart;

import java.math.BigDecimal;
import java.util.List;

public record CartSummary(List<Cart> items, int totalItems, BigDecimal totalAmount) {
    public static CartSummary of(List<Cart> items) {
        int count = items.stream().mapToInt(Cart::getQuantity).sum();
        BigDecimal total = items.stream().map(Cart::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartSummary(items, count, total);
    }
}
