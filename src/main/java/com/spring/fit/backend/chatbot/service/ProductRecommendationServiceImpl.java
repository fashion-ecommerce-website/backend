package com.spring.fit.backend.chatbot.service;

import com.spring.fit.backend.chatbot.constants.ChatbotConstants;
import com.spring.fit.backend.chatbot.domain.dto.ChatbotResponse;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class ProductRecommendationServiceImpl implements ProductRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(ProductRecommendationServiceImpl.class);

    private final VectorStore vectorStore;
    private final ProductDetailRepository productDetailRepository;
    private final QueryIntentService queryIntentService;

    public ProductRecommendationServiceImpl(
            VectorStore vectorStore,
            ProductDetailRepository productDetailRepository,
            QueryIntentService queryIntentService) {
        this.vectorStore = vectorStore;
        this.productDetailRepository = productDetailRepository;
        this.queryIntentService = queryIntentService;
    }

    @Override
    public Set<Long> extractProductIdsFromQuery(String query) {
        try {
            // Use GPT to understand query intent
            boolean isOutfitQuery = queryIntentService.isOutfitQuery(query);
            
            Set<Long> productIds;
            
            if (isOutfitQuery) {
                // Generate diverse search queries based on context
                List<String> searchQueries = queryIntentService.generateOutfitSearchQueries(query);
                
                // Collect all documents from multiple searches
                Set<Document> allDocuments = new HashSet<>();
                for (String searchQuery : searchQueries) {
                    try {
                        List<Document> docs = vectorStore.similaritySearch(searchQuery);
                        allDocuments.addAll(docs);
                    } catch (Exception e) {
                        log.warn("Error searching with query: {}", searchQuery, e);
                    }
                }
                
                // Extract productIds and group by category
                Map<String, List<Long>> productsByCategory = groupProductsByCategory(allDocuments);
                
                // Filter products by season if mentioned in query
                productsByCategory = filterProductsBySeason(productsByCategory, query, allDocuments);
                
                // Get target categories from query context
                List<String> targetCategories = queryIntentService.extractTargetCategories(query);
                
                // Select diverse products ensuring at least 1 from each target category
                productIds = selectDiverseProducts(productsByCategory, targetCategories);
                
            } else {
                // For regular queries, do simple similarity search
                List<Document> documents = vectorStore.similaritySearch(query);
                productIds = extractProductIdsFromDocuments(documents);
            }
            
            log.debug("Extracted {} product IDs from query: {} (outfit query: {})", 
                    productIds.size(), productIds, isOutfitQuery);
            return productIds;
            
        } catch (Exception e) {
            log.error("Error extracting product IDs from query", e);
            return Collections.emptySet();
        }
    }

    /**
     * Group products by category from documents
     */
    private Map<String, List<Long>> groupProductsByCategory(Set<Document> documents) {
        Map<String, List<Long>> productsByCategory = new HashMap<>();
        
        for (Document doc : documents) {
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
                                String catLower = cat.toLowerCase().trim();
                                productsByCategory.computeIfAbsent(catLower, k -> new ArrayList<>()).add(productId);
                            }
                        } else {
                            productsByCategory.computeIfAbsent("other", k -> new ArrayList<>()).add(productId);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Invalid productId in metadata: {}", productIdStr);
                    }
                }
            }
        }
        
        return productsByCategory;
    }

    /**
     * Select diverse products ensuring at least 1 from each target category
     */
    private Set<Long> selectDiverseProducts(Map<String, List<Long>> productsByCategory, List<String> targetCategories) {
        Set<Long> selectedProductIds = new HashSet<>();
        Set<String> usedCategories = new HashSet<>();
        
        // First pass: get at least 1 product from each target category
        for (String targetCat : targetCategories) {
            String targetCatLower = targetCat.toLowerCase();
            for (Map.Entry<String, List<Long>> entry : productsByCategory.entrySet()) {
                String category = entry.getKey();
                if (category.contains(targetCatLower) || targetCatLower.contains(category)) {
                    List<Long> products = entry.getValue();
                    if (!products.isEmpty() && !usedCategories.contains(category)) {
                        selectedProductIds.add(products.get(0));
                        usedCategories.add(category);
                        break;
                    }
                }
            }
        }
        
        // Second pass: fill remaining slots with diverse products
        for (Map.Entry<String, List<Long>> entry : productsByCategory.entrySet()) {
            if (selectedProductIds.size() >= ChatbotConstants.MAX_PRODUCTS_FOR_OUTFIT) {
                break;
            }
            if (!usedCategories.contains(entry.getKey())) {
                List<Long> products = entry.getValue();
                if (!products.isEmpty()) {
                    selectedProductIds.add(products.get(0));
                    usedCategories.add(entry.getKey());
                }
            }
        }
        
        // Third pass: add remaining products if space available
        for (List<Long> products : productsByCategory.values()) {
            if (selectedProductIds.size() >= ChatbotConstants.MAX_PRODUCTS_FOR_OUTFIT) {
                break;
            }
            for (Long productId : products) {
                if (selectedProductIds.size() >= ChatbotConstants.MAX_PRODUCTS_FOR_OUTFIT) {
                    break;
                }
                selectedProductIds.add(productId);
            }
        }
        
        return selectedProductIds;
    }

    /**
     * Filter products by season based on query and product titles
     */
    private Map<String, List<Long>> filterProductsBySeason(
            Map<String, List<Long>> productsByCategory, 
            String query, 
            Set<Document> allDocuments) {
        
        // Create a map of productId -> product title from documents
        Map<Long, String> productTitleMap = new HashMap<>();
        for (Document doc : allDocuments) {
            Map<String, Object> metadata = doc.getMetadata();
            String type = (String) metadata.get(ChatbotConstants.METADATA_TYPE);
            
            if (ChatbotConstants.DOC_TYPE_PRODUCT.equals(type) || 
                ChatbotConstants.DOC_TYPE_PRODUCT_DETAIL.equals(type)) {
                String productIdStr = (String) metadata.get(ChatbotConstants.METADATA_PRODUCT_ID);
                String title = (String) metadata.get(ChatbotConstants.METADATA_TITLE);
                
                if (productIdStr != null && title != null) {
                    try {
                        Long productId = Long.parseLong(productIdStr);
                        productTitleMap.put(productId, title.toLowerCase());
                    } catch (NumberFormatException e) {
                        // Skip invalid productId
                    }
                }
            }
        }
        
        // Detect season from query
        String queryLower = query.toLowerCase();
        boolean isSummer = queryLower.contains("mùa hè") || queryLower.contains("mùa nóng") || 
                          queryLower.contains("mùa hạ") || queryLower.contains("hè");
        boolean isWinter = queryLower.contains("mùa đông") || queryLower.contains("mùa lạnh") || 
                          queryLower.contains("đông");
        
        // If no season mentioned, return original map
        if (!isSummer && !isWinter) {
            return productsByCategory;
        }
        
        // Filter products by season
        Map<String, List<Long>> filteredProductsByCategory = new HashMap<>();
        
        for (Map.Entry<String, List<Long>> entry : productsByCategory.entrySet()) {
            String category = entry.getKey();
            List<Long> productIds = entry.getValue();
            List<Long> filteredProductIds = new ArrayList<>();
            
            for (Long productId : productIds) {
                String title = productTitleMap.get(productId);
                if (title == null) {
                    // If title not found, keep the product (better to include than exclude)
                    filteredProductIds.add(productId);
                    continue;
                }
                
                boolean matchesSeason = false;
                
                if (isSummer) {
                    // Summer keywords: ngắn tay, áo thun, quần short, mỏng, nhẹ
                    matchesSeason = title.contains("ngắn tay") || 
                                   title.contains("áo thun") || 
                                   title.contains("quần short") ||
                                   title.contains("short") ||
                                   (!title.contains("dài tay") && 
                                    !title.contains("sweatshirt") && 
                                    !title.contains("hoodie") &&
                                    !title.contains("áo len"));
                } else if (isWinter) {
                    // Winter keywords: dài tay, sweatshirt, hoodie, áo len, dày, ấm
                    matchesSeason = title.contains("dài tay") || 
                                   title.contains("sweatshirt") || 
                                   title.contains("hoodie") ||
                                   title.contains("áo len") ||
                                   (!title.contains("ngắn tay") && 
                                    !title.contains("quần short"));
                }
                
                if (matchesSeason) {
                    filteredProductIds.add(productId);
                }
            }
            
            if (!filteredProductIds.isEmpty()) {
                filteredProductsByCategory.put(category, filteredProductIds);
            }
        }
        
        // If filtering removed all products, return original (better to show something than nothing)
        if (filteredProductsByCategory.isEmpty()) {
            log.warn("Season filtering removed all products, returning original results");
            return productsByCategory;
        }
        
        log.debug("Filtered products by season: {} -> {} products", 
                productsByCategory.values().stream().mapToInt(List::size).sum(),
                filteredProductsByCategory.values().stream().mapToInt(List::size).sum());
        
        return filteredProductsByCategory;
    }

    /**
     * Extract product IDs from documents
     */
    private Set<Long> extractProductIdsFromDocuments(List<Document> documents) {
        Set<Long> productIds = new HashSet<>();
        
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
        
        return productIds;
    }

    @Override
    public List<ChatbotResponse.ProductRecommendation> getProductRecommendations(Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<ChatbotResponse.ProductRecommendation> recommendations = new ArrayList<>();
            
            // Duyệt từng productId
            for (Long productId : productIds) {
                // Tìm list ProductDetail tương ứng (đã có JOIN FETCH product, color, size)
                List<ProductDetail> details = productDetailRepository.findActiveDetailsByProductIdWithProduct(productId);
                
                if (!details.isEmpty()) {
                    // Lấy item đầu tiên
                    ProductDetail firstDetail = details.get(0);
                    
                    // Build ProductRecommendation
                    ChatbotResponse.ProductRecommendation recommendation = buildProductRecommendation(firstDetail);
                    recommendations.add(recommendation);
                }
            }
            
            return recommendations;

        } catch (Exception e) {
            log.error("Error getting product recommendations", e);
            return Collections.emptyList();
        }
    }

    /**
     * Build ProductRecommendation từ ProductDetail
     */
    private ChatbotResponse.ProductRecommendation buildProductRecommendation(ProductDetail detail) {
        ChatbotResponse.ProductRecommendation recommendation = new ChatbotResponse.ProductRecommendation();
        
        // objectId = productDetailId
        recommendation.setObjectId(detail.getId());
        
        // title = title của ProductEntity
        if (detail.getProduct() != null && detail.getProduct().getTitle() != null) {
            recommendation.setTitle(detail.getProduct().getTitle());
        } else {
            recommendation.setTitle("");
        }
        
        // description = description của ProductEntity
        if (detail.getProduct() != null && detail.getProduct().getDescription() != null) {
            recommendation.setDescription(detail.getProduct().getDescription());
        } else {
            recommendation.setDescription("");
        }
        
        // imageUrl = từ ProductDetail (native query để lấy imageUrl đầu tiên)
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
        
        // color = name của Color từ database (đã có trong JOIN FETCH)
        if (detail.getColor() != null && detail.getColor().getName() != null) {
            recommendation.setColor(detail.getColor().getName());
        } else {
            recommendation.setColor("");
        }
        
        // size = label của Size từ database (đã có trong JOIN FETCH)
        if (detail.getSize() != null && detail.getSize().getLabel() != null) {
            recommendation.setSize(detail.getSize().getLabel());
        } else {
            recommendation.setSize("");
        }
        
        // price = từ ProductDetail
        recommendation.setPrice(detail.getPrice());
        
        // quantity = từ ProductDetail
        recommendation.setQuantity(detail.getQuantity());
        
        return recommendation;
    }
}

