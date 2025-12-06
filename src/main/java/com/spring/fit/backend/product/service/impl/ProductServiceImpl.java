package com.spring.fit.backend.product.service.impl;

import com.spring.fit.backend.category.domain.entity.Category;
import com.spring.fit.backend.category.repository.CategoryRepository;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.category.domain.dto.CategoryResponse;
import com.spring.fit.backend.common.model.response.PageResult;
import com.spring.fit.backend.common.service.ImageService;
import com.spring.fit.backend.product.domain.dto.request.*;
import com.spring.fit.backend.product.domain.dto.response.*;
import com.spring.fit.backend.product.domain.entity.*;
import com.spring.fit.backend.product.repository.*;
import com.spring.fit.backend.product.service.ProductService;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionApplyRequest;
import com.spring.fit.backend.promotion.domain.dto.response.PromotionApplyResponse;
import com.spring.fit.backend.promotion.service.PromotionService;
import com.spring.fit.backend.user.service.RecentViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMainRepository productMainRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ImageRepository imageRepository;
    private final ProductImageRepository productImageRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final CategoryRepository categoryRepository;
    private final ImageService imageService;
    private final RecentViewService recentViewService;
    private final PromotionService promotionService;
    
    private static final Map<String, BigDecimal[]> PRICE_BUCKET_MAP = Map.of(
            "<1m", new BigDecimal[]{null, BigDecimal.valueOf(1_000_000)},
            "1-2m", new BigDecimal[]{BigDecimal.valueOf(1_000_000), BigDecimal.valueOf(2_000_000)},
            "2-3m", new BigDecimal[]{BigDecimal.valueOf(2_000_000), BigDecimal.valueOf(3_000_000)},
            ">4m", new BigDecimal[]{BigDecimal.valueOf(4_000_000), null}
    );

    private static final String DEFAULT_SORT_FIELD = "productTitle";
    private static final String DEFAULT_SORT_DIRECTION = "asc";

    @Override
    public PageResult<ProductCardWithPromotionResponse> filterByCategory(
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
            Page<ProductCardView> pageResult = productRepository.filterProducts(
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
            
            // Map thêm giá sau khuyến mãi cho từng SKU
            List<ProductCardWithPromotionResponse> items = pageResult.getContent().stream()
                .map(card -> {
                    var applyRes = PromotionApplyResponse.builder().build();
                    try {
                        var applyReq = PromotionApplyRequest.builder()
                                .skuId(card.getDetailId())
                                .basePrice(card.getPrice())
                                .build();
                        applyRes = promotionService.applyPromotionForSku(applyReq);
                    } catch (Exception ex) {
                        // fallback giữ nguyên giá nếu có lỗi
                        applyRes = PromotionApplyResponse.builder()
                                .basePrice(card.getPrice())
                                .finalPrice(card.getPrice())
                                .percentOff(0)
                                .build();
                    }
                    return ProductCardWithPromotionResponse.builder()
                            .productId(card.getProductId())
                            .detailId(card.getDetailId())
                            .productTitle(card.getProductTitle())
                            .productSlug(card.getProductSlug())
                            .colorName(card.getColorName())
                            .price(card.getPrice())
                            .finalPrice(applyRes.getFinalPrice())
                            .percentOff(applyRes.getPercentOff())
                            .promotionId(applyRes.getPromotionId())
                            .promotionName(applyRes.getPromotionName())
                            .quantity(card.getQuantity())
                            .colors(card.getColors())
                            .imageUrls(card.getImageUrls())
                            .build();
                })
                .toList();

            return new PageResult<>(
                    items,
                    pageResult.getNumber(),
                    pageResult.getSize(),
                    pageResult.getTotalElements(),
                    pageResult.getTotalPages(),
                    pageResult.hasNext(),
                    pageResult.hasPrevious()
            );
            
        } catch (Exception e) {
            log.error("Database query failed for category={}: {}", categorySlug, e.getMessage(), e);
            throw new RuntimeException("Error querying product data", e);
        }
    }

    @Override
    public PageResult<ProductCardWithPromotionResponse> filterDiscounted(
            String title,
            List<String> colorNames,
            List<String> sizeCodes,
            String priceBucket,
            String sortBy,
            int page,
            int pageSize) {

        log.debug("Filtering discounted products: title={}, colors={}, sizes={}, priceBucket={}, sortBy={}, page={}, pageSize={}",
                title, colorNames, sizeCodes, priceBucket, sortBy, page, pageSize);

        // 1) Map price bucket
        BigDecimal[] priceRange = mapPriceBucket(priceBucket);
        BigDecimal min = priceRange[0];
        BigDecimal max = priceRange[1];

        // 2) Parse sort parameters
        SortParams sortParams = parseSortParameters(sortBy);

        // 3) Create pagination
        Pageable pageable = PageRequest.of(page, pageSize);

        // 4) Normalize và sanitize filters
        FilterParams filterParams = normalizeFilters(title, colorNames, sizeCodes);

        // 5) Query database với filter promotion ở repository level
        try {
            Page<ProductCardView> pageResult = productRepository.filterDiscountedProducts(
                    filterParams.title(),
                    filterParams.colors(),
                    filterParams.sizes(),
                    min,
                    max,
                    filterParams.colorsEmpty(),
                    filterParams.sizesEmpty(),
                    sortParams.field(),
                    sortParams.direction(),
                    LocalDateTime.now(),
                    pageable
            );

            log.debug("Query completed: found {} total discounted items, returned {} items",
                    pageResult.getTotalElements(), pageResult.getContent().size());

            // Map thêm thông tin promotion chi tiết cho từng SKU
            List<ProductCardWithPromotionResponse> items = pageResult.getContent().stream()
                    .map(card -> {
                        var applyRes = PromotionApplyResponse.builder().build();
                        try {
                            var applyReq = PromotionApplyRequest.builder()
                                    .skuId(card.getDetailId())
                                    .basePrice(card.getPrice())
                                    .build();
                            applyRes = promotionService.applyPromotionForSku(applyReq);
                        } catch (Exception ex) {
                            // fallback giữ nguyên giá nếu có lỗi
                            log.warn("Error applying promotion for detailId {}: {}", card.getDetailId(), ex.getMessage());
                            applyRes = PromotionApplyResponse.builder()
                                    .basePrice(card.getPrice())
                                    .finalPrice(card.getPrice())
                                    .percentOff(0)
                                    .build();
                        }
                        return ProductCardWithPromotionResponse.builder()
                                .productId(card.getProductId())
                                .detailId(card.getDetailId())
                                .productTitle(card.getProductTitle())
                                .productSlug(card.getProductSlug())
                                .colorName(card.getColorName())
                                .price(card.getPrice())
                                .finalPrice(applyRes.getFinalPrice())
                                .percentOff(applyRes.getPercentOff())
                                .promotionId(applyRes.getPromotionId())
                                .promotionName(applyRes.getPromotionName())
                                .quantity(card.getQuantity())
                                .colors(card.getColors())
                                .imageUrls(card.getImageUrls())
                                .build();
                    })
                    .toList();

            return new PageResult<>(
                    items,
                    pageResult.getNumber(),
                    pageResult.getSize(),
                    pageResult.getTotalElements(),
                    pageResult.getTotalPages(),
                    pageResult.hasNext(),
                    pageResult.hasPrevious()
            );

        } catch (Exception e) {
            log.error("Database query failed for filterDiscounted: {}", e.getMessage(), e);
            throw new RuntimeException("Error when querying promotional product data", e);
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
    public List<ProductCardView> getRecentlyViewedProducts(List<Long> productIds, Long userId) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        
        log.debug("Getting recently viewed products for IDs: {}", productIds);
        
        try {
            // Convert Long to Integer for repository method
            List<Integer> integerIds = productIds.stream()
                    .map(Long::intValue)
                    .toList();
            
            List<ProductCardView> products = productRepository.findRecentlyViewedProduct(integerIds);

            // Lấy ra danh sách detailId đã có trong products
            Set<Long> existingDetailIds = products.stream()
                    .map(ProductCardView::getDetailId)
                    .collect(Collectors.toSet());

            List<Long> duplicateProductColors = productIds.stream()
                    .filter(id -> !existingDetailIds.contains(id))
                    .toList();

            // Chỉ cleanup recent views nếu userId không null
            if (userId != null && !duplicateProductColors.isEmpty()) {
                recentViewService.removeSelected(userId, duplicateProductColors);
            }

            log.debug("Found {} recently viewed products", products.size());
            
            return products;
            
        } catch (Exception e) {
            log.error("Error getting recently viewed products: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting list of recently viewed products", e);
        }
    }

    @Override
    public List<ProductCardWithPromotionResponse> getRecentlyViewedProductsWithPromotion(List<Long> productIds, Long userId) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        
        log.debug("Getting recently viewed products with promotion for IDs: {}", productIds);
        
        try {
            // Convert Long to Integer for repository method
            List<Integer> integerIds = productIds.stream()
                    .map(Long::intValue)
                    .toList();
            
            List<ProductCardView> products = productRepository.findRecentlyViewedProduct(integerIds);

            // Lấy ra danh sách detailId đã có trong products
            Set<Long> existingDetailIds = products.stream()
                    .map(ProductCardView::getDetailId)
                    .collect(Collectors.toSet());

            List<Long> duplicateProductColors = productIds.stream()
                    .filter(id -> !existingDetailIds.contains(id))
                    .toList();

            // Chỉ cleanup recent views nếu userId không null
            if (userId != null && !duplicateProductColors.isEmpty()) {
                recentViewService.removeSelected(userId, duplicateProductColors);
            }

            // Áp dụng promotion cho từng sản phẩm
            List<ProductCardWithPromotionResponse> enrichedProducts = products.stream()
                    .map(card -> {
                        var applyRes = PromotionApplyResponse.builder().build();
                        try {
                            var applyReq = PromotionApplyRequest.builder()
                                    .skuId(card.getDetailId())
                                    .basePrice(card.getPrice())
                                    .build();
                            applyRes = promotionService.applyPromotionForSku(applyReq);
                        } catch (Exception ex) {
                            // fallback giữ nguyên giá nếu có lỗi
                            applyRes = PromotionApplyResponse.builder()
                                    .basePrice(card.getPrice())
                                    .finalPrice(card.getPrice())
                                    .percentOff(0)
                                    .build();
                        }
                        return ProductCardWithPromotionResponse.builder()
                                .productId(card.getProductId())
                                .detailId(card.getDetailId())
                                .productTitle(card.getProductTitle())
                                .productSlug(card.getProductSlug())
                                .colorName(card.getColorName())
                                .price(card.getPrice())
                                .finalPrice(applyRes.getFinalPrice())
                                .percentOff(applyRes.getPercentOff())
                                .promotionId(applyRes.getPromotionId())
                                .promotionName(applyRes.getPromotionName())
                                .quantity(card.getQuantity())
                                .colors(card.getColors())
                                .imageUrls(card.getImageUrls())
                                .build();
                    })
                    .collect(Collectors.toList());

            log.debug("Found {} recently viewed products with promotion", enrichedProducts.size());
            
            return enrichedProducts;
            
        } catch (Exception e) {
            log.error("Error getting recently viewed products with promotion: {}", e.getMessage(), e);
            throw new RuntimeException("Error when getting list of recently viewed products with promotion", e);
        }
    }

    @Override
    public List<ProductCardWithPromotionResponse> getProductsWithPromotionByProductIds(List<Long> productIds, Long userId) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        
        log.debug("Getting products with promotion for productIds: {}", productIds);
        
        try {
            // Lấy danh sách sản phẩm từ productIds (mỗi product lấy một detailId bất kỳ)
            List<ProductCardView> products = productRepository.findProductsByProductIds(productIds);

            // Áp dụng promotion cho từng sản phẩm
            List<ProductCardWithPromotionResponse> enrichedProducts = products.stream()
                    .map(card -> {
                        var applyRes = PromotionApplyResponse.builder().build();
                        try {
                            var applyReq = PromotionApplyRequest.builder()
                                    .skuId(card.getDetailId())
                                    .basePrice(card.getPrice())
                                    .build();
                            applyRes = promotionService.applyPromotionForSku(applyReq);
                        } catch (Exception ex) {
                            // fallback giữ nguyên giá nếu có lỗi
                            applyRes = PromotionApplyResponse.builder()
                                    .basePrice(card.getPrice())
                                    .finalPrice(card.getPrice())
                                    .percentOff(0)
                                    .build();
                        }
                        return ProductCardWithPromotionResponse.builder()
                                .productId(card.getProductId())
                                .detailId(card.getDetailId())
                                .productTitle(card.getProductTitle())
                                .productSlug(card.getProductSlug())
                                .colorName(card.getColorName())
                                .price(card.getPrice())
                                .finalPrice(applyRes.getFinalPrice())
                                .percentOff(applyRes.getPercentOff())
                                .promotionId(applyRes.getPromotionId())
                                .promotionName(applyRes.getPromotionName())
                                .quantity(card.getQuantity())
                                .colors(card.getColors())
                                .imageUrls(card.getImageUrls())
                                .build();
                    })
                    .collect(Collectors.toList());

            log.debug("Found {} products with promotion for productIds", enrichedProducts.size());
            
            return enrichedProducts;
            
        } catch (Exception e) {
            log.error("Error getting products with promotion by productIds: {}", e.getMessage(), e);
            throw new RuntimeException("Error when getting list of products with promotion from ProductIds", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetailById(Long detailId) {
        log.info("Inside ProductServiceImpl.getProductDetailById detailId={}", detailId);
        
        if (detailId == null) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Detail ID cannot be null");
        }

        try {
            // 1. Tìm ProductDetail theo ID
            ProductDetail productDetail = productDetailRepository.findById(detailId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found with ID: " + detailId));
            
            log.debug("Inside ProductServiceImpl.getProductDetailById found detailId={}", productDetail.getId());
            
            // 2. Lấy Product từ ProductDetail
            Product product = productDetail.getProduct();
            if (product == null) {
                throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Product not found for detail ID: " + detailId);
            }
            
            log.info("Inside ProductServiceImpl.getProductDetailById productId={}, title={}", product.getId(), product.getTitle());
            // 3. Lấy activeColor & activeSize từ ProductDetail
            String activeColor = productDetail.getColor().getName();
            String activeSize = productDetail.getSize().getLabel();
            log.info("Inside ProductServiceImpl.getProductDetailById activeColor={}, activeSize={}", activeColor, activeSize);
            // 4. Lấy tất cả colors của product này (native query để tránh lazy load)
            List<String> colorList = productRepository.findAllColorsByDetailId(detailId);
            Set<String> allColors = new LinkedHashSet<>(colorList);
            
            log.info("Inside ProductServiceImpl.getProductDetailById colors={}", allColors);
            // 5. Lấy images của ProductDetail hiện tại (native query)
            List<String> images = productRepository.findImageUrlsByDetailId(detailId);
            
            log.info("Inside ProductServiceImpl.getProductDetailById imagesSize={}", images != null ? images.size() : 0);
            // 6. Lấy mapSizeToQuantity từ ProductDetail của cùng product và color (native query)
            Map<String, Integer> mapSizeToQuantity = productRepository.findSizeQuantityByDetailId(detailId)
                    .stream()
                    .collect(Collectors.toMap(
                            v -> v.getSizeCode(),
                            v -> v.getQuantity() != null ? v.getQuantity() : 0,
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));
            
            log.info("Inside ProductServiceImpl.getProductDetailById mapSizeToQuantityKeys={}", mapSizeToQuantity.keySet());
            // 7. Parse description từ String thành List<String>
            List<String> descriptionList = new ArrayList<>();
            if (StringUtils.hasText(product.getDescription())) {
                // Split by newline hoặc semicolon và trim
                descriptionList = Arrays.stream(product.getDescription().split("[;\n]"))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
            }
            log.info("Inside ProductServiceImpl.getProductDetailById descriptionCount={}", descriptionList.size());
            // 8. Build response
            ProductDetailResponse response = ProductDetailResponse.builder()
                    .detailId(detailId)
                    .title(product.getTitle())
                    .price(productDetail.getPrice())
                    .activeColor(activeColor)
                    .activeSize(activeSize)
                    .images(images)
                    .colors(new ArrayList<>(allColors))
                    .mapSizeToQuantity(mapSizeToQuantity)
                    .description(descriptionList)
                    .build();
            
            log.info("Inside ProductServiceImpl.getProductDetailById success detailId={}", detailId);
            return response;

        } catch (ErrorException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting product detail by ID {}: {}", detailId, e.getMessage(), e);
            throw new RuntimeException("Error getting product details", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailWithPromotionResponse getProductDetailByIdWithPromotion(Long detailId) {
        ProductDetailResponse base = getProductDetailById(detailId);

        ProductDetail productDetail = productDetailRepository.findById(detailId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found with ID: " + detailId));
        Long productId = productDetail.getProduct().getId();
        
        String categorySlug = null;
        Product product = productDetail.getProduct();
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            categorySlug = product.getCategories().iterator().next().getSlug();
        }

        
        var applyRes = PromotionApplyResponse.builder().build();
        try {
            var applyReq = PromotionApplyRequest.builder()
                    .skuId(base.getDetailId())
                    .basePrice(base.getPrice())
                    .build();
            applyRes = promotionService.applyPromotionForSku(applyReq);
        } catch (Exception ex) {
            applyRes = PromotionApplyResponse.builder()
                    .basePrice(base.getPrice())
                    .finalPrice(base.getPrice())
                    .percentOff(0)
                    .build();
        }

        return ProductDetailWithPromotionResponse.builder()
                .detailId(base.getDetailId())
                .productId(productId)
                .title(base.getTitle())
                .price(base.getPrice())
                .finalPrice(applyRes.getFinalPrice())
                .percentOff(applyRes.getPercentOff())
                .promotionId(applyRes.getPromotionId())
                .promotionName(applyRes.getPromotionName())
                .activeColor(base.getActiveColor())
                .activeSize(base.getActiveSize())
                .images(base.getImages())
                .colors(base.getColors())
                .mapSizeToQuantity(base.getMapSizeToQuantity())
                .description(base.getDescription())
                .categorySlug(categorySlug)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetailByColor(Long baseDetailId, String activeColor) {
        log.info("Inside ProductServiceImpl.getProductDetailByColor baseDetailId={}, color={}", baseDetailId, activeColor);
        Long resolvedDetailId = resolveDetailId(baseDetailId, activeColor, null);
        return getProductDetailById(resolvedDetailId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailWithPromotionResponse getProductDetailByColorWithPromotion(Long baseDetailId, String activeColor) {
        Long resolvedDetailId = resolveDetailId(baseDetailId, activeColor, null);
        return getProductDetailByIdWithPromotion(resolvedDetailId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetailByColorAndSize(Long baseDetailId, String activeColor, String activeSize) {
        log.info("Inside getProductDetailByColorAndSize.ProductServiceImpl getProductDetail baseDetailId={}, color={}, size={}", baseDetailId, activeColor, activeSize);
        Long resolvedDetailId = resolveDetailId(baseDetailId, activeColor, activeSize);
        return getProductDetailById(resolvedDetailId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailWithPromotionResponse getProductDetailByColorAndSizeWithPromotion(Long baseDetailId, String activeColor, String activeSize) {
        Long resolvedDetailId = resolveDetailId(baseDetailId, activeColor, activeSize);
        return getProductDetailByIdWithPromotion(resolvedDetailId);
    }

    private Long resolveDetailId(Long baseDetailId, String activeColor, String activeSize) {
        if (baseDetailId == null || !StringUtils.hasText(activeColor)) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "baseDetailId and activeColor are required");
        }
        if (StringUtils.hasText(activeSize)) {
            return productRepository
                    .findDetailIdForColorAndSize(baseDetailId, activeColor, activeSize)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND,
                            "No detail found for color & size: " + activeColor + ", " + activeSize));
        }
        return productRepository
                .findDetailIdForColor(baseDetailId, activeColor)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND,
                        "No detail found for color: " + activeColor));
    }

    // ============ CRUD Operations Implementation ============

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request,
                                         Map<Short, List<MultipartFile>> imagesByColor) {
        log.info("Inside ProductServiceImpl.createProduct title={}", request.getTitle());

        try {
            // 1. Validate categories
            Set<Category> categories = validateAndGetCategories(request.getCategoryIds());

            // 2. Create Product entity
            Product product = new Product();
            product.setTitle(request.getTitle());
            product.setDescription(request.getDescription());
            product.setIsActive(true);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            product.setCategories(categories);

            // 3. Save product first to get ID
            product = productMainRepository.save(product);

            // 4. Create product details and handle images by color
            Set<ProductDetail> productDetails = new HashSet<>();
            Map<Short, List<ProductDetail>> productDetailsByColor = new HashMap<>();
            
            // First pass: Create all ProductDetails and group by color
            for (CreateProductRequest.ColorVariantRequest colorVariant : request.getProductDetails()) {
                Short colorId = colorVariant.getColorId();
                
                // Validate color exists
                Color color = colorRepository.findById(colorId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Color not found with ID: " + colorId));

                List<ProductDetail> colorProductDetails = new ArrayList<>();
                
                // Create product details for each size in this color
                for (CreateProductRequest.SizeVariantRequest sizeVariant : colorVariant.getSizeVariants()) {
                    Short sizeId = sizeVariant.getSizeId();
                    
                    // Validate size exists
                    Size size = sizeRepository.findById(sizeId)
                        .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Size not found with ID: " + sizeId));

                    // Check for duplicate color-size combination
                    Optional<ProductDetail> existingDetail = productDetailRepository
                        .findByProductAndColorAndSize(product.getId(), colorId, sizeId);
                    if (existingDetail.isPresent()) {
                        throw new ErrorException(HttpStatus.CONFLICT, "Product variant already exists with color " + color.getName() + " and size " + size.getCode());
                    }

                    // Create ProductDetail
                    ProductDetail productDetail = new ProductDetail();
                    productDetail.setProduct(product);
                    productDetail.setSlug(generateSlug(product.getTitle(), color.getName(), size.getCode()));
                    productDetail.setColor(color);
                    productDetail.setSize(size);
                    productDetail.setPrice(sizeVariant.getPrice());
                    productDetail.setQuantity(sizeVariant.getQuantity());
                    productDetail.setIsActive(true);
                    productDetail.setCreatedAt(LocalDateTime.now());
                    productDetail.setUpdatedAt(LocalDateTime.now());

                    // Save detail
                    productDetail = productDetailRepository.save(productDetail);
                    colorProductDetails.add(productDetail);
                    productDetails.add(productDetail);
                }
                
                productDetailsByColor.put(colorId, colorProductDetails);
            }

            // Second pass: Handle images for each color
            handleImagesForColors(productDetailsByColor, imagesByColor, product.getTitle());

            product.setDetails(productDetails);

            log.info("Inside ProductServiceImpl.createProduct success productId={}", product.getId());
            return mapToProductResponse(product);

        } catch (Exception e) {
            log.error("Inside ProductServiceImpl.createProduct error title={}, error={}", request.getTitle(), e.getMessage(), e);
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating product: " + e.getMessage());
        }
    }

    /**
     * Handle images for each color - upload once per color and assign to all sizes of that color
     */
    private void handleImagesForColors(Map<Short, List<ProductDetail>> productDetailsByColor, 
                                       Map<Short, List<MultipartFile>> imagesByColor, 
                                       String productTitle) {
        for (Map.Entry<Short, List<ProductDetail>> entry : productDetailsByColor.entrySet()) {
            Short colorId = entry.getKey();
            List<ProductDetail> productDetailsForColor = entry.getValue();
            List<MultipartFile> colorImages = imagesByColor.get(colorId);
            
            if (colorImages != null && !colorImages.isEmpty()) {
                if (colorImages.size() > 5) {
                    throw new ErrorException(HttpStatus.BAD_REQUEST, "Each color variant can only have maximum 5 images");
                }
                
                // Get color name for alt text
                String colorName = productDetailsForColor.get(0).getColor().getName();
                
                // Upload images once for this color
                List<Image> uploadedImages = uploadImagesForColor(colorImages, productTitle, colorName);
                
                // Assign uploaded images to all ProductDetails of this color
                assignImagesToProductDetails(productDetailsForColor, uploadedImages);
            }
        }
    }

    /**
     * Upload images to Cloudinary and create Image entities
     */
    private List<Image> uploadImagesForColor(List<MultipartFile> images, String productTitle, String colorName) {
        List<Image> uploadedImages = new ArrayList<>();
        
        for (MultipartFile imageFile : images) {
            try {
                // Upload image to Cloudinary
                String imageUrl = imageService.uploadImage(imageFile, "products");

                // Create or find Image entity
                Image image = imageRepository.findByUrl(imageUrl)
                    .orElseGet(() -> {
                        Image newImage = new Image();
                        newImage.setUrl(imageUrl);
                        newImage.setAlt(productTitle + " - " + colorName);
                        newImage.setCreatedAt(LocalDateTime.now());
                        return imageRepository.save(newImage);
                    });
                
                uploadedImages.add(image);
                
            } catch (IOException e) {
                log.error("Error uploading image for color {}: {}", colorName, e.getMessage());
                throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error uploading image: " + e.getMessage());
            }
        }
        
        return uploadedImages;
    }

    /**
     * Assign uploaded images to all ProductDetails of a color
     */
    private void assignImagesToProductDetails(List<ProductDetail> productDetails, List<Image> images) {
        for (ProductDetail productDetail : productDetails) {
            for (Image image : images) {
                // Create ProductImage relationship
                ProductImage productImage = new ProductImage();
                productImage.setDetail(productDetail);
                productImage.setImage(image);
                productImage.setCreatedAt(LocalDateTime.now());
                productImageRepository.save(productImage);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.debug("Getting product by ID: {}", id);

        Product product = productMainRepository.findActiveProductByIdWithDetails(id)
            .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        return mapToProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ProductListResponse> getAllProducts(String categorySlug, String title, Boolean isActive,
                                                          String sortBy, String sortDirection, int page, int pageSize) {
        log.debug("Getting all products with filter: categorySlug={}, title={}, isActive={}, sortBy={}, sortDirection={}, page={}, pageSize={}",
                categorySlug, title, isActive, sortBy, sortDirection, page, pageSize);

        try {
            // 1. Parse sort parameters
            SortParams sortParams = parseSortParameters(sortBy + "_" + sortDirection);

            // 2. Create pagination
            Pageable pageable = PageRequest.of(page, pageSize);

            String searchPattern = null;
            if (StringUtils.hasText(title)) {
                searchPattern = "%" + title.trim() + "%";
            }

            // 3. Query database
            Page<Product> productPage = productMainRepository.findAllProductsWithFilter(
                    categorySlug,
                    searchPattern,
                    isActive,
                    sortParams.field(),
                    sortParams.direction(),
                    pageable
            );

            // 4. Map to response
            List<ProductListResponse> responses = productPage.getContent().stream()
                    .map(this::mapToProductListResponse)
                    .collect(Collectors.toList());

            log.debug("Query completed: found {} total items, returned {} items", 
                    productPage.getTotalElements(), responses.size());

            return new PageResult<>(
                    responses,
                    productPage.getNumber(),
                    productPage.getSize(),
                    productPage.getTotalElements(),
                    productPage.getTotalPages(),
                    productPage.hasNext(),
                    productPage.hasPrevious()
            );

        } catch (Exception e) {
            log.error("Database query failed for getAllProducts: {}", e.getMessage(), e);
            throw new RuntimeException("Inside ProductServiceImpl.getAllProducts: Error while querying product list", e);
        }
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product ID: {}", id);

        try {
            // 1. Get existing product
            Product product = productMainRepository.findActiveProductByIdWithDetails(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

            // 2. Validate categories
            Set<Category> categories = validateAndGetCategories(request.getCategoryIds());

            // 3. Update product basic info
            product.setTitle(request.getTitle());
            product.setDescription(request.getDescription());
            product.setCategories(categories);
            product.setUpdatedAt(LocalDateTime.now());

            // 4. Save product
            product = productMainRepository.save(product);

            log.info("Successfully updated product ID: {}", id);
            return mapToProductResponse(product);

        } catch (Exception e) {
            log.error("Error updating product ID {}: {}", id, e.getMessage(), e);
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating product: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ProductResponse.ProductDetailResponse createProductDetail(Long productId, CreateProductDetailRequest request,
                                                                     List<MultipartFile> images) {
        log.info("Creating product detail for product ID: {}", productId);

        try {
            // 1. Get existing product
            Product product = productMainRepository.findActiveProductByIdWithDetails(productId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product not found with ID: " + productId));

            // 2. Validate color and size
            Color color = colorRepository.findById(request.getColorId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Color not found with ID: " + request.getColorId()));
            Size size = sizeRepository.findById(request.getSizeId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Size not found with ID: " + request.getSizeId()));

            // 3. Check for duplicate
            Optional<ProductDetail> existingDetail = productDetailRepository
                    .findByProductAndColorAndSize(productId, request.getColorId(), request.getSizeId());
            if (existingDetail.isPresent()) {
                throw new ErrorException(HttpStatus.CONFLICT,
                        "Product variant already exists with color " + color.getName() + " and size " + size.getCode());
            }

            // 4. Create new detail
            ProductDetail productDetail = new ProductDetail();
            productDetail.setProduct(product);
            productDetail.setSlug(generateSlug(product.getTitle(), color.getName(), size.getCode()));
            productDetail.setColor(color);
            productDetail.setSize(size);
            productDetail.setPrice(request.getPrice());
            productDetail.setQuantity(request.getQuantity());
            productDetail.setIsActive(true);
            productDetail.setCreatedAt(LocalDateTime.now());
            productDetail.setUpdatedAt(LocalDateTime.now());

            productDetail = productDetailRepository.save(productDetail);

            // 5. Handle images
            if (images != null && !images.isEmpty()) {
                if (images.size() > 5) {
                    throw new ErrorException(HttpStatus.BAD_REQUEST, "Each product variant can only have maximum 5 images");
                }
                handleProductDetailImages(productDetail, images);
            }

            log.info("Successfully created product detail with ID: {}", productDetail.getId());
            return mapToProductDetailResponse(productDetail);

        } catch (Exception e) {
            log.error("Error creating product detail for product ID {}: {}", productId, e.getMessage(), e);
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating product detail: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public ProductResponse.ProductDetailResponse updateProductDetail(Long detailId, UpdateProductDetailRequest request) {
        log.info("Updating product detail ID: {}", detailId);

        try {
            // 1. Get existing product detail
            ProductDetail productDetail = productDetailRepository.findById(detailId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found with ID: " + detailId));

            // 2. Validate color and size
            Color color = colorRepository.findById(request.getColorId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Color not found with ID: " + request.getColorId()));
            Size size = sizeRepository.findById(request.getSizeId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Size not found with ID: " + request.getSizeId()));

            // 3. Check for duplicate color-size combination (exclude current detail)
            Optional<ProductDetail> existingDetail = productDetailRepository
                    .findByProductAndColorAndSize(productDetail.getProduct().getId(), request.getColorId(), request.getSizeId());
            if (existingDetail.isPresent() && !existingDetail.get().getId().equals(detailId)) {
                throw new ErrorException(HttpStatus.CONFLICT, "Product variant already exists with color " + color.getName() + " and size " + size.getCode());
            }

            // 4. Update product detail
            productDetail.setColor(color);
            productDetail.setSize(size);
            productDetail.setPrice(request.getPrice());
            productDetail.setQuantity(request.getQuantity());
            productDetail.setSlug(generateSlug(productDetail.getProduct().getTitle(), color.getName(), size.getCode()));
            productDetail.setUpdatedAt(LocalDateTime.now());

            productDetail = productDetailRepository.save(productDetail);

            log.info("Successfully updated product detail ID: {}", detailId);
            return mapToProductDetailResponse(productDetail);

        } catch (Exception e) {
            log.error("Error updating product detail ID {}: {}", detailId, e.getMessage(), e);
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating product detail: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ProductResponse.ProductDetailResponse addProductDetailImages(Long detailId, List<MultipartFile> newImages) {
        log.info("Adding images for product detail ID: {}", detailId);

        try {
            // 1. Get existing product detail
            ProductDetail productDetail = productDetailRepository.findById(detailId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found with ID: " + detailId));

            // 2. Validate new images
            if (newImages == null || newImages.isEmpty()) {
                throw new ErrorException(HttpStatus.BAD_REQUEST, "No images provided");
            }

            // 3. Check total image count limit
            long currentImageCount = productImageRepository.countByDetailId(detailId);
            if (currentImageCount + newImages.size() > 5) {
                throw new ErrorException(HttpStatus.BAD_REQUEST,
                        String.format("Cannot add %d images. Current count: %d, maximum allowed: 5",
                                newImages.size(), currentImageCount));
            }

            // 4. Handle new images
            handleProductDetailImages(productDetail, newImages);

            // 5. Refresh product detail to get updated images
            productDetail = productDetailRepository.findById(detailId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found with ID: " + detailId));

            log.info("Successfully added {} images for product detail ID: {}", newImages.size(), detailId);
            return mapToProductDetailResponse(productDetail);

        } catch (Exception e) {
            log.error("Error adding images for product detail ID {}: {}", detailId, e.getMessage(), e);
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding product detail images: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ProductResponse.ProductDetailResponse deleteProductDetailImages(Long detailId, DeleteProductDetailImagesRequest request) {
        log.info("Deleting images for product detail ID: {}", detailId);

        try {
            // 1. Get existing product detail
            ProductDetail productDetail = productDetailRepository.findById(detailId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found with ID: " + detailId));

            // 2. Validate request
            if (request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
                throw new ErrorException(HttpStatus.BAD_REQUEST, "No image URLs provided for deletion");
            }

            // 3. Delete specified images
            deleteProductImages(productDetail, request.getImageUrls());

            // 4. Refresh product detail to get updated images
            productDetail = productDetailRepository.findById(detailId)
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found with ID: " + detailId));

            log.info("Successfully deleted {} images for product detail ID: {}", request.getImageUrls().size(), detailId);
            return mapToProductDetailResponse(productDetail);

        } catch (Exception e) {
            log.error("Error deleting images for product detail ID {}: {}", detailId, e.getMessage(), e);
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting product detail images: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product ID: {}", id);

        Product product = productMainRepository.findActiveProductById(id)
            .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id));

        // Soft delete - set isActive = false
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());

        // Also soft delete all product details
        if (product.getDetails() != null) {
            product.getDetails().forEach(detail -> {
                detail.setIsActive(false);
                detail.setUpdatedAt(LocalDateTime.now());
            });
        }

        productMainRepository.save(product);

        log.info("Successfully deleted product ID: {}", id);
    }

    @Override
    @Transactional
    public void deleteProductDetail(Long id) {
        log.info("Deleting product detail ID: {}", id);

        ProductDetail product = productDetailRepository.findActiveProductDetailById(id)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found with ID: " + id));

        // Soft delete - set isActive = false
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());

        productDetailRepository.save(product);

        log.info("Successfully deleted product detail ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailByColorAndSizeResponse getProductDetailByColorAndSize(GetProductDetailByColorAndSizeRequest request) {
        log.info("Getting product detail by color and size: productId={}, colorId={}, sizeId={}",
                request.getProductId(), request.getColorId(), request.getSizeId());

        try {
            // 1. Validate và lấy Product
            Product product = productMainRepository.findActiveProductByIdWithDetails(request.getProductId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product not found with ID: " + request.getProductId()));

            // 2. Validate Color
            Color color = colorRepository.findById(request.getColorId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Color not found with ID: " + request.getColorId()));

            // 3. Validate Size
            Size size = sizeRepository.findById(request.getSizeId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Size not found with ID: " + request.getSizeId()));

            // 4. Tìm ProductDetail theo productId, colorId, sizeId
            ProductDetail productDetail = productDetailRepository
                    .findByProductAndColorAndSize(request.getProductId(), request.getColorId(), request.getSizeId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND,
                            "Product variant not found with productId=" + request.getProductId() +
                                    ", colorId=" + request.getColorId() + ", sizeId=" + request.getSizeId()));

            // 5. Kiểm tra ProductDetail có active không
            if (!productDetail.getIsActive()) {
                throw new ErrorException(HttpStatus.NOT_FOUND, "Product variant is not active");
            }

            // 6. Lấy tất cả variant colors của product này (chỉ active)
            List<ColorResponse> variantColors = getVariantColorsForProduct(request.getProductId());

            // 7. Lấy tất cả variant sizes của product này (chỉ active)
            List<SizeResponse> variantSizes = getVariantSizesForProduct(request.getProductId());

            // 8. Lấy images của ProductDetail hiện tại
            List<String> images = productRepository.findImageUrlsByDetailId(productDetail.getId());

            // 9. Build response
            ProductDetailByColorAndSizeResponse response = new ProductDetailByColorAndSizeResponse();
            response.setDetailId(productDetail.getId());
            response.setProductId(request.getProductId());
            response.setTitle(product.getTitle());
            response.setImages(images);
            response.setVariantColors(variantColors);
            response.setActiveColor(mapToColorResponse(color));
            response.setVariantSizes(variantSizes);
            response.setActiveSize(mapToSizeResponse(size));
            response.setQuantity(productDetail.getQuantity());
            response.setPrice(productDetail.getPrice());

            log.info("Successfully retrieved product detail for productId={}, colorId={}, sizeId={}",
                    request.getProductId(), request.getColorId(), request.getSizeId());
            return response;

        } catch (ErrorException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting product detail by color and size: {}", e.getMessage(), e);
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving product detail: " + e.getMessage());
        }
    }
    // ============ Helper Methods ============
    private List<ColorResponse> getVariantColorsForProduct(Long productId) {
        try {
            // Lấy tất cả colors của product này từ product details active
            List<String> colorNames = productRepository.findAllColorsByProductId(productId);

            return colorNames.stream()
                    .map(colorName -> {
                        // Tìm Color entity theo name
                        Color color = colorRepository.findByName(colorName)
                                .orElse(null);
                        return color != null ? mapToColorResponse(color) : null;
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Error getting variant colors for product {}: {}", productId, e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<SizeResponse> getVariantSizesForProduct(Long productId) {
        try {
            // Lấy tất cả sizes của product này từ product details active
            List<String> sizeCodes = productRepository.findAllSizesByProductId(productId);

            return sizeCodes.stream()
                    .map(sizeCode -> {
                        // Tìm Size entity theo code
                        Size size = sizeRepository.findByCode(sizeCode)
                                .orElse(null);
                        return size != null ? mapToSizeResponse(size) : null;
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Error getting variant sizes for product {}: {}", productId, e.getMessage());
            return new ArrayList<>();
        }
    }

    private Set<Category> validateAndGetCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Product must belong to at least one category");
        }

        List<Category> categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new ErrorException(HttpStatus.NOT_FOUND, "One or more categories do not exist");
        }

        return new HashSet<>(categories);
    }

    private void handleProductDetailImages(ProductDetail productDetail, List<MultipartFile> images) {
        for (MultipartFile imageFile : images) {
            try {
                // Upload image to Cloudinary
                String imageUrl = imageService.uploadImage(imageFile, "products");

                // Create or find Image entity
                Image image = imageRepository.findByUrl(imageUrl)
                    .orElseGet(() -> {
                        Image newImage = new Image();
                        newImage.setUrl(imageUrl);
                        newImage.setAlt(productDetail.getProduct().getTitle());
                        newImage.setCreatedAt(LocalDateTime.now());
                        return imageRepository.save(newImage);
                    });

                // Create ProductImage relationship
                ProductImage productImage = new ProductImage();
                productImage.setDetail(productDetail);
                productImage.setImage(image);
                productImage.setCreatedAt(LocalDateTime.now());
                productImageRepository.save(productImage);

            } catch (IOException e) {
                log.error("Error uploading image for product detail {}: {}", productDetail.getId(), e.getMessage());
                throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error uploading image: " + e.getMessage());
            }
        }
    }


    private void deleteProductImages(ProductDetail detail, List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            try {
                List<ProductImage> productImages = productImageRepository.findByImageUrl(imageUrl);
                List<ProductImage> imagesToDelete = productImages.stream()
                        .filter(pi -> pi.getDetail().getId().equals(detail.getId()))
                        .toList();

                if (imagesToDelete.isEmpty()) {
                    log.warn("No images found with URL {} for product detail {}", imageUrl, detail.getId());
                    continue;
                }

                productImageRepository.deleteAll(imagesToDelete);
                log.debug("Deleted {} images with URL {} for product detail {}", imagesToDelete.size(), imageUrl, detail.getId());

            } catch (Exception e) {
                log.error("Error deleting image with URL {} for product detail {}: {}", imageUrl, detail.getId(), e.getMessage());
                throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting image: " + e.getMessage());
            }
        }
    }

     String generateSlug(String productTitle, String colorName, String sizeCode) {
        String base = productTitle + "-" + colorName + "-" + sizeCode;
        String slug = toSlug(base);

        // Add timestamp to ensure uniqueness
        return slug + "-" + System.currentTimeMillis();
    }

    private String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String withoutAccents = pattern.matcher(normalized).replaceAll("");
        return withoutAccents.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }

    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setTitle(product.getTitle());
        response.setDescription(product.getDescription());
        response.setIsActive(product.getIsActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        // Map categories
        if (product.getCategories() != null) {
            response.setCategories(product.getCategories().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toSet()));
        }

        // Map product details
        if (product.getDetails() != null) {
            response.setProductDetails(product.getDetails().stream()
                .filter(ProductDetail::getIsActive)
                .map(this::mapToProductDetailResponse)
                .collect(Collectors.toList()));
        }

        return response;
    }

    private ProductResponse.ProductDetailResponse mapToProductDetailResponse(ProductDetail detail) {
        ProductResponse.ProductDetailResponse response = new ProductResponse.ProductDetailResponse();
        response.setId(detail.getId());
        response.setSlug(detail.getSlug());
        response.setPrice(detail.getPrice());
        response.setQuantity(detail.getQuantity());
        response.setIsActive(detail.getIsActive());
        response.setCreatedAt(detail.getCreatedAt());
        response.setUpdatedAt(detail.getUpdatedAt());

        // Map color
        if (detail.getColor() != null) {
            ProductResponse.ColorResponse colorResponse = new ProductResponse.ColorResponse();
            colorResponse.setId(detail.getColor().getId());
            colorResponse.setName(detail.getColor().getName());
            colorResponse.setHex(detail.getColor().getHex());
            response.setColor(colorResponse);
        }

        // Map size
        if (detail.getSize() != null) {
            ProductResponse.SizeResponse sizeResponse = new ProductResponse.SizeResponse();
            sizeResponse.setId(detail.getSize().getId());
            sizeResponse.setCode(detail.getSize().getCode());
            sizeResponse.setLabel(detail.getSize().getLabel());
            response.setSize(sizeResponse);
        }

        // Map images
        List<ProductImage> productImages = productImageRepository.findByDetailIdOrderByCreatedAt(detail.getId());
        response.setImageUrls(productImages.stream()
            .map(pi -> pi.getImage().getUrl())
            .collect(Collectors.toList()));

        return response;
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
//        response.setDescription(category.getDescription());
        response.setIsActive(category.getIsActive());
        return response;
    }

    private ProductListResponse mapToProductListResponse(Product product) {
        ProductListResponse response = new ProductListResponse();
        response.setId(product.getId());
        response.setTitle(product.getTitle());
        response.setDescription(product.getDescription());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        // Map category (lấy category đầu tiên)
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            response.setCategoryId(product.getCategories().iterator().next().getId());
        }

        // Lấy thumbnail (ảnh đầu tiên của 1 màu bất kì) và detail ID tương ứng
        String thumbnail = getThumbnailForProduct(product.getId());
        response.setThumbnail(thumbnail);
        
        Long currentDetailId = productRepository.findFirstDetailIdByProductId(product.getId());
        response.setCurrentDetailId(currentDetailId);

        // Lấy danh sách màu active và inactive
        List<ColorResponse> variantColors = new ArrayList<>();
        List<SizeResponse> variantSizes = new ArrayList<>();

        if (product.getDetails() != null) {
            Map<Short, ColorResponse> colorMap = new HashMap<>();
            Map<Short, SizeResponse> sizeMap = new HashMap<>();

            for (ProductDetail detail : product.getDetails()) {
                // Chỉ lấy các detail active
                if (detail.getIsActive()) {
                    // Lấy màu
                    Color color = detail.getColor();
                    if (!colorMap.containsKey(color.getId())) {
                        colorMap.put(color.getId(), mapToColorResponse(color));
                    }

                    // Lấy size
                    Size size = detail.getSize();
                    if (!sizeMap.containsKey(size.getId())) {
                        sizeMap.put(size.getId(), mapToSizeResponse(size));
                    }
                }
            }

            variantColors.addAll(colorMap.values());
            variantSizes.addAll(sizeMap.values());
        }

        response.setVariantColors(variantColors);
        response.setVariantSizes(variantSizes);

        return response;
    }
    private SizeResponse mapToSizeResponse(Size size) {
        SizeResponse response = new SizeResponse();
        response.setId(size.getId());
        response.setCode(size.getCode());
        response.setLabel(size.getLabel());
        return response;
    }

    private ColorResponse mapToColorResponse(Color color) {
        ColorResponse response = new ColorResponse();
        response.setId(color.getId());
        response.setName(color.getName());
        response.setHex(color.getHex());
        return response;
    }

    private String getThumbnailForProduct(Long productId) {
        try {
            // Lấy ảnh đầu tiên của product detail đầu tiên
            List<String> imageUrls = productRepository.findFirstImageUrlByProductId(productId);
            return imageUrls.isEmpty() ? null : imageUrls.get(0);
        } catch (Exception e) {
            log.warn("Error getting thumbnail for product {}: {}", productId, e.getMessage());
            return null;
        }
    }

    // Records cho better type safety và immutability
    private record SortParams(String field, String direction) {}
    private record FilterParams(String title, List<String> colors, List<String> sizes, boolean colorsEmpty, boolean sizesEmpty) {}

    @Override
    @Transactional(readOnly = true)
    public List<ProductSimpleResponse> getAllProductsSimple() {
        log.info("Getting all products simple");
        List<Product> products = productMainRepository.findAll();
        return products.stream()
                .map(p -> ProductSimpleResponse.builder()
                        .id(p.getId())
                        .name(p.getTitle())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetailSimpleResponse> getAllProductDetailsSimple() {
        log.info("Getting all product details simple");
        List<ProductDetail> details = productDetailRepository.findAll();
        return details.stream()
                .map(d -> {
                    String name = d.getProduct().getTitle() + " - " + 
                                  d.getColor().getName() + " - " + 
                                  d.getSize().getLabel();
                    return ProductDetailSimpleResponse.builder()
                            .id(d.getId())
                            .name(name)
                            .build();
                })
                .toList();
    }
}
