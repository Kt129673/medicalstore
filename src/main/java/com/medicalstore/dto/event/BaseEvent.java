package com.medicalstore.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract base class for all Kafka event payloads.
 *
 * <p>
 * Every event carries a unique {@code eventId}, a {@code timestamp},
 * the {@code branchId} it belongs to, and the {@code performedBy} username.
 * Subclasses add domain-specific fields.
 * </p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for this event (UUID). */
    private String eventId;

    /** Discriminator — e.g. {@code SALE_CREATED}, {@code MEDICINE_UPDATED}. */
    private String eventType;

    /** When the event was produced. */
    private LocalDateTime timestamp;

    /** Branch context at the time the event was created (may be {@code null}). */
    private Long branchId;

    /** Username of the actor who triggered the event. */
    private String performedBy;

    /**
     * Populates {@code eventId} and {@code timestamp} with sensible defaults
     * if they have not been set by the builder.
     */
    public void initDefaults() {
        if (this.eventId == null) {
            this.eventId = UUID.randomUUID().toString();
        }
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
