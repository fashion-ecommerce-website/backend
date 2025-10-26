package com.spring.fit.backend.product.controller;
import com.spring.fit.backend.product.domain.dto.request.CreateSizeRequest;
import com.spring.fit.backend.product.domain.dto.request.UpdateSizeRequest;
import com.spring.fit.backend.product.domain.dto.response.SizeResponse;
import com.spring.fit.backend.product.service.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sizes")
@RequiredArgsConstructor
public class SizeController {

    private final SizeService sizeService;

    @PostMapping
    public ResponseEntity<SizeResponse> createSize(@RequestBody CreateSizeRequest request) {
        return ResponseEntity.ok(sizeService.createSize(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SizeResponse> updateSize(@PathVariable Short id, @RequestBody UpdateSizeRequest request) {
        return ResponseEntity.ok(sizeService.updateSize(id, request));
    }

    @GetMapping
    public ResponseEntity<List<SizeResponse>> getAllSizes() {
        return ResponseEntity.ok(sizeService.getAllSizes());
    }

    @GetMapping("/active")
    public ResponseEntity<List<SizeResponse>> getAllActiveSizes() {
        return ResponseEntity.ok(sizeService.getAllActiveSizes());
    }

    @PatchMapping("/toggle/{id}")
    public ResponseEntity<String> toggleSizeStatus(@PathVariable Short id) {
        sizeService.toggleSizeStatus(id);
        return ResponseEntity.ok("Size status toggled successfully!");
    }
}