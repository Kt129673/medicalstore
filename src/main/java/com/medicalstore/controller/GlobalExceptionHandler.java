package com.medicalstore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, HttpServletRequest request, Model model) {
        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorStatus", 403);
        model.addAttribute("errorTitle", "Access Denied");
        model.addAttribute("errorMessage",
                "You do not have permission to access this resource.");
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoResourceFoundException ex, HttpServletRequest request, Model model) {
        log.debug("Resource not found: {}", request.getRequestURI());
        model.addAttribute("errorStatus", 404);
        model.addAttribute("errorTitle", "Page Not Found");
        model.addAttribute("errorMessage",
                "The page you are looking for does not exist or has been moved.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, HttpServletRequest request, Model model) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        model.addAttribute("errorStatus", 500);
        model.addAttribute("errorTitle", "Something Went Wrong");
        model.addAttribute("errorMessage",
                "An unexpected error occurred. Please try again or contact support.");
        return "error";
    }
}
