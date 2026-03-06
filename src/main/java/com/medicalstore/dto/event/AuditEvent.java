package com.medicalstore.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;

/**
 * Kafka event payload for audit-log entries.
 *
 * <p>
 * Event types: {@code USER_CREATED}, {@code USER_UPDATED},
 * {@code ROLE_CHANGED}, {@code USER_DELETED}.
 * </p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditEvent extends BaseEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    private String entityName;
    private Long entityId;
    private String action;
    private String description;
    private String performedByRole;
    private String oldValue;
    private String newValue;
    private String ipAddress;
}
