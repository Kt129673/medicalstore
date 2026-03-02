package com.medicalstore.exception;

/**
 * Thrown when a requested entity cannot be found.
 * Maps to HTTP 404 Not Found via GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String entityName, Long id) {
        super("RESOURCE_NOT_FOUND", entityName + " not found with id: " + id);
    }

    public ResourceNotFoundException(String entityName, String field, Object value) {
        super("RESOURCE_NOT_FOUND", entityName + " not found with " + field + ": " + value);
    }
}
