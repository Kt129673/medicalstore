package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String category;
    
    private String manufacturer;
    
    @Column(nullable = false)
    private Double price;
    
    @Column(nullable = false)
    private Integer quantity;
    
    private String description;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(name = "batch_number")
    private String batchNumber;
    
    @Column(unique = true)
    private String barcode;
    
    @Column(name = "created_date")
    private LocalDate createdDate;
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
    }
}
