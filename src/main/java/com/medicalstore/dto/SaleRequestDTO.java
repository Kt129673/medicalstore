package com.medicalstore.dto;

import lombok.Data;
import java.util.List;

@Data
public class SaleRequestDTO {
    private Long customerId;
    private String paymentMethod;
    private Double discountPercentage;
    private Double gstPercentage;

    private List<SaleItemDTO> items;

    @Data
    public static class SaleItemDTO {
        private Long medicineId;
        private Integer quantity;
        private Double unitPrice;
    }
}
