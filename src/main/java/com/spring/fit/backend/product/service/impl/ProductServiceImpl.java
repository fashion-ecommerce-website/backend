package com.spring.fit.backend.product.service.impl;

import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.product.domain.dto.ProductCardView;
import com.spring.fit.backend.product.repository.ProductRepository;
import com.spring.fit.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productDetailRepository;
    
    private static final Map<String, BigDecimal[]> PRICE_BUCKET_MAP = Map.of(
            "<1m", new BigDecimal[]{null, BigDecimal.valueOf(1_000_000)},
            "1-2m", new BigDecimal[]{BigDecimal.valueOf(1_000_000), BigDecimal.valueOf(2_000_000)},
            "2-3m", new BigDecimal[]{BigDecimal.valueOf(2_000_000), BigDecimal.valueOf(3_000_000)},
            ">4m", new BigDecimal[]{BigDecimal.valueOf(4_000_000), null}
    );

    private static final String DEFAULT_SORT_FIELD = "productTitle";
    private static final String DEFAULT_SORT_DIRECTION = "asc";

    @Override
    public PageResult<ProductCardView> filterByCategory(
            String categorySlug,
            String title,
            List<String> colorNames,
            List<String> sizeCodes,
            String priceBucket,
            String sortBy,
            int page,
            int pageSize) {
        
        log.debug("Filtering products: category={}, title={}, colors={}, sizes={}, priceBucket={}, sortBy={}, page={}, pageSize={}", 
                categorySlug, title, colorNames, sizeCodes, priceBucket, sortBy, page, pageSize);
        
        // 1) Map price bucket với performance optimization
        BigDecimal[] priceRange = mapPriceBucket(priceBucket);
        BigDecimal min = priceRange[0];
        BigDecimal max = priceRange[1];

        // 2) Parse sort parameters
        SortParams sortParams = parseSortParameters(sortBy);

        // 3) Create pagination
        Pageable pageable = PageRequest.of(page, pageSize);

        // 4) Normalize và sanitize filters
        FilterParams filterParams = normalizeFilters(title, colorNames, sizeCodes);

        // 5) Query database
        try {
            Page<ProductCardView> pageResult = productDetailRepository.filterProducts(
                    categorySlug, 
                    filterParams.title(), 
                    filterParams.colors(), 
                    filterParams.sizes(), 
                    min, 
                    max, 
                    filterParams.colorsEmpty(), 
                    filterParams.sizesEmpty(), 
                    sortParams.field(), 
                    sortParams.direction(), 
                    pageable
            );
            
            log.debug("Query completed: found {} total items, returned {} items", 
                    pageResult.getTotalElements(), pageResult.getContent().size());
            
            return PageResult.from(pageResult);
            
        } catch (Exception e) {
            log.error("Database query failed for category={}: {}", categorySlug, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi truy vấn dữ liệu sản phẩm", e);
        }
    }
    
    
    private BigDecimal[] mapPriceBucket(String priceBucket) {
        if (!StringUtils.hasText(priceBucket)) {
            return new BigDecimal[]{null, null};
        }
        
        BigDecimal[] range = PRICE_BUCKET_MAP.get(priceBucket);
        if (range == null) {
            log.warn("Invalid price bucket: {}, ignoring", priceBucket);
            return new BigDecimal[]{null, null};
        }
        
        return range;
    }
    
    private SortParams parseSortParameters(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return new SortParams(DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION);
        }
        
        String[] sortCriteria = sortBy.split("_", 2);
        String sortField = sortCriteria[0].trim();
        String sortDirection = sortCriteria[1].trim().toLowerCase();

        return new SortParams(sortField, sortDirection);
    }

    
    private FilterParams normalizeFilters(String title, List<String> colorNames, List<String> sizeCodes) {
        String normalizedTitle = StringUtils.hasText(title) ? title.trim() : null;
        
        List<String> normalizedColors = normalizeStringList(colorNames);
        boolean colorsEmpty = normalizedColors == null || normalizedColors.isEmpty();
        
        List<String> normalizedSizes = normalizeStringList(sizeCodes);
        boolean sizesEmpty = normalizedSizes == null || normalizedSizes.isEmpty();
        
        return new FilterParams(
            normalizedTitle,
            colorsEmpty ? null : normalizedColors,
            sizesEmpty ? null : normalizedSizes,
            colorsEmpty,
            sizesEmpty
        );
    }
    
    private List<String> normalizeStringList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        
        return list.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductCardView> getRecentlyViewedProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        
        log.debug("Getting recently viewed products for IDs: {}", productIds);
        
        try {
            // Convert Long to Integer for repository method
            List<Integer> integerIds = productIds.stream()
                    .map(Long::intValue)
                    .toList();
            
            List<ProductCardView> products = productDetailRepository.findRecentlyViewedProduct(integerIds);
            log.debug("Found {} recently viewed products", products.size());
            
            return products;
            
        } catch (Exception e) {
            log.error("Error getting recently viewed products: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lấy danh sách sản phẩm đã xem gần đây", e);
        }
    }
    
    // Records cho better type safety và immutability
    private record SortParams(String field, String direction) {}
    private record FilterParams(String title, List<String> colors, List<String> sizes, boolean colorsEmpty, boolean sizesEmpty) {}
}
