package com.spring.fit.backend.product.controller;

import com.spring.fit.backend.product.domain.dto.request.ProductDetailCheckRequest;
import com.spring.fit.backend.product.domain.dto.response.ProductDetailCheckResponse;
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

    @PostMapping("/zip-preview")
    public ResponseEntity<List<ProductGroupResponse>> importProductsWithZips(
            @RequestParam("csv") MultipartFile csv,
            @RequestParam("zips") List<MultipartFile> zips) {
        System.out.println("=== CSV: " + csv.getOriginalFilename());
        System.out.println("=== ZIP COUNT: " + zips.size());
        zips.forEach(z -> System.out.println("ZIP FILE: " + z.getOriginalFilename()));
        List<ProductGroupResponse> preview = productImportService.parseCsvWithZips(csv, zips);
        return ResponseEntity.ok(preview);
    }

    @PostMapping("/zip-save")
    public ResponseEntity<Void> saveImportedZip(
            @RequestBody List<ProductGroupResponse> groups) {

        if (groups == null || groups.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        productImportService.saveAllImportedProducts(groups);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-detail")
    public ResponseEntity<ProductDetailCheckResponse> checkProductDetail(
            @RequestBody ProductDetailCheckRequest request
    ) {
        ProductDetailCheckResponse response = productImportService.checkProductDetail(request);
        return ResponseEntity.ok(response);
    }
}
