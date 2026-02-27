package com.medicalstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDTO {
    private Long id;
    private String name;
    private String category;
    private Double price;
    private Integer quantity;
    private String batchNumber;
    private Double gstPercentage;

    // For fast search dropdowns
}
