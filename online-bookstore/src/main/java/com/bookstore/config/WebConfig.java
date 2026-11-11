package com.bookstore.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web context: REST controllers + JSON conversion (Jackson is picked up automatically).
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.bookstore.controller", "com.bookstore.exception"})
public class WebConfig implements WebMvcConfigurer {

    /** The DispatcherServlet owns "/", so hand HTML/CSS/JS/image requests back to Tomcat's default servlet. */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /** Open http://localhost:8080/online-bookstore/ and land on the home page. */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

}
