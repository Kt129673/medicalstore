package com.medicalstore.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.time.LocalDate;

/**
 * Kafka event payload for inventory / medicine changes.
 *
 * <p>
 * Event types: {@code MEDICINE_CREATED}, {@code MEDICINE_UPDATED},
 * {@code MEDICINE_DELETED}, {@code STOCK_CHANGED}.
 * </p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryEvent extends BaseEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long medicineId;
    private String medicineName;
    private String batchNumber;
    private Integer previousQuantity;
    private Integer newQuantity;
    private LocalDate expiryDate;
    private String category;
}
