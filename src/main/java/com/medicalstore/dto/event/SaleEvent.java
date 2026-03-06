package com.medicalstore.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Kafka event payload for sale-related actions.
 *
 * <p>
 * Event types: {@code SALE_CREATED}, {@code SALE_DELETED},
 * {@code SALE_RETURNED}.
 * </p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SaleEvent extends BaseEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long saleId;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Double totalAmount;
    private Double finalAmount;
    private String paymentMethod;
    private int itemCount;
    private List<SaleItemInfo> items;

    /**
     * Lightweight representation of a sale item for event payloads.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleItemInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long medicineId;
        private String medicineName;
        private int quantity;
        private Double unitPrice;
    }
}
