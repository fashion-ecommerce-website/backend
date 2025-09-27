package com.spring.fit.backend.product.service;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.product.domain.dto.request.*;
import com.spring.fit.backend.product.domain.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    PageResult<ProductCardView> filterByCategory(
            String categorySlug,
            String title,
            List<String> colorNames,      // có thể null/empty
            List<String> sizeCodes,       // có thể null/empty
            String priceBucket,           // "<1m" | "1-2m" | "2-3m" | ">4m" | null
            String sortBy,                // "price_asc","price_desc","name_asc","name_desc"
            int page,
            int pageSize);
    
    ProductDetailResponse getProductDetailById(Long detailId);

    ProductDetailResponse getProductDetailByColor(Long baseDetailId, String activeColor);

    ProductDetailResponse getProductDetailByColorAndSize(Long baseDetailId, String activeColor, String activeSize);

    List<ProductCardView> getRecentlyViewedProducts(List<Long> productIds, Long userId);

//    // ============ CRUD Operations ============

    /**
     * Tạo sản phẩm mới với danh sách images cho mỗi variant
     * @param request Thông tin sản phẩm
     * @param imagesByDetail Map từ index của ProductDetailRequest sang danh sách file images (tối đa 5 images/detail)
     * @return ProductResponse
     */
    ProductResponse createProduct(CreateProductRequest request,
                                  java.util.Map<Integer, List<MultipartFile>> imagesByDetail);

    /**
     * Lấy thông tin chi tiết sản phẩm theo ID
     * @param id ID sản phẩm
     * @return ProductResponse
     */
    ProductResponse getProductById(Long id);

    /**
     * Lấy danh sách sản phẩm với filter và pagination
     * @param categorySlug Filter theo category slug
     * @param title Search theo title
     * @param isActive Filter theo trạng thái active/inactive
     * @param sortBy Sort theo field (title hoặc createdAt)
     * @param sortDirection Sort direction (asc hoặc desc)
     * @param page Số trang
     * @param pageSize Kích thước trang
     * @return PageResult<ProductListResponse>
     */
    PageResult<ProductListResponse> getAllProducts(String categorySlug, String title, Boolean isActive,
                                                   String sortBy, String sortDirection, int page, int pageSize);
    /**
     * Lấy thông tin chi tiết sản phẩm theo productId, colorId, sizeId
     * @param request Thông tin request (productId, colorId, sizeId)
     * @return ProductDetailByColorAndSizeResponse
     */
    ProductDetailByColorAndSizeResponse getProductDetailByColorAndSize(GetProductDetailByColorAndSizeRequest request);

    /**
     * Cập nhật thông tin sản phẩm
     * @param id ID sản phẩm
     * @param request Thông tin cập nhật
     * @return ProductRespons
     */
    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    /**
     * Cập nhật thông tin chi tiết sản phẩm (price, quantity, color, size)
     * @param detailId ID product detail
     * @param request Thông tin cập nhật
     * @return ProductDetailResponse
     */
    ProductResponse.ProductDetailResponse updateProductDetail(Long detailId, UpdateProductDetailRequest request);

    /**
     * Tạo mới một product detail cho sản phẩm
     * @param productId ID sản phẩm
     * @param request Thông tin product detail
     * @param images Danh sách ảnh (tối đa 5 ảnh)
     * @return ProductDetailResponse
     */
    ProductResponse.ProductDetailResponse createProductDetail(Long productId, CreateProductDetailRequest request,
                                              List<MultipartFile> images);

    /**
     * Thêm ảnh mới cho một product detail
     * @param detailId ID product detail
     * @param newImages Danh sách ảnh mới (tối đa 5 ảnh)
     * @return ProductDetailResponse
     */
    ProductResponse.ProductDetailResponse addProductDetailImages(Long detailId, List<MultipartFile> newImages);

    /**
     * Xóa ảnh của một product detail
     * @param detailId ID product detail
     * @param request Danh sách URL ảnh cần xóa
     * @return ProductDetailResponse
     */
    ProductResponse.ProductDetailResponse deleteProductDetailImages(Long detailId, DeleteProductDetailImagesRequest request);

    /**
     * Xóa sản phẩm (soft delete - set isActive = false)
     * @param id ID sản phẩm
     */
    void deleteProduct(Long id);

    /**
     * Xóa sản phẩm (soft delete - set isActive = false)
     * @param id ID sản phẩm
     */
    void deleteProductDetail(Long id);
}

