package com.medicalstore.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;

/**
 * Kafka event payload for purchase-order actions.
 *
 * <p>
 * Event types: {@code PURCHASE_ORDER_CREATED},
 * {@code PURCHASE_ORDER_RECEIVED}, {@code PURCHASE_ORDER_CANCELLED}.
 * </p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseEvent extends BaseEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long purchaseOrderId;
    private String orderNumber;
    private Long supplierId;
    private String supplierName;
    private Double totalAmount;
    private int itemCount;
    private String status;
}
