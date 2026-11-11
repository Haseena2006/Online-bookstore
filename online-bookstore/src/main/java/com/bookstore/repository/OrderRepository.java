package com.bookstore.repository;

import com.bookstore.model.Book;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private static final RowMapper<Order> ORDER_MAPPER = (rs, i) -> {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setUserId(rs.getLong("user_id"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setDeliveryAddress(rs.getString("delivery_address"));
        o.setOrderDate(rs.getObject("order_date", LocalDateTime.class));
        o.setOrderStatus(rs.getString("order_status"));
        return o;
    };

    private static final RowMapper<OrderItem> ITEM_MAPPER = (rs, i) -> {
        OrderItem it = new OrderItem();
        it.setId(rs.getLong("id"));
        it.setOrderId(rs.getLong("order_id"));
        it.setBookId(rs.getLong("book_id"));
        it.setQuantity(rs.getInt("quantity"));
        it.setPrice(rs.getBigDecimal("price"));
        it.setTitle(rs.getString("title"));
        it.setImageUrl(rs.getString("image_url"));
        return it;
    };

    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Row-locks a book for the rest of the transaction so concurrent checkouts cannot oversell. */
    public Optional<Book> lockBook(long bookId) {
        return jdbc.query("SELECT * FROM books WHERE id = ? FOR UPDATE", BookRepository.MAPPER, bookId)
                .stream().findFirst();
    }

    public long insertOrder(long userId, BigDecimal total, String address) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO orders (user_id, total_amount, delivery_address, order_status) VALUES (?, ?, ?, 'PLACED')",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setBigDecimal(2, total);
            ps.setString(3, address);
            return ps;
        }, keys);
        return ((Number) keys.getKeys().get("id")).longValue();
    }

    public void insertItem(long orderId, long bookId, int quantity, BigDecimal price) {
        jdbc.update("INSERT INTO order_items (order_id, book_id, quantity, price) VALUES (?, ?, ?, ?)",
                orderId, bookId, quantity, price);
    }

    /** Returns false when stock was insufficient (guard against races, in addition to the row lock). */
    public boolean reduceStock(long bookId, int quantity) {
        return jdbc.update("UPDATE books SET stock = stock - ?, sold_count = sold_count + ? WHERE id = ? AND stock >= ?",
                quantity, quantity, bookId, quantity) == 1;
    }

    public Optional<Order> findById(long id) {
        return jdbc.query("SELECT * FROM orders WHERE id = ?", ORDER_MAPPER, id).stream().findFirst()
                .map(this::withItems);
    }

    public List<Order> findByUser(long userId) {
        List<Order> orders = jdbc.query("SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC, id DESC",
                ORDER_MAPPER, userId);
        orders.forEach(this::withItems);
        return orders;
    }

    private Order withItems(Order o) {
        o.setItems(jdbc.query("""
                SELECT oi.*, b.title, b.image_url
                FROM order_items oi JOIN books b ON b.id = oi.book_id
                WHERE oi.order_id = ? ORDER BY oi.id
                """, ITEM_MAPPER, o.getId()));
        return o;
    }
}
