package com.medicalstore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class SaleRequestDTO {
    
    // Customer is optional for walk-in customers
    private Long customerId;
    
    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "Cash|Card|UPI|Credit|CASH|CARD|UPI|CREDIT", message = "Invalid payment method")
    private String paymentMethod;
    
    @Min(value = 0, message = "Discount percentage cannot be negative")
    @Max(value = 100, message = "Discount percentage cannot exceed 100")
    private Double discountPercentage;
    
    @Min(value = 0, message = "GST percentage cannot be negative")
    @Max(value = 100, message = "GST percentage cannot exceed 100")
    private Double gstPercentage;

    @NotNull(message = "Sale items are required")
    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<SaleItemDTO> items;

    @Data
    public static class SaleItemDTO {
        
        @NotNull(message = "Medicine ID is required")
        private Long medicineId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
        private Double unitPrice;
    }
}
