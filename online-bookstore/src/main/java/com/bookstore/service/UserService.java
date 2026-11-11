package com.bookstore.service;

import com.bookstore.dto.Requests.LoginRequest;
import com.bookstore.dto.Requests.RegisterRequest;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.exception.UnauthorizedException;
import com.bookstore.exception.ValidationException;
import com.bookstore.model.User;
import com.bookstore.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserService {

    static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{10,13}$");

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public User register(RegisterRequest req) {
        if (req == null) throw new ValidationException("Request body is required.");
        String name = trim(req.fullName());
        String email = trim(req.email()).toLowerCase();
        String phone = normalisePhone(req.phone());
        String password = req.password() == null ? "" : req.password();

        if (name.length() < 2) throw new ValidationException("Full name must be at least 2 characters.");
        if (!EMAIL.matcher(email).matches()) throw new ValidationException("Enter a valid email address.");
        if (!phone.isEmpty() && !PHONE.matcher(phone).matches())
            throw new ValidationException("Phone number must have 10 to 13 digits.");
        if (password.length() < 8 || !password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*"))
            throw new ValidationException("Password must be at least 8 characters and include a letter and a number.");
        if (users.findByEmail(email).isPresent())
            throw new ConflictException("An account with this email already exists.");

        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPhone(phone.isEmpty() ? null : phone);
        u.setPassword(encoder.encode(password));
        try {
            return getById(users.insert(u));
        } catch (DuplicateKeyException e) {   // two registrations racing on the same email
            throw new ConflictException("An account with this email already exists.");
        }
    }

    public User login(LoginRequest req) {
        if (req == null || trim(req.email()).isEmpty() || req.password() == null || req.password().isEmpty())
            throw new ValidationException("Email and password are required.");
        User u = users.findByEmail(trim(req.email()).toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));
        if (!encoder.matches(req.password(), u.getPassword()))
            throw new UnauthorizedException("Invalid email or password.");
        return u;
    }

    public User getById(long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User " + id + " was not found."));
    }

    static String trim(String s) { return s == null ? "" : s.trim(); }

    static String normalisePhone(String s) { return trim(s).replaceAll("[\\s-]", ""); }
}
