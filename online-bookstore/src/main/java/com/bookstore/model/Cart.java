package com.bookstore.model;

import java.math.BigDecimal;

/** One cart row joined with its book, so the UI needs a single request. */
public class Cart {
    private Long id;
    private Long userId;
    private Long bookId;
    private int quantity;
    private String title;
    private String author;
    private String imageUrl;
    private BigDecimal price;
    private int stock;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public BigDecimal getLineTotal() { return price.multiply(BigDecimal.valueOf(quantity)); }
}
