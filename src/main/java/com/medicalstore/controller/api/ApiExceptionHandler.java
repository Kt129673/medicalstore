package com.medicalstore.controller.api;

import com.medicalstore.dto.ErrorResponse;
import com.medicalstore.exception.BusinessException;
import com.medicalstore.exception.ResourceNotFoundException;
import com.medicalstore.exception.StockConflictException;

import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * JSON exception handler for all REST API controllers in the
 * {@code controller.api} package.
 * All responses follow the {@link ErrorResponse} schema.
 *
 * <p>
 * This handler is deliberately scoped to the {@code api} sub-package so that
 * Thymeleaf controllers handled by {@code GlobalExceptionHandler} are
 * unaffected.
 * </p>
 */
@RestControllerAdvice(basePackages = "com.medicalstore.controller.api")
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // ── 400 Bad Request ─────────────────────────────────────────────────────

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        log.warn("API business exception: [{}] {}", ex.getCode(), ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("API illegal argument: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.badRequest(ex.getMessage()));
    }

    // ── 403 Forbidden ───────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("API access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.forbidden("You do not have permission to perform this action."));
    }

    // ── 404 Not Found ───────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.notFound(ex.getMessage()));
    }

    // ── 409 Conflict ────────────────────────────────────────────────────────

    /**
     * Handles concurrent stock conflicts — {@code @Version} optimistic lock or
     * explicit
     * {@link StockConflictException}. Returns 409 so clients can retry.
     */
    @ExceptionHandler({ StockConflictException.class, OptimisticLockException.class })
    public ResponseEntity<ErrorResponse> handleStockConflict(RuntimeException ex) {
        log.warn("API optimistic lock conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.conflict(
                        "Stock was modified by another transaction. Please retry your request."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("API data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.conflict("Data conflict: duplicate entry or constraint violation."));
    }

    // ── 500 Internal Server Error ───────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled API exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.internalError("An unexpected error occurred. Please try again."));
    }
}
