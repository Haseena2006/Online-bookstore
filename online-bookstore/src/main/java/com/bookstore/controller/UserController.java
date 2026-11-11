package com.bookstore.controller;

import com.bookstore.dto.Requests.LoginRequest;
import com.bookstore.dto.Requests.RegisterRequest;
import com.bookstore.model.User;
import com.bookstore.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(req));
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest req, HttpServletRequest http) {
        User user = service.login(req);
        HttpSession old = http.getSession(false);
        if (old != null) http.changeSessionId();          // prevent session fixation
        http.getSession(true).setAttribute(AuthSupport.USER_ID, user.getId());
        return user;
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Logged out.");
    }

    /** Who is logged in? 401 when nobody is. */
    @GetMapping("/me")
    public User me(HttpSession session) {
        return service.getById(AuthSupport.requireUserId(session));
    }
}
