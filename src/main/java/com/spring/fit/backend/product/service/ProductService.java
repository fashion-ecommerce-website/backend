package com.spring.fit.backend.product.service;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.product.domain.dto.request.*;
import com.spring.fit.backend.product.domain.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductService {
    PageResult<ProductCardWithPromotionResponse> filterByCategory(
            String categorySlug,
            String title,
            List<String> colorNames,      // có thể null/empty
            List<String> sizeCodes,       // có thể null/empty
            String priceBucket,           // "<1m" | "1-2m" | "2-3m" | ">3m" | null
            String sortBy,                // "price_asc","price_desc","name_asc","name_desc"
            int page,
            int pageSize);

    PageResult<ProductCardWithPromotionResponse> filterDiscounted(
            String title,
            List<String> colorNames,
            List<String> sizeCodes,
            String priceBucket,
            String sortBy,
            int page,
            int pageSize);
    
    ProductDetailResponse getProductDetailById(Long detailId);
    ProductDetailWithPromotionResponse getProductDetailByIdWithPromotion(Long detailId);

    ProductDetailResponse getProductDetailByColor(Long baseDetailId, String activeColor);
    ProductDetailWithPromotionResponse getProductDetailByColorWithPromotion(Long baseDetailId, String activeColor);

    ProductDetailResponse getProductDetailByColorAndSize(Long baseDetailId, String activeColor, String activeSize);
    ProductDetailWithPromotionResponse getProductDetailByColorAndSizeWithPromotion(Long baseDetailId, String activeColor, String activeSize);

    List<ProductCardView> getRecentlyViewedProducts(List<Long> productIds, Long userId);
    List<ProductCardWithPromotionResponse> getRecentlyViewedProductsWithPromotion(List<Long> productIds, Long userId);
    
    List<ProductCardWithPromotionResponse> getProductsWithPromotionByProductIds(List<Long> productIds, Long userId);

    ProductResponse createProduct(CreateProductRequest request, Map<Short, List<MultipartFile>> imagesByColor);

    ProductResponse getProductById(Long id);

    PageResult<ProductListResponse> getAllProducts(String categorySlug, String title, Boolean isActive,
                                                   String sortBy, String sortDirection, int page, int pageSize);

    ProductDetailByColorAndSizeResponse getProductDetailByColorAndSize(GetProductDetailByColorAndSizeRequest request);

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    ProductResponse.ProductDetailResponse updateProductDetail(Long detailId, UpdateProductDetailRequest request);

    ProductResponse.ProductDetailResponse createProductDetail(Long productId, CreateProductDetailRequest request,
                                              List<MultipartFile> images);

    ProductResponse.ProductDetailResponse addProductDetailImages(Long detailId, List<MultipartFile> newImages);

    ProductResponse.ProductDetailResponse deleteProductDetailImages(Long detailId, DeleteProductDetailImagesRequest request);

    void toggleProductActive(Long id);

    void toggleProductDetailActive(Long id);

    List<ProductSimpleResponse> getAllProductsSimple();

    List<ProductDetailSimpleResponse> getAllProductDetailsSimple();

    List<NewArrivalsResponse> getNewArrivalsByRootCategories(String categorySlug, int limit);

    List<ProductCardWithPromotionResponse> getTopSellingProducts();

}
