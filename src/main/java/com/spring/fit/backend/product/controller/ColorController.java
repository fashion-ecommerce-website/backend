package com.spring.fit.backend.product.controller;


import com.spring.fit.backend.product.domain.dto.request.ColorRequest;
import com.spring.fit.backend.product.domain.dto.response.ColorResponse;
import com.spring.fit.backend.product.service.ColorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colors")
@RequiredArgsConstructor
public class ColorController {

    private final ColorService colorService;

    @PostMapping
    public ResponseEntity<ColorResponse> createColor(@Valid @RequestBody ColorRequest request) {
        return ResponseEntity.ok(colorService.createColor(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColorResponse> updateColor(
            @PathVariable Short id,
            @Valid @RequestBody ColorRequest request) {
        return ResponseEntity.ok(colorService.updateColor(id, request));
    }

    @GetMapping
    public ResponseEntity<List<ColorResponse>> getAllColors() {
        return ResponseEntity.ok(colorService.getAllColors());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ColorResponse>> getAllActiveColors() {
        return ResponseEntity.ok(colorService.getAllActiveColors());
    }

    @PatchMapping("/toggle/{id}")
    public ResponseEntity<String> toggleColorStatus(@PathVariable Short id) {
        colorService.toggleColorStatus(id);
        return ResponseEntity.ok("Color status toggled successfully!");
    }
}