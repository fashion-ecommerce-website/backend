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
import org.springframework.util.StringUtils;
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
    public ResponseEntity<PageResult<ProductCardWithPromotionResponse>> getProductsByCategory(
            @RequestParam
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
            @Pattern(regexp = "^(price|productTitle|name|createdAt)_(asc|desc)$", message = "Invalid sort format")
            String sortBy,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be >= 0")
            @Max(value = 100, message = "Page cannot exceed 100")
            int page,

            @RequestParam(defaultValue = "12") @Min(value = 1, message = "PageSize must be >= 1") @Max(value = 100, message = "PageSize cannot exceed 100") int pageSize) {
        log.info("Inside ProductController.getProductsByCategory category={}, title={}, colors={}, sizes={}, priceBucket={}, sortBy={}, page={}, pageSize={}",
                category, title, colors, sizes, priceBucket, sortBy, page, pageSize);
        
        try {
            PageResult<ProductCardWithPromotionResponse> result = productService.filterByCategory(
                    category, title, colors, sizes, priceBucket, sortBy, page, pageSize);

            log.info("Inside ProductController.getProductsByCategory success totalItems={}", result.totalItems());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Inside ProductController.getProductsByCategory error category={}, error={}", category, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/discounted")
    public ResponseEntity<PageResult<ProductCardWithPromotionResponse>> getDiscountedProducts(
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

            @RequestParam(defaultValue = "12") @Min(value = 1, message = "PageSize must be >= 1") @Max(value = 100, message = "PageSize cannot exceed 100") int pageSize) {
        try {
            PageResult<ProductCardWithPromotionResponse> result = productService.filterDiscounted(
                    title, colors, sizes, priceBucket, sortBy, page, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/details/{detailId}")
    public ResponseEntity<ProductDetailWithPromotionResponse> getProductDetailById(
            @PathVariable Long detailId) {
        log.info("Inside ProductController.getProductDetailById detailId={}", detailId);
        try {
            ProductDetailWithPromotionResponse response = productService.getProductDetailByIdWithPromotion(detailId);
            log.info("Inside ProductController.getProductDetailById success detailId={}", detailId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Inside ProductController.getProductDetailById error detailId={}, error={}", detailId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/details/{detailId}/color")
    public ResponseEntity<ProductDetailWithPromotionResponse> getProductDetailByColor(
            @PathVariable Long detailId,
            @RequestParam("activeColor") String activeColor,
            @RequestParam(value = "activeSize", required = false) String activeSize) {
        log.info("Inside ProductController.getProductDetailByColor baseDetailId={}, activeColor={}, activeSize={}", detailId, activeColor, activeSize);
        try {
            ProductDetailWithPromotionResponse response = StringUtils.hasText(activeSize)
                    ? productService.getProductDetailByColorAndSizeWithPromotion(detailId, activeColor, activeSize)
                    : productService.getProductDetailByColorWithPromotion(detailId, activeColor);
            log.info("Inside ProductController.getProductDetailByColor success baseDetailId={}, activeColor={}, activeSize={}", detailId, activeColor, activeSize);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Inside ProductController.getProductDetailByColor error baseDetailId={}, activeColor={}, activeSize={}, error={}", detailId, activeColor, activeSize, e.getMessage(), e);
            throw e;
        }
    }

    // ============ CRUD Endpoints ============

    @PostMapping("/admin")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestPart("product") CreateProductRequest request,
            @RequestParam(required = false) MultiValueMap<String, MultipartFile> images
    ) {
        log.info("Inside ProductController.createProduct title={}", request.getTitle());

        try {
            // Parse images by color ID
            Map<Short, List<MultipartFile>> imagesByColor = parseImagesByColor(images);

            ProductResponse response = productService.createProduct(request, imagesByColor);

            log.info("Inside ProductController.createProduct success productId={}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Inside ProductController.createProduct error title={}, error={}", request.getTitle(), e.getMessage(), e);
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
        log.info("Inside ProductController.getProductById productId={}", id);

        try {
            ProductResponse response = productService.getProductById(id);

            log.info("Inside ProductController.getProductById success productId={}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Inside ProductController.getProductById error productId={}, error={}", id, e.getMessage(), e);
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
        log.info("Inside ProductController.getAllProducts categorySlug={}, title={}, isActive={}, sortBy={}, sortDirection={}, page={}, pageSize={}", 
                categorySlug, title, isActive, sortBy, sortDirection, page, pageSize);

        try {
            PageResult<ProductListResponse> result = productService.getAllProducts(
                    categorySlug, title, isActive, sortBy, sortDirection, page, pageSize);

            log.info("Inside ProductController.getAllProducts success totalItems={}", result.totalItems());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Inside ProductController.getAllProducts error categorySlug={}, title={}, error={}", categorySlug, title, e.getMessage(), e);
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
        log.info("Inside ProductController.getProductDetailByColorAndSize productId={}, colorId={}, sizeId={}",
                productId, colorId, sizeId);

        try {
            ProductDetailByColorAndSizeResponse response = productService.getProductDetailByColorAndSize(
                    new GetProductDetailByColorAndSizeRequest(productId, colorId, sizeId)
            );

            log.info("Inside ProductController.getProductDetailByColorAndSize success productId={}, colorId={}, sizeId={}", productId, colorId, sizeId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Inside ProductController.getProductDetailByColorAndSize error productId={}, colorId={}, sizeId={}, error={}", productId, colorId, sizeId, e.getMessage(), e);
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
        log.info("Inside ProductController.updateProduct productId={}", id);

        try {
            ProductResponse response = productService.updateProduct(id, request);

            log.info("Inside ProductController.updateProduct success productId={}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Inside ProductController.updateProduct error productId={}, error={}", id, e.getMessage(), e);
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
        log.info("Inside ProductController.updateProductDetail detailId={}", detailId);

        try {
            ProductResponse.ProductDetailResponse response = productService.updateProductDetail(detailId, request);

            log.info("Inside ProductController.updateProductDetail success detailId={}", detailId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Inside ProductController.updateProductDetail error detailId={}, error={}", detailId, e.getMessage(), e);
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
        log.info("Inside ProductController.createProductDetail productId={}", productId);

        try {
            ProductResponse.ProductDetailResponse response = productService.createProductDetail(productId, request, images);

            log.info("Inside ProductController.createProductDetail success productId={}, detailId={}", productId, response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Inside ProductController.createProductDetail error productId={}, error={}", productId, e.getMessage(), e);
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
        log.info("Inside ProductController.addProductDetailImages detailId={}, imageCount={}", detailId, newImages.size());

        try {
            ProductResponse.ProductDetailResponse response = productService.addProductDetailImages(detailId, newImages);

            log.info("Inside ProductController.addProductDetailImages success detailId={}, imageCount={}", detailId, newImages.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Inside ProductController.addProductDetailImages error detailId={}, imageCount={}, error={}", detailId, newImages.size(), e.getMessage(), e);
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
        log.info("Inside ProductController.deleteProductDetailImages detailId={}, imageUrlCount={}", detailId, request.getImageUrls().size());

        try {
            ProductResponse.ProductDetailResponse response = productService.deleteProductDetailImages(detailId, request);

            log.info("Inside ProductController.deleteProductDetailImages success detailId={}, imageUrlCount={}", detailId, request.getImageUrls().size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Inside ProductController.deleteProductDetailImages error detailId={}, imageUrlCount={}, error={}", detailId, request.getImageUrls().size(), e.getMessage(), e);
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
        log.info("Inside ProductController.deleteProduct productId={}", id);

        try {
            productService.deleteProduct(id);

            log.info("Inside ProductController.deleteProduct success productId={}", id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Inside ProductController.deleteProduct error productId={}, error={}", id, e.getMessage(), e);
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
        log.info("Inside ProductController.deleteProductDetail detailId={}", id);

        try {
            productService.deleteProductDetail(id);

            log.info("Inside ProductController.deleteProductDetail success detailId={}", id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Inside ProductController.deleteProductDetail error detailId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    // ============ Helper Methods ============

    private Map<Short, List<MultipartFile>> parseImagesByColor(MultiValueMap<String, MultipartFile> images) {
        Map<Short, List<MultipartFile>> imagesByColor = new HashMap<>();

        if (images != null) {
            images.forEach((key, files) -> {
                try {
                    Short colorId = null;

                    // Support keys: "detail_1", "detail_2", etc. where number is colorId
                    if (key != null && key.startsWith("detail_")) {
                        String colorIdStr = key.substring(7); // Remove "detail_" prefix
                        colorId = Short.parseShort(colorIdStr);
                    }

                    if (colorId != null) {
                        if (files != null && files.size() > 5) {
                            throw new IllegalArgumentException("Each color variant can only have maximum 5 images");
                        }
                        imagesByColor.put(colorId, files);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid image key format: {}", key);
                }
            });
        }

        return imagesByColor;
    }
}
