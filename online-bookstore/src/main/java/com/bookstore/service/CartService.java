package com.bookstore.service;

import com.bookstore.dto.CartSummary;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ForbiddenException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.exception.ValidationException;
import com.bookstore.model.Book;
import com.bookstore.model.Cart;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository carts;
    private final BookRepository books;

    public CartService(CartRepository carts, BookRepository books) {
        this.carts = carts;
        this.books = books;
    }

    public CartSummary get(long userId) {
        return CartSummary.of(carts.findByUser(userId));
    }

    /** Adding a book already in the cart increases its quantity instead of creating a second row. */
    @Transactional
    public CartSummary add(long userId, Long bookId, Integer quantity) {
        if (bookId == null) throw new ValidationException("bookId is required.");
        int qty = quantity == null ? 1 : quantity;
        if (qty <= 0) throw new ValidationException("Quantity must be greater than zero.");

        Book book = books.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book " + bookId + " was not found."));
        if (book.getStock() <= 0) throw new ConflictException("\"" + book.getTitle() + "\" is out of stock.");

        int already = carts.findByUserAndBook(userId, bookId).map(Cart::getQuantity).orElse(0);
        int wanted = already + qty;
        if (wanted > book.getStock()) {
            throw new ConflictException("Only " + book.getStock() + " cop" + (book.getStock() == 1 ? "y" : "ies")
                    + " of \"" + book.getTitle() + "\" available"
                    + (already > 0 ? " (you already have " + already + " in your cart)." : "."));
        }
        carts.upsert(userId, bookId, wanted);
        return get(userId);
    }

    @Transactional
    public CartSummary updateQuantity(long userId, long cartId, Integer quantity) {
        if (quantity == null || quantity <= 0)
            throw new ValidationException("Quantity must be greater than zero. Use DELETE to remove an item.");
        Cart item = owned(userId, cartId);
        if (quantity > item.getStock()) {
            throw new ConflictException("Only " + item.getStock() + " cop" + (item.getStock() == 1 ? "y" : "ies")
                    + " of \"" + item.getTitle() + "\" available.");
        }
        carts.updateQuantity(cartId, quantity);
        return get(userId);
    }

    @Transactional
    public CartSummary remove(long userId, long cartId) {
        owned(userId, cartId);
        carts.delete(cartId);
        return get(userId);
    }

    private Cart owned(long userId, long cartId) {
        Cart item = carts.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item " + cartId + " was not found."));
        if (item.getUserId() != userId) throw new ForbiddenException("This cart item belongs to another user.");
        return item;
    }
}
