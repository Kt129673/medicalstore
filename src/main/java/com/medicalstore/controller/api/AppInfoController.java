package com.medicalstore.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringBootVersion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Application info endpoint — returns version, runtime, and environment
 * details.
 */
@RestController
@RequestMapping("/api/v1/info")
@Tag(name = "Application Info", description = "Application version and runtime information")
public class AppInfoController {

    @GetMapping
    @Operation(summary = "Get application info", description = "Returns version, Java runtime, Spring Boot version, and server time")
    public ResponseEntity<Map<String, Object>> appInfo() {
        return ResponseEntity.ok(Map.of(
                "name", "MedicalStore",
                "version", "1.0.0",
                "description", "Pharmacy Inventory & Sales Management System",
                "java", System.getProperty("java.version"),
                "springBoot", SpringBootVersion.getVersion(),
                "serverTime", LocalDateTime.now().toString(),
                "os", System.getProperty("os.name") + " " + System.getProperty("os.version")));
    }
}
