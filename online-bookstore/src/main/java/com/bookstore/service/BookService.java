package com.bookstore.service;

import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BookService {

    private final BookRepository books;

    public BookService(BookRepository books) {
        this.books = books;
    }

    public List<Book> find(String keyword, String category, String sort) {
        return books.find(keyword, category, sort);
    }

    public Book getById(long id) {
        return books.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book " + id + " was not found."));
    }

    public List<Book> search(String keyword) {
        return books.find(keyword, null, null);
    }

    public List<Book> byCategory(String category) {
        return books.find(null, category, null);
    }

    public List<Book> featured() { return books.findFeatured(8); }
    public List<Book> popular() { return books.findPopular(8); }
    public List<Book> newArrivals() { return books.findNewArrivals(8); }
    public List<Map<String, Object>> categories() { return books.categoryCounts(); }
}
