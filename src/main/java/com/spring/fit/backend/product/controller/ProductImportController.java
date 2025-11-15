package com.spring.fit.backend.product.controller;

import com.spring.fit.backend.product.domain.dto.response.ProductDetailPreviewResponse;
import com.spring.fit.backend.product.domain.dto.response.ProductGroupResponse;
import com.spring.fit.backend.product.service.ProductImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products/import")
@RequiredArgsConstructor
public class ProductImportController {

    private final ProductImportService productImportService;

    @PostMapping("/preview")
    public ResponseEntity<List<ProductGroupResponse>> importPreview(@RequestParam("file") MultipartFile file) {
        List<ProductGroupResponse> result = productImportService.parseCsv(file);
        return ResponseEntity.ok(result);
    }

    // --- Lưu tất cả product từ CSV ---
    @PostMapping("/save")
    public ResponseEntity<String> saveAllProducts(@RequestBody List<ProductGroupResponse> groups) {

        if (groups == null || groups.isEmpty()) {
            return ResponseEntity.badRequest().body(" No product data provided");
        }

        // Gọi service để lưu tất cả
        productImportService.saveAllImportedProducts(groups);

        return ResponseEntity.ok("✅ Saved " + groups.size() + " products successfully!");
    }
}