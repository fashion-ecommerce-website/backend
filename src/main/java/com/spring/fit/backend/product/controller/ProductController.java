package com.spring.fit.backend.product.controller;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.product.domain.dto.ProductCardView;
import com.spring.fit.backend.product.domain.dto.ProductDetailResponse;
import com.spring.fit.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {
    private final ProductService productDetailService;

    @GetMapping
    public ResponseEntity<PageResult<ProductCardView>> getProductsByCategory(
            @RequestParam @NotBlank(message = "Category cannot be blank") @Size(max = 100, message = "Category cannot exceed 100 characters") String category,

            @RequestParam(required = false) @Size(max = 255, message = "Title cannot exceed 255 characters") String title,

            @RequestParam(required = false) @Size(max = 20, message = "Cannot select more than 20 colors") List<@NotBlank @Size(max = 50) String> colors,

            @RequestParam(required = false) @Size(max = 20, message = "Cannot select more than 20 sizes") List<@NotBlank @Size(max = 10) String> sizes,

            @RequestParam(required = false, name = "price") @Pattern(regexp = "^(<1m|1-2m|2-3m|>4m)$", message = "Invalid price bucket format") String priceBucket,

            @RequestParam(required = false, name = "sort") @Pattern(regexp = "^(price|productTitle|name)_(asc|desc)$", message = "Invalid sort format") String sortBy,

            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be >= 0") @Max(value = 100, message = "Page cannot exceed 100") int page,

            @RequestParam(defaultValue = "12") @Min(value = 1, message = "PageSize must be >= 1") @Max(value = 100, message = "PageSize cannot exceed 100") int pageSize) {
        log.info("Filtering products: category={}, title={}, page={}, pageSize={}",
                category, title, page, pageSize);

        try {
            PageResult<ProductCardView> result = productDetailService.filterByCategory(
                    category, title, colors, sizes, priceBucket, sortBy, page, pageSize);

            log.info("Successfully filtered products: found {} items", result.totalItems());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error filtering products: category={}, error={}", category, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{detailId}")
    public ResponseEntity<ProductDetailResponse> getProductDetailById(
            @PathVariable Long detailId) {
        log.info("Getting product detail by ID: {}", detailId);

        try {
            ProductDetailResponse response = productDetailService.getProductDetailById(detailId);
            log.info("Successfully retrieved product detail for ID: {}", detailId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting product detail by ID {}: {}", detailId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{detailId}/color")
    public ResponseEntity<ProductDetailResponse> getProductDetailByColor(
            @PathVariable Long detailId,
            @RequestParam("activeColor") String activeColor) {
        log.info("Getting product detail by color: baseDetailId={}, activeColor={}", detailId, activeColor);
        try {
            ProductDetailResponse response = productDetailService.getProductDetailByColor(detailId, activeColor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting product detail by color. detailId={}, color={}, error={}", detailId, activeColor,
                    e.getMessage(), e);
            throw e;
        }
    }
}
