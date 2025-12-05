package com.spring.fit.backend.chatbot.service;

import com.spring.fit.backend.chatbot.constants.ChatbotConstants;
import com.spring.fit.backend.chatbot.domain.dto.ChatbotResponse;
import com.spring.fit.backend.category.domain.entity.Category;
import com.spring.fit.backend.category.repository.CategoryRepository;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductRecommendationServiceImpl implements ProductRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(ProductRecommendationServiceImpl.class);

    private final VectorStore vectorStore;
    private final ProductDetailRepository productDetailRepository;
    private final CategoryRepository categoryRepository;

    // Keywords that indicate product-related queries
    private static final String[] PRODUCT_QUERY_KEYWORDS = {
        "bán", "có", "mua", "sản phẩm", "hàng", "đồ", "áo", "quần", 
        "túi", "giày", "mũ", "nón", "phụ kiện", "thời trang"
    };
    
    // Keywords that indicate non-product queries (greetings, general questions)
    private static final String[] NON_PRODUCT_KEYWORDS = {
        "xin chào", "hello", "hi", "cảm ơn", "thanks", "tạm biệt", "bye",
        "giờ", "thời gian", "địa chỉ", "address", "liên hệ", "contact"
    };

    public ProductRecommendationServiceImpl(
            VectorStore vectorStore,
            ProductDetailRepository productDetailRepository,
            CategoryRepository categoryRepository) {
        this.vectorStore = vectorStore;
        this.productDetailRepository = productDetailRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Set<Long> extractProductIdsFromQuery(String query) {
        try {
            // Check if query is about outfit/styling
            String queryLower = query.toLowerCase();
            boolean isOutfitQuery = false;
            for (String keyword : ChatbotConstants.OUTFIT_QUERY_KEYWORDS) {
                if (queryLower.contains(keyword)) {
                    isOutfitQuery = true;
                    break;
                }
            }

            Set<Long> productIds = new HashSet<>();
            
            if (isOutfitQuery) {
                // For outfit queries, search for diverse categories
                // Search with the original query to get general results
                List<Document> documents = vectorStore.similaritySearch(query);
                
                // Also search for specific category types to ensure diversity
                String[] categoryQueries = {
                    query + " áo sơ mi polo hoodie",
                    query + " quần jogger shorts",
                    query + " túi tote đeo vai đeo chéo",
                    query + " giày dép sneakers",
                    query + " mũ nón phụ kiện"
                };
                
                // Collect all documents from multiple searches
                Set<Document> allDocuments = new HashSet<>(documents);
                for (String categoryQuery : categoryQueries) {
                    try {
                        List<Document> categoryDocs = vectorStore.similaritySearch(categoryQuery);
                        allDocuments.addAll(categoryDocs);
                    } catch (Exception e) {
                        log.warn("Error searching for category query: {}", categoryQuery, e);
                    }
                }
                
                // Group products by category type to ensure diversity
                Map<String, List<Long>> productsByCategory = new HashMap<>();
                Map<Long, String> productCategoryMap = new HashMap<>();
                
                for (Document doc : allDocuments) {
                    Map<String, Object> metadata = doc.getMetadata();
                    String type = (String) metadata.get(ChatbotConstants.METADATA_TYPE);
                    
                    if (ChatbotConstants.DOC_TYPE_PRODUCT.equals(type) || 
                        ChatbotConstants.DOC_TYPE_PRODUCT_DETAIL.equals(type)) {
                        String productIdStr = (String) metadata.get(ChatbotConstants.METADATA_PRODUCT_ID);
                        if (productIdStr != null) {
                            try {
                                Long productId = Long.parseLong(productIdStr);
                                String parentCategories = (String) metadata.get(ChatbotConstants.METADATA_PARENT_CATEGORIES);
                                
                                if (parentCategories != null && !parentCategories.isEmpty()) {
                                    String[] parentCats = parentCategories.split(ChatbotConstants.DELIMITER_COMMA_SPACE);
                                    for (String cat : parentCats) {
                                        String catLower = cat.toLowerCase();
                                        productsByCategory.computeIfAbsent(catLower, k -> new ArrayList<>()).add(productId);
                                        productCategoryMap.put(productId, catLower);
                                    }
                                } else {
                                    // If no parent category, use "other"
                                    productsByCategory.computeIfAbsent("other", k -> new ArrayList<>()).add(productId);
                                    productCategoryMap.put(productId, "other");
                                }
                            } catch (NumberFormatException e) {
                                log.warn("Invalid productId in metadata: {}", productIdStr);
                            }
                        }
                    }
                }
                
                // Select products ensuring diversity: at least 1 from each major category
                Set<String> targetCategories = new HashSet<>(Arrays.asList(
                    "áo", "quần", "túi ví", "giày dép", "mũ nón"
                ));
                
                // First, get at least 1 product from each target category
                for (String targetCat : targetCategories) {
                    for (Map.Entry<String, List<Long>> entry : productsByCategory.entrySet()) {
                        String category = entry.getKey();
                        if (category.contains(targetCat) || targetCat.contains(category)) {
                            List<Long> products = entry.getValue();
                            if (!products.isEmpty()) {
                                productIds.add(products.get(0));
                                break;
                            }
                        }
                    }
                }
                
                // Then, fill up to MAX_PRODUCTS_FOR_OUTFIT with diverse products
                for (Map.Entry<String, List<Long>> entry : productsByCategory.entrySet()) {
                    if (productIds.size() >= ChatbotConstants.MAX_PRODUCTS_FOR_OUTFIT) {
                        break;
                    }
                    List<Long> products = entry.getValue();
                    for (Long productId : products) {
                        if (productIds.size() >= ChatbotConstants.MAX_PRODUCTS_FOR_OUTFIT) {
                            break;
                        }
                        productIds.add(productId);
                    }
                }
                
            } else {
                // For regular queries, just do normal search
                List<Document> documents = vectorStore.similaritySearch(query);
                
                for (Document doc : documents) {
                    Map<String, Object> metadata = doc.getMetadata();
                    String type = (String) metadata.get(ChatbotConstants.METADATA_TYPE);
                    
                    if (ChatbotConstants.DOC_TYPE_PRODUCT.equals(type) || 
                        ChatbotConstants.DOC_TYPE_PRODUCT_DETAIL.equals(type)) {
                        String productIdStr = (String) metadata.get(ChatbotConstants.METADATA_PRODUCT_ID);
                        if (productIdStr != null) {
                            try {
                                productIds.add(Long.parseLong(productIdStr));
                            } catch (NumberFormatException e) {
                                log.warn("Invalid productId in metadata: {}", productIdStr);
                            }
                        }
                    }
                }
            }
            
            log.debug("Extracted {} product IDs from query: {} (outfit query: {})", 
                    productIds.size(), productIds, isOutfitQuery);
            return productIds;
            
        } catch (Exception e) {
            log.error("Error extracting product IDs from query", e);
            return Collections.emptySet();
        }
    }

    @Override
    public List<ChatbotResponse.ProductRecommendation> getProductRecommendations(Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // Get ProductDetails with their category information
            Map<Long, ProductDetail> productDetailMap = new HashMap<>();
            Map<Long, String> productCategoryMap = new HashMap<>();
            
            for (Long productId : productIds) {
                List<ProductDetail> details = productDetailRepository.findActiveDetailsByProductIdWithProduct(productId);
                if (!details.isEmpty()) {
                    ProductDetail detail = details.get(0);
                    productDetailMap.put(productId, detail);
                    
                    // Extract category information
                    if (detail.getProduct() != null && detail.getProduct().getCategories() != null) {
                        String parentCategories = detail.getProduct().getCategories().stream()
                                .filter(cat -> cat.getIsActive() != null && cat.getIsActive())
                                .filter(cat -> cat.getParent() != null)
                                .map(cat -> cat.getParent().getName().toLowerCase())
                                .distinct()
                                .collect(Collectors.joining(ChatbotConstants.DELIMITER_COMMA_SPACE));
                        if (!parentCategories.isEmpty()) {
                            productCategoryMap.put(productId, parentCategories);
                        }
                    }
                }
            }

            // Group products by category to ensure diversity
            Map<String, List<ProductDetail>> detailsByCategory = new HashMap<>();
            List<ProductDetail> uncategorized = new ArrayList<>();
            
            for (Map.Entry<Long, ProductDetail> entry : productDetailMap.entrySet()) {
                Long productId = entry.getKey();
                ProductDetail detail = entry.getValue();
                String categories = productCategoryMap.get(productId);
                
                if (categories != null && !categories.isEmpty()) {
                    String[] categoryArray = categories.split(ChatbotConstants.DELIMITER_COMMA_SPACE);
                    boolean categorized = false;
                    for (String cat : categoryArray) {
                        String catLower = cat.toLowerCase();
                        // Map to major categories
                        String majorCategory = mapToMajorCategory(catLower);
                        detailsByCategory.computeIfAbsent(majorCategory, k -> new ArrayList<>()).add(detail);
                        categorized = true;
                    }
                    if (!categorized) {
                        uncategorized.add(detail);
                    }
                } else {
                    uncategorized.add(detail);
                }
            }
            
            // Build final list ensuring diversity
            List<ProductDetail> finalDetails = new ArrayList<>();
            Set<String> usedCategories = new HashSet<>();
            
            // First pass: get at least 1 from each major category
            String[] majorCategories = {"áo", "quần", "túi ví", "giày dép", "mũ nón"};
            for (String majorCat : majorCategories) {
                for (Map.Entry<String, List<ProductDetail>> entry : detailsByCategory.entrySet()) {
                    if (entry.getKey().contains(majorCat) || majorCat.contains(entry.getKey())) {
                        List<ProductDetail> details = entry.getValue();
                        if (!details.isEmpty() && !usedCategories.contains(entry.getKey())) {
                            finalDetails.add(details.get(0));
                            usedCategories.add(entry.getKey());
                            break;
                        }
                    }
                }
            }
            
            // Second pass: fill remaining slots with diverse products
            for (Map.Entry<String, List<ProductDetail>> entry : detailsByCategory.entrySet()) {
                if (finalDetails.size() >= ChatbotConstants.RECOMMENDATION_LIMIT_OUTFIT) {
                    break;
                }
                if (!usedCategories.contains(entry.getKey())) {
                    List<ProductDetail> details = entry.getValue();
                    if (!details.isEmpty()) {
                        finalDetails.add(details.get(0));
                        usedCategories.add(entry.getKey());
                    }
                }
            }
            
            // Third pass: add remaining products if we still have space
            for (ProductDetail detail : productDetailMap.values()) {
                if (finalDetails.size() >= ChatbotConstants.RECOMMENDATION_LIMIT_OUTFIT) {
                    break;
                }
                if (!finalDetails.contains(detail)) {
                    finalDetails.add(detail);
                }
            }
            
            // Add uncategorized products
            for (ProductDetail detail : uncategorized) {
                if (finalDetails.size() >= ChatbotConstants.RECOMMENDATION_LIMIT_OUTFIT) {
                    break;
                }
                if (!finalDetails.contains(detail)) {
                    finalDetails.add(detail);
                }
            }
            
            // Limit to max recommendations
            finalDetails = finalDetails.stream()
                    .limit(ChatbotConstants.RECOMMENDATION_LIMIT_OUTFIT)
                    .collect(Collectors.toList());

            // Map to recommendations
            return finalDetails.stream()
                    .map(this::mapToRecommendation)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting product recommendations", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Map category name to major category for grouping
     */
    private String mapToMajorCategory(String category) {
        String catLower = category.toLowerCase();
        if (catLower.contains("áo") || catLower.contains("ao")) {
            return "áo";
        } else if (catLower.contains("quần") || catLower.contains("quan")) {
            return "quần";
        } else if (catLower.contains("túi") || catLower.contains("tui") || catLower.contains("ví") || catLower.contains("vi")) {
            return "túi ví";
        } else if (catLower.contains("giày") || catLower.contains("giay") || catLower.contains("dép") || catLower.contains("dep")) {
            return "giày dép";
        } else if (catLower.contains("mũ") || catLower.contains("mu") || catLower.contains("nón") || catLower.contains("non")) {
            return "mũ nón";
        }
        return category;
    }

    private ChatbotResponse.ProductRecommendation mapToRecommendation(ProductDetail detail) {
        ChatbotResponse.ProductRecommendation recommendation = new ChatbotResponse.ProductRecommendation();
        
        // objectId = productDetailId
        recommendation.setObjectId(detail.getId());
        
        // title = slug của productDetail (sẽ được thay thế bởi GPT response nếu có)
        recommendation.setTitle(detail.getSlug());
        
        // description = description của product
        if (detail.getProduct() != null) {
            recommendation.setDescription(
                    detail.getProduct().getDescription() != null 
                            ? detail.getProduct().getDescription() 
                            : ""
            );
        } else {
            recommendation.setDescription("");
        }
        
        // imageUrl = first image của productDetail (using native query)
        String imageUrl = "";
        try {
            Optional<String> imageUrlOpt = productDetailRepository.findFirstImageUrlByDetailId(detail.getId());
            if (imageUrlOpt.isPresent()) {
                imageUrl = imageUrlOpt.get();
            }
        } catch (Exception e) {
            log.warn("Error getting image for productDetail {}: {}", detail.getId(), e.getMessage());
        }
        recommendation.setImageUrl(imageUrl);
        
        // color = tên màu từ database
        if (detail.getColor() != null) {
            recommendation.setColor(detail.getColor().getName());
        } else {
            recommendation.setColor("");
        }
        
        // size = label của size từ database
        if (detail.getSize() != null) {
            recommendation.setSize(detail.getSize().getLabel());
        } else {
            recommendation.setSize("");
        }
        
        // price = giá từ database
        recommendation.setPrice(detail.getPrice());
        
        // quantity = số lượng từ database
        recommendation.setQuantity(detail.getQuantity());
        
        return recommendation;
    }
    
    @Override
    public boolean isProductRelatedQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        
        String queryLower = query.toLowerCase();
        
        // Check for non-product keywords first
        for (String keyword : NON_PRODUCT_KEYWORDS) {
            if (queryLower.contains(keyword)) {
                return false;
            }
        }
        
        // Check for product-related keywords
        for (String keyword : PRODUCT_QUERY_KEYWORDS) {
            if (queryLower.contains(keyword)) {
                return true;
            }
        }
        
        // If query contains question words about products, consider it product-related
        if (queryLower.matches(".*(có|bán|mua|tìm|giá|màu|size|kích thước).*")) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean checkProductsRelevance(String query, Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return false;
        }
        
        String queryLower = query.toLowerCase();
        
        // Extract key terms from query
        Set<String> queryTerms = extractKeyTerms(queryLower);
        
        // Get product details and check if they match query terms
        for (Long productId : productIds) {
            List<ProductDetail> details = productDetailRepository.findActiveDetailsByProductIdWithProduct(productId);
            if (!details.isEmpty()) {
                ProductDetail detail = details.get(0);
                if (detail.getProduct() != null) {
                    String productTitle = detail.getProduct().getTitle().toLowerCase();
                    String productDescription = detail.getProduct().getDescription() != null 
                            ? detail.getProduct().getDescription().toLowerCase() 
                            : "";
                    
                    // Check if product title or description contains query terms
                    boolean hasMatch = queryTerms.stream().anyMatch(term -> 
                        productTitle.contains(term) || productDescription.contains(term)
                    );
                    
                    // Also check categories
                    if (!hasMatch && detail.getProduct().getCategories() != null) {
                        hasMatch = detail.getProduct().getCategories().stream()
                            .anyMatch(cat -> {
                                String catName = cat.getName().toLowerCase();
                                return queryTerms.stream().anyMatch(term -> catName.contains(term));
                            });
                    }
                    
                    if (hasMatch) {
                        return true; // At least one product is relevant
                    }
                }
            }
        }
        
        // If no products match query terms, they're not relevant
        return false;
    }
    
    /**
     * Extract key terms from query (remove common words)
     */
    private Set<String> extractKeyTerms(String query) {
        Set<String> stopWords = Set.of(
            "có", "bán", "mua", "tìm", "giá", "màu", "size", "kích thước",
            "shop", "cửa hàng", "cho", "tôi", "bạn", "mình", "em", "anh",
            "không", "gì", "nào", "đó", "này", "đây", "đó", "và", "hoặc"
        );
        
        String[] words = query.split("\\s+");
        Set<String> terms = new HashSet<>();
        
        for (String word : words) {
            word = word.trim().toLowerCase();
            if (word.length() > 1 && !stopWords.contains(word)) {
                terms.add(word);
            }
        }
        
        return terms;
    }
    
    @Override
    public List<String> getAvailableProductCategories() {
        try {
            // Get all active parent categories (categories with no parent)
            List<Category> parentCategories = categoryRepository.findByParentIsNull();
            
            return parentCategories.stream()
                    .filter(cat -> cat.getIsActive() != null && cat.getIsActive())
                    .map(Category::getName)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting available categories: {}", e.getMessage(), e);
            // Return default categories if error
            return Arrays.asList(
                "Áo thun", "Áo sơ mi", "Áo polo", "Áo hoodie",
                "Quần short", "Quần jogger",
                "Túi đeo chéo", "Túi tote", "Túi đeo vai",
                "Mũ bóng chày", "Mũ bucket",
                "Phụ kiện thời trang"
            );
        }
    }
}

