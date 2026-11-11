package com.bookstore.controller;

import com.bookstore.dto.Requests.CheckoutRequest;
import com.bookstore.exception.ForbiddenException;
import com.bookstore.model.Order;
import com.bookstore.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestBody CheckoutRequest req, HttpSession session) {
        long userId = AuthSupport.requireUserId(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.checkout(userId, req));
    }

    @GetMapping("/orders/{id}")
    public Order get(@PathVariable("id") long id, HttpSession session) {
        return service.getForUser(AuthSupport.requireUserId(session), id);
    }

    @GetMapping("/users/{userId}/orders")
    public List<Order> history(@PathVariable("userId") long userId, HttpSession session) {
        long me = AuthSupport.requireUserId(session);
        if (me != userId) throw new ForbiddenException("You can only view your own orders.");
        return service.history(userId);
    }
}
