package com.spring.fit.backend.product.service;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.product.domain.dto.response.ProductCardView;
import com.spring.fit.backend.product.repository.ProductRepository;
import com.spring.fit.backend.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductDetailService Unit Tests")
public class ProductServiceTest {

    @Mock
    private ProductRepository productDetailRepository;

    @InjectMocks
    private ProductServiceImpl productDetailService;

    private ProductCardView mockProductCardView;
    private Page<ProductCardView> mockPage;

    @BeforeEach
    void setUp() {
        mockProductCardView = createMockProductCardView();
        mockPage = new PageImpl<>(List.of(mockProductCardView), PageRequest.of(0, 12), 1);
    }

    @Test
    @DisplayName("Nên filter sản phẩm với tham số category cơ bản")
    void shouldFilterProductsWithBasicCategoryParameter() {
        // Given
        String categorySlug = "ao-thun";
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(true),
                eq(true),
                eq("productTitle"),
                eq("asc"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, null, null, null, null, null, 0, 12
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.pageSize()).isEqualTo(12);
        assertThat(result.totalItems()).isEqualTo(1);
    }

    @Test
    @DisplayName("Nên xử lý price bucket '<1m' đúng cách")
    void shouldHandlePriceBucketLessThan1Million() {
        // Given
        String categorySlug = "ao-thun";
        String priceBucket = "<1m";
        BigDecimal expectedMaxPrice = BigDecimal.valueOf(1_000_000);
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(expectedMaxPrice),
                eq(true),
                eq(true),
                eq("productTitle"),
                eq("asc"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, null, null, null, priceBucket, null, 0, 12
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("Nên xử lý price bucket '1-2m' đúng cách")
    void shouldHandlePriceBucket1To2Million() {
        // Given
        String categorySlug = "quan-jeans";
        String priceBucket = "1-2m";
        BigDecimal expectedMinPrice = BigDecimal.valueOf(1_000_000);
        BigDecimal expectedMaxPrice = BigDecimal.valueOf(2_000_000);
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                isNull(),
                isNull(),
                isNull(),
                eq(expectedMinPrice),
                eq(expectedMaxPrice),
                eq(true),
                eq(true),
                eq("productTitle"),
                eq("asc"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, null, null, null, priceBucket, null, 0, 12
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("Nên xử lý price bucket '2-3m' đúng cách")
    void shouldHandlePriceBucket2To3Million() {
        // Given
        String categorySlug = "ao-khoac";
        String priceBucket = "2-3m";
        BigDecimal expectedMinPrice = BigDecimal.valueOf(2_000_000);
        BigDecimal expectedMaxPrice = BigDecimal.valueOf(3_000_000);
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                isNull(),
                isNull(),
                isNull(),
                eq(expectedMinPrice),
                eq(expectedMaxPrice),
                eq(true),
                eq(true),
                eq("productTitle"),
                eq("asc"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, null, null, null, priceBucket, null, 0, 12
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("Nên xử lý price bucket '>4m' đúng cách")
    void shouldHandlePriceBucketGreaterThan4Million() {
        // Given
        String categorySlug = "tui-xach";
        String priceBucket = ">4m";
        BigDecimal expectedMinPrice = BigDecimal.valueOf(4_000_000);
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                isNull(),
                isNull(),
                isNull(),
                eq(expectedMinPrice),
                isNull(),
                eq(true),
                eq(true),
                eq("productTitle"),
                eq("asc"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, null, null, null, priceBucket, null, 0, 12
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("Nên xử lý price bucket không hợp lệ bằng cách bỏ qua")
    void shouldHandleInvalidPriceBucketByIgnoring() {
        // Given
        String categorySlug = "giay-sneaker";
        String invalidPriceBucket = "invalid-bucket";
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(true),
                eq(true),
                eq("productTitle"),
                eq("asc"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, null, null, null, invalidPriceBucket, null, 0, 12
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("Nên xử lý sorting 'price_asc' đúng cách")
    void shouldHandlePriceAscendingSorting() {
        // Given
        String categorySlug = "ao-polo";
        String sortBy = "price_asc";
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(true),
                eq(true),
                eq("price"),
                eq("asc"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, null, null, null, null, sortBy, 0, 12
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("Nên xử lý sorting 'name_desc' đúng cách")
    void shouldHandleNameDescendingSorting() {
        // Given
        String categorySlug = "quan-short";
        String sortBy = "name_desc";
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(true),
                eq(true),
                eq("name"),
                eq("desc"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, null, null, null, null, sortBy, 0, 12
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("Nên ném exception khi sortBy có format không đúng")
    void shouldThrowExceptionWhenSortByHasInvalidFormat() {
        // Given
        String categorySlug = "ao-vest";
        String invalidSortBy = "invalid_sort_format_too_many_parts";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            productDetailService.filterByCategory(
                    categorySlug, null, null, null, null, invalidSortBy, 0, 12
            );
        });
    }

    @Test
    @DisplayName("Nên xử lý colors và sizes không rỗng đúng cách")
    void shouldHandleNonEmptyColorsAndSizes() {
        // Given
        String categorySlug = "ao-thun";
        List<String> colors = List.of("Red", "Blue");
        List<String> sizes = List.of("S", "M", "L");
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                isNull(),
                eq(colors),
                eq(sizes),
                isNull(),
                isNull(),
                eq(false),
                eq(false),
                eq("productTitle"),
                eq("asc"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, null, colors, sizes, null, null, 0, 12
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("Nên xử lý colors và sizes rỗng như null")
    void shouldHandleEmptyColorsAndSizesAsNull() {
        // Given
        String categorySlug = "ao-thun";
        List<String> emptyColors = List.of();
        List<String> emptySizes = List.of();
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(true),
                eq(true),
                eq("productTitle"),
                eq("asc"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, null, emptyColors, emptySizes, null, null, 0, 12
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("Nên xử lý title search đúng cách")
    void shouldHandleTitleSearchCorrectly() {
        // Given
        String categorySlug = "ao-thun";
        String title = "áo thun nam";
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                eq(title),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(true),
                eq(true),
                eq("productTitle"),
                eq("asc"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, title, null, null, null, null, 0, 12
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("Nên xử lý title trống hoặc blank như null")
    void shouldHandleBlankTitleAsNull() {
        // Given
        String categorySlug = "ao-thun";
        String blankTitle = "   ";
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(true),
                eq(true),
                eq("productTitle"),
                eq("asc"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, blankTitle, null, null, null, null, 0, 12
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("Nên đảm bảo page và pageSize có giá trị tối thiểu")
    void shouldEnsureMinimumPageAndPageSizeValues() {
        // Given
        String categorySlug = "ao-thun";
        int negativePage = -5;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            productDetailService.filterByCategory(
                    categorySlug, null, null, null, null, null, negativePage, 12
            );
        });
    }

    @Test
    @DisplayName("Nên filter với tất cả tham số được cung cấp")
    void shouldFilterWithAllParametersProvided() {
        // Given
        String categorySlug = "ao-thun";
        String title = "áo thun nam basic";
        List<String> colors = List.of("Red", "Blue");
        List<String> sizes = List.of("M", "L");
        String priceBucket = "1-2m";
        String sortBy = "price_asc";
        int page = 1;
        int pageSize = 20;
        
        BigDecimal expectedMinPrice = BigDecimal.valueOf(1_000_000);
        BigDecimal expectedMaxPrice = BigDecimal.valueOf(2_000_000);
        
        when(productDetailRepository.filterProducts(
                eq(categorySlug),
                eq(title),
                eq(colors),
                eq(sizes),
                eq(expectedMinPrice),
                eq(expectedMaxPrice),
                eq(false),
                eq(false),
                eq("price"),
                eq("asc"),
                eq(PageRequest.of(page, pageSize))
        )).thenReturn(mockPage);

        // When
        PageResult<ProductCardView> result = productDetailService.filterByCategory(
                categorySlug, title, colors, sizes, priceBucket, sortBy, page, pageSize
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        assertThat(result.page()).isEqualTo(0); // từ mock page
        assertThat(result.pageSize()).isEqualTo(12); // từ mock page
    }

    /**
     * Tạo mock ProductCardView để sử dụng trong tests
     */
    private ProductCardView createMockProductCardView() {
        return new ProductCardView() {
            @Override
            public Long getDetailId() { return 1L; }
            
            @Override
            public String getProductTitle() { return "Áo thun nam basic"; }
            
            @Override
            public String getProductSlug() { return "ao-thun-nam-basic"; }
            
            @Override
            public String getColorName() { return "Red"; }
            
            @Override
            public BigDecimal getPrice() { return new BigDecimal("199000"); }
            
            @Override
            public Integer getQuantity() { return 100; }
            
            @Override
            public List<String> getColors() { return List.of("Red", "Blue", "Green"); }
            
            @Override
            public List<String> getImageUrls() { return List.of("image1.jpg", "image2.jpg"); }
        };
    }
}
