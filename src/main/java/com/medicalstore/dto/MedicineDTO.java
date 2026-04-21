package com.medicalstore.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDTO {
    
    private Long id;
    
    @NotBlank(message = "Medicine name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;
    
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
    
    @NotBlank(message = "Batch number is required")
    @Size(max = 50, message = "Batch number cannot exceed 50 characters")
    private String batchNumber;
    
    @NotNull(message = "GST percentage is required")
    @Min(value = 0, message = "GST percentage cannot be negative")
    @Max(value = 100, message = "GST percentage cannot exceed 100")
    private Double gstPercentage;

    // For fast search dropdowns
}
