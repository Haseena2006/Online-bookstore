package com.bookstore.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Turns every exception into a consistent JSON body: {status, error, message, timestamp}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> validation(ValidationException e) {
        return body(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, Object>> badRequest(Exception e) {
        return body(HttpStatus.BAD_REQUEST, "Invalid request. Check the request body, parameters and IDs.");
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> unauthorized(UnauthorizedException e) {
        return body(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> forbidden(ForbiddenException e) {
        return body(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler({ResourceNotFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<Map<String, Object>> notFound(Exception e) {
        String msg = e instanceof ResourceNotFoundException ? e.getMessage() : "Endpoint not found.";
        return body(HttpStatus.NOT_FOUND, msg);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> conflict(ConflictException e) {
        return body(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> method(HttpRequestMethodNotSupportedException e) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method " + e.getMethod() + " is not supported here.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> media(HttpMediaTypeNotSupportedException e) {
        return body(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Send the request body as application/json.");
    }

    @ExceptionHandler({CannotGetJdbcConnectionException.class, CannotCreateTransactionException.class})
    public ResponseEntity<Map<String, Object>> noDb(Exception e) {
        log.error("Database connection failed", e);
        return body(HttpStatus.SERVICE_UNAVAILABLE, "The database is unavailable. Please try again shortly.");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> db(DataAccessException e) {
        log.error("Database error", e);
        // Connection problems surface as different subclasses depending on the driver state.
        Throwable root = e.getMostSpecificCause();
        if (root != null && root.getClass().getName().startsWith("java.net")) {
            return body(HttpStatus.SERVICE_UNAVAILABLE, "The database is unavailable. Please try again shortly.");
        }
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> other(Exception e) {
        log.error("Unexpected error", e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on the server.");
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", status.value());
        m.put("error", status.getReasonPhrase());
        m.put("message", message);
        m.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(status).body(m);
    }
}
