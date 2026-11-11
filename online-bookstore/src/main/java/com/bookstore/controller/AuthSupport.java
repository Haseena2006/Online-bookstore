package com.bookstore.controller;

import com.bookstore.exception.UnauthorizedException;
import jakarta.servlet.http.HttpSession;

/** Reads the logged-in user id stored in the HTTP session by UserController.login. */
final class AuthSupport {
    static final String USER_ID = "userId";

    private AuthSupport() {}

    static long requireUserId(HttpSession session) {
        Object id = session == null ? null : session.getAttribute(USER_ID);
        if (id == null) throw new UnauthorizedException("Please log in to continue.");
        return (Long) id;
    }
}
