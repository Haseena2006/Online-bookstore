package com.bookstore.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * JDBC wiring: connection pool, JdbcTemplate (all queries run as PreparedStatements)
 * and the transaction manager used by {@code @Transactional} service methods.
 */
@Configuration
public class DatabaseConfig {

    @Bean(destroyMethod = "close")
    public DataSource dataSource(Environment env) {
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("org.postgresql.Driver");
        cfg.setJdbcUrl(env.getRequiredProperty("db.url"));
        cfg.setUsername(env.getRequiredProperty("db.username"));
        cfg.setPassword(env.getRequiredProperty("db.password"));
        cfg.setMaximumPoolSize(env.getProperty("db.pool.size", Integer.class, 10));
        cfg.setConnectionTimeout(5000);
        // Do not stop the whole application from starting when PostgreSQL is down:
        // requests then get a clean 503 "database unavailable" response instead.
        cfg.setInitializationFailTimeout(-1);
        cfg.setPoolName("bookstore-pool");
        return new HikariDataSource(cfg);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
