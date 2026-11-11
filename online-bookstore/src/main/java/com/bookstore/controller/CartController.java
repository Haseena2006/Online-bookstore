package com.bookstore.controller;

import com.bookstore.dto.CartSummary;
import com.bookstore.dto.Requests.AddToCartRequest;
import com.bookstore.dto.Requests.UpdateCartRequest;
import com.bookstore.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public CartSummary get(HttpSession session) {
        return service.get(AuthSupport.requireUserId(session));
    }

    @PostMapping
    public ResponseEntity<CartSummary> add(@RequestBody AddToCartRequest req, HttpSession session) {
        long userId = AuthSupport.requireUserId(session);
        if (req == null) throw new com.bookstore.exception.ValidationException("Request body is required.");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(userId, req.bookId(), req.quantity()));
    }

    @PutMapping("/{id}")
    public CartSummary update(@PathVariable("id") long id, @RequestBody UpdateCartRequest req, HttpSession session) {
        return service.updateQuantity(AuthSupport.requireUserId(session), id, req == null ? null : req.quantity());
    }

    @DeleteMapping("/{id}")
    public CartSummary remove(@PathVariable("id") long id, HttpSession session) {
        return service.remove(AuthSupport.requireUserId(session), id);
    }
}
