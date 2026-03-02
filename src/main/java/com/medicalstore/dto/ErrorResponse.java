package com.medicalstore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standardised JSON error body returned for all API error responses.
 * Used by GlobalExceptionHandler for application/json requests.
 *
 * <pre>
 * {
 *   "error":   "INSUFFICIENT_STOCK",
 *   "message": "Insufficient stock for medicine: Paracetamol",
 *   "timestamp": "2026-03-02T10:15:30Z",
 *   "fieldErrors": [ { "field": "quantity", "message": "must be > 0" } ]
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String error;
    private final String message;
    private final Instant timestamp;
    private List<FieldError> fieldErrors;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public ErrorResponse(String error, String message, List<FieldError> fieldErrors) {
        this(error, message);
        this.fieldErrors = fieldErrors;
    }

    public String getError() { return error; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public List<FieldError> getFieldErrors() { return fieldErrors; }

    // ── Nested ──────────────────────────────────────────────────────────────
    public static class FieldError {
        private final String field;
        private final String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public String getMessage() { return message; }
    }

    // ── Factory helpers ─────────────────────────────────────────────────────
    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message);
    }

    public static ErrorResponse badRequest(String message) {
        return new ErrorResponse("BAD_REQUEST", message);
    }

    public static ErrorResponse notFound(String message) {
        return new ErrorResponse("NOT_FOUND", message);
    }

    public static ErrorResponse conflict(String message) {
        return new ErrorResponse("CONFLICT", message);
    }

    public static ErrorResponse forbidden(String message) {
        return new ErrorResponse("FORBIDDEN", message);
    }

    public static ErrorResponse internalError(String message) {
        return new ErrorResponse("INTERNAL_ERROR", message);
    }
}
