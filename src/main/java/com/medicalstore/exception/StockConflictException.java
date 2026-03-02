package com.medicalstore.exception;

/**
 * Thrown when concurrent sale requests race for the same medicine stock,
 * resulting in an optimistic locking conflict or insufficient stock condition.
 * Maps to HTTP 409 Conflict via GlobalExceptionHandler.
 *
 * <p>Callers should present a user-friendly "please try again" message.</p>
 */
public class StockConflictException extends RuntimeException {

    private final String medicineName;

    public StockConflictException(String medicineName) {
        super("Stock conflict for medicine: " + medicineName
                + ". Another transaction modified the stock simultaneously. Please retry.");
        this.medicineName = medicineName;
    }

    public StockConflictException(String medicineName, Throwable cause) {
        super("Stock conflict for medicine: " + medicineName
                + ". Another transaction modified the stock simultaneously. Please retry.", cause);
        this.medicineName = medicineName;
    }

    public String getMedicineName() {
        return medicineName;
    }
}
