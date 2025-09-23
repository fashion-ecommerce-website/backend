package com.spring.fit.backend.product.controller;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.product.domain.dto.request.*;
import com.spring.fit.backend.product.domain.dto.response.*;
import com.spring.fit.backend.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.MultiValueMap;

import jakarta.validation.constraints.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PageResult<ProductCardView>> getProductsByCategory(
            @RequestParam
            @NotBlank(message = "Category cannot be blank")
            @Size(max = 100, message = "Category cannot exceed 100 characters")
            String category,

            @RequestParam(required = false)
            @Size(max = 255, message = "Title cannot exceed 255 characters")
            String title,

            @RequestParam(required = false)
            @Size(max = 20, message = "Cannot select more than 20 colors")
            List<@NotBlank @Size(max = 50) String> colors,

            @RequestParam(required = false)
            @Size(max = 20, message = "Cannot select more than 20 sizes")
            List<@NotBlank @Size(max = 10) String> sizes,

            @RequestParam(required = false, name="price")
            @Pattern(regexp = "^(<1m|1-2m|2-3m|>4m)$", message = "Invalid price bucket format")
            String priceBucket,

            @RequestParam(required = false, name="sort")
            @Pattern(regexp = "^(price|productTitle|name)_(asc|desc)$", message = "Invalid sort format")
            String sortBy,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be >= 0")
            @Max(value = 100, message = "Page cannot exceed 100")
            int page,

            @RequestParam(defaultValue = "12")
            @Min(value = 1, message = "PageSize must be >= 1")
            @Max(value = 100, message = "PageSize cannot exceed 100")
            int pageSize
    ) {
        log.info("Filtering products: category={}, title={}, page={}, pageSize={}",
                category, title, page, pageSize);
        
        try {
            PageResult<ProductCardView> result = productService.filterByCategory(
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
            ProductDetailResponse response = productService.getProductDetailById(detailId);
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
            ProductDetailResponse response = productService.getProductDetailByColor(detailId, activeColor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting product detail by color. detailId={}, color={}, error={}", detailId, activeColor,
                    e.getMessage(), e);
            throw e;
        }
    }

    // ============ CRUD Endpoints ============

    @PostMapping("/admin")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestPart("product") CreateProductRequest request,
            @RequestParam(required = false) MultiValueMap<String, MultipartFile> images
    ) {
        log.info("Creating new product: {}", request.getTitle());

        try {
            // Parse images by detail index
            Map<Integer, List<MultipartFile>> imagesByDetail = parseImagesByDetail(images);

            ProductResponse response = productService.createProduct(request, imagesByDetail);

            log.info("Successfully created product with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable
            @NotNull(message = "Product ID cannot be null")
            @Positive(message = "Product ID must be positive")
            Long id
    ) {
        log.info("Getting product by ID: {}", id);

        try {
            ProductResponse response = productService.getProductById(id);

            log.info("Successfully retrieved product ID: {}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting product ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<PageResult<ProductListResponse>> getAllProducts(
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "title") 
            @Pattern(regexp = "^(title|createdAt)$", message = "Sort field must be 'title' or 'createdAt'")
            String sortBy,
            @RequestParam(defaultValue = "asc")
            @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
            String sortDirection,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be >= 0")
            @Max(value = 100, message = "Page cannot exceed 100")
            int page,
            @RequestParam(defaultValue = "12")
            @Min(value = 1, message = "PageSize must be >= 1")
            @Max(value = 100, message = "PageSize cannot exceed 100")
            int pageSize
    ) {
        log.info("Getting all products with filter: categorySlug={}, title={}, isActive={}, sortBy={}, sortDirection={}, page={}, pageSize={}", 
                categorySlug, title, isActive, sortBy, sortDirection, page, pageSize);

        try {
            PageResult<ProductListResponse> result = productService.getAllProducts(
                    categorySlug, title, isActive, sortBy, sortDirection, page, pageSize);

            log.info("Successfully retrieved {} products", result.totalItems());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error getting all products: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/admin/details")
    public ResponseEntity<ProductDetailByColorAndSizeResponse> getProductDetailByColorAndSize(
            @RequestParam
            @NotNull(message = "Product ID cannot be null")
            @Positive(message = "Product ID must be positive")
            Long productId,

            @RequestParam
            @NotNull(message = "Color ID cannot be null")
            @Positive(message = "Color ID must be positive")
            Short colorId,

            @RequestParam
            @NotNull(message = "Size ID cannot be null")
            @Positive(message = "Size ID must be positive")
            Short sizeId
    ) {
        log.info("Getting product detail by color and size: productId={}, colorId={}, sizeId={}",
                productId, colorId, sizeId);

        try {
            ProductDetailByColorAndSizeResponse response = productService.getProductDetailByColorAndSize(
                    new GetProductDetailByColorAndSizeRequest(productId, colorId, sizeId)
            );

            log.info("Successfully retrieved product detail");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting product detail by color and size: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable
            @NotNull(message = "Product ID cannot be null")
            @Positive(message = "Product ID must be positive")
            Long id,

            @Valid @RequestBody UpdateProductRequest request
    ) {
        log.info("Updating product ID: {}", id);

        try {
            ProductResponse response = productService.updateProduct(id, request);

            log.info("Successfully updated product ID: {}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating product ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/admin/details/{detailId}")
    public ResponseEntity<ProductResponse.ProductDetailResponse> updateProductDetail(
            @PathVariable
            @NotNull(message = "Product detail ID cannot be null")
            @Positive(message = "Product detail ID must be positive")
            Long detailId,

            @Valid @RequestBody UpdateProductDetailRequest request
    ) {
        log.info("Updating product detail ID: {}", detailId);

        try {
            ProductResponse.ProductDetailResponse response = productService.updateProductDetail(detailId, request);

            log.info("Successfully updated product detail ID: {}", detailId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating product detail ID {}: {}", detailId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/admin/{productId}/details")
    public ResponseEntity<ProductResponse.ProductDetailResponse> createProductDetail(
            @PathVariable
            @NotNull(message = "Product ID cannot be null")
            @Positive(message = "Product ID must be positive")
            Long productId,

            @Valid @RequestPart("detail") CreateProductDetailRequest request,
            @RequestParam(required = false) List<MultipartFile> images
    ) {
        log.info("Creating product detail for product ID: {}", productId);

        try {
            ProductResponse.ProductDetailResponse response = productService.createProductDetail(productId, request, images);

            log.info("Successfully created product detail with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating product detail for product ID {}: {}", productId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/admin/details/{detailId}/images")
    public ResponseEntity<ProductResponse.ProductDetailResponse> addProductDetailImages(
            @PathVariable
            @NotNull(message = "Product detail ID cannot be null")
            @Positive(message = "Product detail ID must be positive")
            Long detailId,

            @RequestParam("images")
            @NotEmpty(message = "Images cannot be empty")
            List<MultipartFile> newImages
    ) {
        log.info("Adding images for product detail ID: {}", detailId);

        try {
            ProductResponse.ProductDetailResponse response = productService.addProductDetailImages(detailId, newImages);

            log.info("Successfully added images for product detail ID: {}", detailId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error adding images for product detail ID {}: {}", detailId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/admin/details/{detailId}/images")
    public ResponseEntity<ProductResponse.ProductDetailResponse> deleteProductDetailImages(
            @PathVariable
            @NotNull(message = "Product detail ID cannot be null")
            @Positive(message = "Product detail ID must be positive")
            Long detailId,

            @Valid @RequestBody DeleteProductDetailImagesRequest request
    ) {
        log.info("Deleting images for product detail ID: {}", detailId);

        try {
            ProductResponse.ProductDetailResponse response = productService.deleteProductDetailImages(detailId, request);

            log.info("Successfully deleted images for product detail ID: {}", detailId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting images for product detail ID {}: {}", detailId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable
            @NotNull(message = "Product ID cannot be null")
            @Positive(message = "Product ID must be positive")
            Long id
    ) {
        log.info("Deleting product ID: {}", id);

        try {
            productService.deleteProduct(id);

            log.info("Successfully deleted product ID: {}", id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error deleting product ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/admin/details/{id}")
    public ResponseEntity<Void> deleteProductDetail(
            @PathVariable
            @NotNull(message = "Product ID cannot be null")
            @Positive(message = "Product ID must be positive")
            Long id
    ) {
        log.info("Deleting product detail ID: {}", id);

        try {
            productService.deleteProductDetail(id);

            log.info("Successfully deleted product detail ID: {}", id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error deleting product ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // ============ Helper Methods ============

    private Map<Integer, List<MultipartFile>> parseImagesByDetail(MultiValueMap<String, MultipartFile> images) {
        Map<Integer, List<MultipartFile>> imagesByDetail = new HashMap<>();

        if (images != null) {
            images.forEach((key, files) -> {
                try {
                    Integer detailIndex = null;

                    // Support keys: "images[detail_0]" or "detail_0"
                    if (key != null) {
                        if (key.startsWith("images[detail_") && key.endsWith("]")) {
                            String idx = key.substring("images[detail_".length(), key.length() - 1);
                            detailIndex = Integer.parseInt(idx);
                        } else if (key.startsWith("detail_")) {
                            detailIndex = Integer.parseInt(key.substring(7));
                        }
                    }

                    if (detailIndex != null) {
                        if (files != null && files.size() > 5) {
                            throw new IllegalArgumentException("Each product variant can only have maximum 5 images");
                        }
                        imagesByDetail.put(detailIndex, files);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid image key format: {}", key);
                }
            });
        }

        return imagesByDetail;
    }
}
