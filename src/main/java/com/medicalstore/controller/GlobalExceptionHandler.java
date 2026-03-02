package com.medicalstore.controller;

import com.medicalstore.exception.BusinessException;
import com.medicalstore.exception.ResourceNotFoundException;
import com.medicalstore.exception.StockConflictException;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Centralised exception-to-view mapper for all Thymeleaf (HTML) controllers.
 *
 * <p>API controllers in {@code controller.api} are handled by
 * {@link com.medicalstore.controller.api.ApiExceptionHandler} which returns JSON.</p>
 */
@ControllerAdvice(basePackages = "com.medicalstore.controller")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 400 Bad Request ─────────────────────────────────────────────────────

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusiness(BusinessException ex, HttpServletRequest request, Model model) {
        log.warn("Business rule violation at {}: [{}] {}", request.getRequestURI(), ex.getCode(), ex.getMessage());
        model.addAttribute("errorStatus", 400);
        model.addAttribute("errorTitle", "Invalid Request");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request, Model model) {
        log.warn("Illegal argument at {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorStatus", 400);
        model.addAttribute("errorTitle", "Invalid Input");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    // ── 403 Forbidden ───────────────────────────────────────────────────────

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

    // ── 404 Not Found ───────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request, Model model) {
        log.debug("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorStatus", 404);
        model.addAttribute("errorTitle", "Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoResourceFoundException ex, HttpServletRequest request, Model model) {
        log.debug("Static resource not found: {}", request.getRequestURI());
        model.addAttribute("errorStatus", 404);
        model.addAttribute("errorTitle", "Page Not Found");
        model.addAttribute("errorMessage",
                "The page you are looking for does not exist or has been moved.");
        return "error";
    }

    // ── 409 Conflict ────────────────────────────────────────────────────────

    /**
     * Handles concurrent stock modification races detected via @Version.
     */
    @ExceptionHandler({StockConflictException.class, OptimisticLockException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleStockConflict(RuntimeException ex, HttpServletRequest request, Model model) {
        log.warn("Optimistic lock conflict at {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorStatus", 409);
        model.addAttribute("errorTitle", "Concurrent Modification");
        model.addAttribute("errorMessage",
                "Another user modified the same record simultaneously. Please refresh and try again.");
        return "error";
    }

    /** Duplicate key / FK constraint violations from the database. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDataIntegrity(DataIntegrityViolationException ex,
            HttpServletRequest request, Model model) {
        log.warn("Data integrity violation at {}: {}",
                request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        model.addAttribute("errorStatus", 409);
        model.addAttribute("errorTitle", "Data Conflict");
        model.addAttribute("errorMessage",
                "This operation conflicts with existing data (e.g. duplicate entry or a linked record). "
                + "Please check your input and try again.");
        return "error";
    }

    // ── 500 Internal Server Error ───────────────────────────────────────────

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
