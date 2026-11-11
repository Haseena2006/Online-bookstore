package com.bookstore.repository;

import com.bookstore.model.Cart;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CartRepository {

    private static final String SELECT = """
            SELECT c.id, c.user_id, c.book_id, c.quantity,
                   b.title, b.author, b.image_url, b.price, b.stock
            FROM cart c JOIN books b ON b.id = c.book_id
            """;

    private static final RowMapper<Cart> MAPPER = (rs, i) -> {
        Cart c = new Cart();
        c.setId(rs.getLong("id"));
        c.setUserId(rs.getLong("user_id"));
        c.setBookId(rs.getLong("book_id"));
        c.setQuantity(rs.getInt("quantity"));
        c.setTitle(rs.getString("title"));
        c.setAuthor(rs.getString("author"));
        c.setImageUrl(rs.getString("image_url"));
        c.setPrice(rs.getBigDecimal("price"));
        c.setStock(rs.getInt("stock"));
        return c;
    };

    private final JdbcTemplate jdbc;

    public CartRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Cart> findByUser(long userId) {
        return jdbc.query(SELECT + " WHERE c.user_id = ? ORDER BY c.id", MAPPER, userId);
    }

    public Optional<Cart> findById(long id) {
        return jdbc.query(SELECT + " WHERE c.id = ?", MAPPER, id).stream().findFirst();
    }

    public Optional<Cart> findByUserAndBook(long userId, long bookId) {
        return jdbc.query(SELECT + " WHERE c.user_id = ? AND c.book_id = ?", MAPPER, userId, bookId).stream().findFirst();
    }

    /** Insert, or set the quantity if the book is already in the cart (unique user_id + book_id). */
    public void upsert(long userId, long bookId, int quantity) {
        jdbc.update("""
                INSERT INTO cart (user_id, book_id, quantity) VALUES (?, ?, ?)
                ON CONFLICT (user_id, book_id) DO UPDATE SET quantity = EXCLUDED.quantity
                """, userId, bookId, quantity);
    }

    public void updateQuantity(long id, int quantity) {
        jdbc.update("UPDATE cart SET quantity = ? WHERE id = ?", quantity, id);
    }

    public void delete(long id) {
        jdbc.update("DELETE FROM cart WHERE id = ?", id);
    }

    public void clear(long userId) {
        jdbc.update("DELETE FROM cart WHERE user_id = ?", userId);
    }
}
