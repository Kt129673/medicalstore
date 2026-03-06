package com.medicalstore.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.util.Map;

/**
 * Kafka event payload for notification delivery.
 *
 * <p>
 * Event types: {@code EXPIRY_ALERT}, {@code LOW_STOCK_ALERT},
 * {@code INVOICE_NOTIFICATION}.
 * </p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationEvent extends BaseEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Discriminator for the kind of notification. */
    private String notificationType;

    /** Target phone number (E.164 format recommended). */
    private String recipientPhone;

    /** Pre-built message body. */
    private String message;

    /** Arbitrary extra data (e.g. saleId, medicineName). */
    private Map<String, Object> metadata;
}
