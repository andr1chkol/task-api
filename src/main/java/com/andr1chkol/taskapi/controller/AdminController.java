package com.andr1chkol.taskapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Admin",
        description = "Basic administrator operations"
)
public class AdminController {
    @Operation(
            summary = "Check administrator access",
            description = "Returns a successful response only for users with the ADMIN role"
    )
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("message", "Admin access granted"));
    }
}
