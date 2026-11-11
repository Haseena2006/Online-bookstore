package com.bookstore.repository;

import com.bookstore.model.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** All SQL uses bind parameters (JdbcTemplate prepares a PreparedStatement), never string concatenation of input. */
@Repository
public class BookRepository {

    static final RowMapper<Book> MAPPER = (rs, i) -> {
        Book b = new Book();
        b.setId(rs.getLong("id"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setCategory(rs.getString("category"));
        b.setDescription(rs.getString("description"));
        b.setPrice(rs.getBigDecimal("price"));
        b.setStock(rs.getInt("stock"));
        b.setImageUrl(rs.getString("image_url"));
        b.setFeatured(rs.getBoolean("featured"));
        b.setSoldCount(rs.getInt("sold_count"));
        b.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return b;
    };

    private static final String SELECT = "SELECT * FROM books";

    private final JdbcTemplate jdbc;

    public BookRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Optional keyword (title or author) and category filters, plus a whitelisted sort. */
    public List<Book> find(String keyword, String category, String sort) {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(author) LIKE ?)");
            String like = "%" + keyword.trim().toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            args.add(like);
            args.add(like);
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND LOWER(category) = ?");
            args.add(category.trim().toLowerCase());
        }
        sql.append(switch (sort == null ? "" : sort) {
            case "price_asc" -> " ORDER BY price ASC, id";
            case "price_desc" -> " ORDER BY price DESC, id";
            default -> " ORDER BY id";
        });
        return jdbc.query(sql.toString(), MAPPER, args.toArray());
    }

    public Optional<Book> findById(long id) {
        return jdbc.query(SELECT + " WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    public List<Book> findFeatured(int limit) {
        return jdbc.query(SELECT + " WHERE featured = TRUE ORDER BY id LIMIT ?", MAPPER, limit);
    }

    public List<Book> findPopular(int limit) {
        return jdbc.query(SELECT + " ORDER BY sold_count DESC, id LIMIT ?", MAPPER, limit);
    }

    public List<Book> findNewArrivals(int limit) {
        return jdbc.query(SELECT + " ORDER BY created_at DESC, id DESC LIMIT ?", MAPPER, limit);
    }

    public List<Map<String, Object>> categoryCounts() {
        return jdbc.queryForList("SELECT category AS name, COUNT(*) AS count FROM books GROUP BY category ORDER BY category");
    }
}
