package com.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Root application context: services, repositories, transactions and shared beans.
 * Controllers live in the web context (see {@link WebConfig}).
 */
@Configuration
@ComponentScan(basePackages = {"com.bookstore.service", "com.bookstore.repository"})
@PropertySource("classpath:application.properties")
@EnableTransactionManagement
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
