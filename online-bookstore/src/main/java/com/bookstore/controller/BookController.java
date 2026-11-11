package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    /** GET /books, optionally filtered: ?keyword=&category=&sort=price_asc|price_desc */
    @GetMapping
    public List<Book> all(@RequestParam(name = "keyword", required = false) String keyword,
                          @RequestParam(name = "category", required = false) String category,
                          @RequestParam(name = "sort", required = false) String sort) {
        return service.find(keyword, category, sort);
    }

    @GetMapping("/{id}")
    public Book one(@PathVariable("id") long id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    public List<Book> search(@RequestParam("keyword") String keyword) {
        return service.search(keyword);
    }

    @GetMapping("/category/{category}")
    public List<Book> byCategory(@PathVariable("category") String category) {
        return service.byCategory(category);
    }

    @GetMapping("/featured")
    public List<Book> featured() { return service.featured(); }

    @GetMapping("/popular")
    public List<Book> popular() { return service.popular(); }

    @GetMapping("/new")
    public List<Book> newArrivals() { return service.newArrivals(); }

    @GetMapping("/categories")
    public List<Map<String, Object>> categories() { return service.categories(); }
}
