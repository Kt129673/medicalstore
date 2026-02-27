package com.medicalstore.controller.api;

import com.medicalstore.dto.MedicineDTO;
import com.medicalstore.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
public class PosApiController {

    private final MedicineService medicineService;

    @GetMapping("/search")
    public ResponseEntity<List<MedicineDTO>> searchMedicines(
            @RequestParam(name = "q", defaultValue = "") String query) {

        if (query.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        List<MedicineDTO> results = medicineService.searchMedicinesForPos(query);
        return ResponseEntity.ok(results);
    }
}
