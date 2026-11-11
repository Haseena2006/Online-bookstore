package com.bookstore.service;

import com.bookstore.dto.Requests.CheckoutRequest;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ForbiddenException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.exception.ValidationException;
import com.bookstore.model.Book;
import com.bookstore.model.Cart;
import com.bookstore.model.Order;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static com.bookstore.service.UserService.*;

@Service
public class OrderService {

    private static final Pattern PINCODE = Pattern.compile("^[0-9]{6}$");

    private final OrderRepository orders;
    private final CartRepository carts;

    public OrderService(OrderRepository orders, CartRepository carts) {
        this.orders = orders;
        this.carts = carts;
    }

    /**
     * One database transaction: lock books, verify stock, create the order and its items,
     * reduce stock and empty the cart. Any failure rolls everything back.
     */
    @Transactional
    public Order checkout(long userId, CheckoutRequest req) {
        if (req == null) throw new ValidationException("Request body is required.");
        String name = trim(req.fullName());
        String email = trim(req.email());
        String phone = normalisePhone(req.phone());
        String address = trim(req.address());
        String city = trim(req.city());
        String pincode = trim(req.pincode());

        if (name.length() < 2) throw new ValidationException("Full name is required.");
        if (!EMAIL.matcher(email).matches()) throw new ValidationException("Enter a valid email address.");
        if (!PHONE.matcher(phone).matches()) throw new ValidationException("Phone number must have 10 to 13 digits.");
        if (address.length() < 5) throw new ValidationException("Delivery address is required.");
        if (city.isEmpty()) throw new ValidationException("City is required.");
        if (!PINCODE.matcher(pincode).matches()) throw new ValidationException("Pincode must be 6 digits.");

        List<Cart> cart = carts.findByUser(userId).stream()
                .sorted(Comparator.comparing(Cart::getBookId))   // consistent lock order avoids deadlocks
                .toList();
        if (cart.isEmpty()) throw new ValidationException("Your cart is empty.");

        BigDecimal total = BigDecimal.ZERO;
        long orderId = -1;
        String delivery = name + "\n" + address + ", " + city + " - " + pincode
                + "\nPhone: " + phone + "\nEmail: " + email;

        // First pass: lock rows and validate everything before writing anything.
        List<Book> locked = new java.util.ArrayList<>();
        for (Cart item : cart) {
            Book book = orders.lockBook(item.getBookId())
                    .orElseThrow(() -> new ConflictException("\"" + item.getTitle() + "\" is no longer available."));
            if (book.getStock() <= 0)
                throw new ConflictException("\"" + book.getTitle() + "\" is out of stock. Remove it from your cart to continue.");
            if (item.getQuantity() > book.getStock())
                throw new ConflictException("Only " + book.getStock() + " cop" + (book.getStock() == 1 ? "y" : "ies")
                        + " of \"" + book.getTitle() + "\" left. Reduce the quantity in your cart.");
            locked.add(book);
            total = total.add(book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        orderId = orders.insertOrder(userId, total, delivery);
        for (int i = 0; i < cart.size(); i++) {
            Cart item = cart.get(i);
            Book book = locked.get(i);
            orders.insertItem(orderId, book.getId(), item.getQuantity(), book.getPrice());
            if (!orders.reduceStock(book.getId(), item.getQuantity()))
                throw new ConflictException("\"" + book.getTitle() + "\" just sold out. Please review your cart.");
        }
        carts.clear(userId);
        return orders.findById(orderId).orElseThrow();
    }

    public Order getForUser(long userId, long orderId) {
        Order o = orders.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " was not found."));
        if (o.getUserId() != userId) throw new ForbiddenException("This order belongs to another user.");
        return o;
    }

    public List<Order> history(long userId) {
        return orders.findByUser(userId);
    }
}
