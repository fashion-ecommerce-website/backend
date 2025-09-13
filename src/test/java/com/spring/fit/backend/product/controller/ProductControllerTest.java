package com.spring.fit.backend.product.controller;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.product.domain.dto.ProductCardView;
import com.spring.fit.backend.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Unit Tests")
public class ProductControllerTest {

    @Mock
    private ProductService productDetailService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    @DisplayName("Nên trả về danh sách sản phẩm khi gọi API với category bắt buộc")
    void shouldReturnProductListWhenCallApiWithRequiredCategory() throws Exception {
        // Given
        String category = "ao-thun";
        PageResult<ProductCardView> mockResult = createMockPageResult();
        
        when(productDetailService.filterByCategory(
                eq(category), 
                isNull(), 
                isNull(), 
                isNull(), 
                isNull(), 
                isNull(), 
                eq(0), 
                eq(12)
        )).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("category", category)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.pageSize").value(12))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false))
                .andDo(print());
    }

    @Test
    @DisplayName("Nên trả về danh sách sản phẩm khi gọi API với tất cả tham số filter")
    void shouldReturnProductListWhenCallApiWithAllFilterParameters() throws Exception {
        // Given
        String category = "ao-thun";
        String title = "áo thun nam";
        List<String> colors = List.of("Red", "Blue");
        List<String> sizes = List.of("S", "M");
        String priceBucket = "1-2m";
        String sortBy = "price_asc";
        int page = 1;
        int pageSize = 20;
        
        PageResult<ProductCardView> mockResult = createMockPageResult();
        
        when(productDetailService.filterByCategory(
                eq(category), 
                eq(title), 
                eq(colors), 
                eq(sizes), 
                eq(priceBucket), 
                eq(sortBy), 
                eq(page), 
                eq(pageSize)
        )).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("category", category)
                        .param("title", title)
                        .param("colors", "Red", "Blue")
                        .param("sizes", "S", "M")
                        .param("price", priceBucket)
                        .param("sort", sortBy)
                        .param("page", String.valueOf(page))
                        .param("pageSize", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.pageSize").value(12))
                .andDo(print());
    }

    @Test
    @DisplayName("Nên sử dụng giá trị mặc định cho page và pageSize khi không được cung cấp")
    void shouldUseDefaultValuesForPageAndPageSizeWhenNotProvided() throws Exception {
        // Given
        String category = "quan-jeans";
        PageResult<ProductCardView> mockResult = createMockPageResult();
        
        when(productDetailService.filterByCategory(
                eq(category), 
                isNull(), 
                isNull(), 
                isNull(), 
                isNull(), 
                isNull(), 
                eq(0),  // default page
                eq(12)  // default pageSize
        )).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("category", category)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("Nên trả về lỗi Bad Request khi thiếu tham số category bắt buộc")
    void shouldReturnBadRequestWhenMissingRequiredCategoryParameter() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("Nên xử lý tham số sort với các giá trị hợp lệ")
    void shouldHandleSortParameterWithValidValues() throws Exception {
        // Given
        String category = "ao-khoac";
        String[] sortValues = {"price_asc", "price_desc", "name_asc", "name_desc"};
        PageResult<ProductCardView> mockResult = createMockPageResult();
        
        for (String sortValue : sortValues) {
            when(productDetailService.filterByCategory(
                    eq(category), 
                    isNull(), 
                    isNull(), 
                    isNull(), 
                    isNull(), 
                    eq(sortValue), 
                    eq(0), 
                    eq(12)
            )).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/products")
                            .param("category", category)
                            .param("sort", sortValue)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
        }
    }

    @Test
    @DisplayName("Nên xử lý tham số price bucket với các giá trị hợp lệ")
    void shouldHandlePriceBucketParameterWithValidValues() throws Exception {
        // Given
        String category = "tui-xach";
        String[] priceBuckets = {"<1m", "1-2m", "2-3m", ">4m"};
        PageResult<ProductCardView> mockResult = createMockPageResult();
        
        for (String priceBucket : priceBuckets) {
            when(productDetailService.filterByCategory(
                    eq(category), 
                    isNull(), 
                    isNull(), 
                    isNull(), 
                    eq(priceBucket), 
                    isNull(), 
                    eq(0), 
                    eq(12)
            )).thenReturn(mockResult);

            // When & Then
            mockMvc.perform(get("/api/products")
                            .param("category", category)
                            .param("price", priceBucket)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());
        }
    }

    @Test
    @DisplayName("Nên xử lý multiple values cho colors và sizes parameters")
    void shouldHandleMultipleValuesForColorsAndSizesParameters() throws Exception {
        // Given
        String category = "ao-polo";
        PageResult<ProductCardView> mockResult = createMockPageResult();
        
        when(productDetailService.filterByCategory(
                eq(category), 
                isNull(), 
                eq(List.of("Red", "Blue", "Green")), 
                eq(List.of("S", "M", "L", "XL")), 
                isNull(), 
                isNull(), 
                eq(0), 
                eq(12)
        )).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("category", category)
                        .param("colors", "Red")
                        .param("colors", "Blue")
                        .param("colors", "Green")
                        .param("sizes", "S")
                        .param("sizes", "M")
                        .param("sizes", "L")
                        .param("sizes", "XL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    /**
     * Tạo mock PageResult để sử dụng trong các test case
     */
    private PageResult<ProductCardView> createMockPageResult() {
        // Tạo mock ProductCardView
        ProductCardView product1 = createMockProductCardView(1L, "Áo thun nam basic", "ao-thun-nam-basic", "Red", new BigDecimal("199000"));
        ProductCardView product2 = createMockProductCardView(2L, "Áo thun nữ vintage", "ao-thun-nu-vintage", "Blue", new BigDecimal("250000"));
        
        List<ProductCardView> products = List.of(product1, product2);
        
        return new PageResult<>(
                products,
                0,      // page
                12,     // pageSize
                2L,     // totalItems
                1,      // totalPages
                false,  // hasNext
                false   // hasPrevious
        );
    }

    /**
     * Tạo mock ProductCardView
     */
    private ProductCardView createMockProductCardView(Long detailId, String title, String slug, String colorName, BigDecimal price) {
        return new ProductCardView() {
            @Override
            public Long getDetailId() { return detailId; }
            
            @Override
            public String getProductTitle() { return title; }
            
            @Override
            public String getProductSlug() { return slug; }
            
            @Override
            public String getColorName() { return colorName; }
            
            @Override
            public BigDecimal getPrice() { return price; }
            
            @Override
            public Integer getQuantity() { return 100; }
            
            @Override
            public List<String> getColors() { return List.of("Red", "Blue", "Green"); }
            
            @Override
            public List<String> getImageUrls() { return List.of("image1.jpg", "image2.jpg"); }
        };
    }
}
