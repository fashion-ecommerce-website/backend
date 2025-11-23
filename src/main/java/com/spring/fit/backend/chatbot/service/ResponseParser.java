package com.spring.fit.backend.chatbot.service;

import com.spring.fit.backend.chatbot.constants.ChatbotConstants;
import com.spring.fit.backend.chatbot.domain.dto.ChatbotResponse;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.product.repository.ColorRepository;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import com.spring.fit.backend.product.repository.SizeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Transactional(readOnly = true)
public class ResponseParser {

    private static final Logger log = LoggerFactory.getLogger(ResponseParser.class);

    private final ProductDetailRepository productDetailRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;

    public ResponseParser(
            ProductDetailRepository productDetailRepository,
            ColorRepository colorRepository,
            SizeRepository sizeRepository) {
        this.productDetailRepository = productDetailRepository;
        this.colorRepository = colorRepository;
        this.sizeRepository = sizeRepository;
    }

    /**
     * Parse GPT response to extract product information and find exact ProductDetail
     */
    public List<ChatbotResponse.ProductRecommendation> parseProductRecommendations(
            String gptResponse, 
            Set<Long> productIds,
            List<ChatbotResponse.ProductRecommendation> databaseRecommendations) {
        
        List<ChatbotResponse.ProductRecommendation> parsedRecommendations = new ArrayList<>();
        
        try {
            // Pattern to match product information in GPT response
            // Format: **Title**\n   - ID: productDetailId\n   - Màu sắc: Color\n   - Kích thước: Size\n   - Giá: Price\n   - Số lượng: Quantity\n   - [Xem hình ảnh](URL)
            Pattern productPattern = Pattern.compile(
                    ChatbotConstants.RegexPatterns.PRODUCT_PATTERN_WITH_ID,
                    Pattern.DOTALL
            );
            
            Matcher matcher = productPattern.matcher(gptResponse);
            
            while (matcher.find()) {
                String title = matcher.group(1).trim();
                String productDetailIdStr = matcher.group(2).trim();
                final String colorName = matcher.group(3).trim();
                final String sizeLabel = matcher.group(4).trim();
                String priceStr = matcher.group(5).trim();
                String quantityStr = matcher.group(6).trim();
                String imageUrl = matcher.group(7).trim();
                
                // Parse productDetailId from GPT response
                // Handle formats like "23", "23 (S)", etc. - extract just the number
                Long productDetailId = null;
                try {
                    // Remove any parentheses and extra text, extract just the number
                    String cleanId = productDetailIdStr.replaceAll("[^0-9]", "").trim();
                    if (!cleanId.isEmpty()) {
                        productDetailId = Long.parseLong(cleanId);
                        log.debug("Parsed productDetailId: {} from GPT response: {}", productDetailId, productDetailIdStr);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid productDetailId in GPT response: {}", productDetailIdStr);
                }
                
                // Get ProductDetail from database using productDetailId with eager loading
                ProductDetail productDetail = null;
                if (productDetailId != null) {
                    productDetail = productDetailRepository
                            .findActiveProductDetailByIdWithRelations(productDetailId)
                            .orElse(null);
                    
                    // If productDetailId not found, it might be a productId instead
                    // Try to find a ProductDetail by productId
                    if (productDetail == null) {
                        log.warn("ProductDetail not found for ID: {}. Checking if it's a productId...", productDetailId);
                        // Check if this ID is actually a productId
                        try {
                            List<ProductDetail> details = productDetailRepository
                                    .findActiveDetailsByProductIdWithProduct(productDetailId);
                            if (!details.isEmpty()) {
                                // Try to match by color and size if provided
                                if (!colorName.isEmpty() && !sizeLabel.isEmpty()) {
                                    ProductDetail matched = details.stream()
                                            .filter(d -> d.getColor() != null && 
                                                    d.getColor().getName().equalsIgnoreCase(colorName) &&
                                                    d.getSize() != null && 
                                                    d.getSize().getLabel().equalsIgnoreCase(sizeLabel))
                                            .findFirst()
                                            .orElse(null);
                                    if (matched != null) {
                                        productDetail = matched;
                                        log.info("Found ProductDetail {} for productId {} matching color={}, size={}", 
                                                productDetail.getId(), productDetailId, colorName, sizeLabel);
                                    }
                                }
                                
                                // If no match by color/size, use the first available
                                if (productDetail == null) {
                                    productDetail = details.get(0);
                                    log.info("Found ProductDetail {} for productId {} (first available)", 
                                            productDetail.getId(), productDetailId);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Error checking if ID {} is a productId: {}", productDetailId, e.getMessage());
                        }
                    }
                }
                
                // If still not found, try to find by title + color + size (most accurate matching)
                // This is the PRIMARY method to ensure we get the correct productDetailId
                // ALWAYS use this method to verify and get the correct productDetailId
                if (productDetail == null && !title.isEmpty() && !colorName.isEmpty() && !sizeLabel.isEmpty()) {
                    try {
                        log.info("Trying to find ProductDetail by title='{}', color='{}', size='{}'", title, colorName, sizeLabel);
                        List<ProductDetail> matchedDetails = productDetailRepository
                                .findByProductTitleAndColorAndSize(title, colorName, sizeLabel);
                        if (!matchedDetails.isEmpty()) {
                            productDetail = matchedDetails.get(0);
                            log.info("Found ProductDetail {} by title + color + size matching (objectId will be {})", 
                                    productDetail.getId(), productDetail.getId());
                        } else {
                            log.warn("No ProductDetail found by title='{}', color='{}', size='{}'. Trying fuzzy matching...", 
                                    title, colorName, sizeLabel);
                            
                            // Try fuzzy matching - search for products with similar title
                            // Try different color name variations
                            String[] colorVariations = {
                                colorName,
                                colorName.toLowerCase(),
                                colorName.toUpperCase(),
                                // Vietnamese color name variations
                                colorName.equalsIgnoreCase("black") ? "Đen" : 
                                colorName.equalsIgnoreCase("đen") ? "black" :
                                colorName.equalsIgnoreCase("blue") ? "Xanh" :
                                colorName.equalsIgnoreCase("xanh") ? "blue" :
                                colorName
                            };
                            
                            for (String colorVar : colorVariations) {
                                if (colorVar == null || colorVar.equals(colorName)) continue;
                                matchedDetails = productDetailRepository
                                        .findByProductTitleAndColorAndSize(title, colorVar, sizeLabel);
                                if (!matchedDetails.isEmpty()) {
                                    productDetail = matchedDetails.get(0);
                                    log.info("Found ProductDetail {} by fuzzy color matching: '{}' -> '{}'", 
                                            productDetail.getId(), colorName, colorVar);
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error finding ProductDetail by title + color + size: {}", e.getMessage());
                    }
                }
                
                // CRITICAL: Even if we found productDetail by ID, verify it matches title + color + size
                // This ensures we always get the correct productDetailId, not a wrong one
                if (productDetail != null && !title.isEmpty() && !colorName.isEmpty() && !sizeLabel.isEmpty()) {
                    try {
                        // Verify the found productDetail matches the title + color + size
                        boolean matches = false;
                        if (productDetail.getProduct() != null) {
                            String productTitle = productDetail.getProduct().getTitle();
                            String detailColor = productDetail.getColor() != null ? productDetail.getColor().getName() : "";
                            String detailSize = productDetail.getSize() != null ? productDetail.getSize().getLabel() : "";
                            
                            matches = productTitle.toLowerCase().contains(title.toLowerCase()) &&
                                      detailColor.equalsIgnoreCase(colorName) &&
                                      detailSize.equalsIgnoreCase(sizeLabel);
                        }
                        
                        if (!matches) {
                            log.warn("Found ProductDetail {} but it doesn't match title='{}', color='{}', size='{}'. " +
                                    "Searching again by title + color + size...", 
                                    productDetail.getId(), title, colorName, sizeLabel);
                            
                            // Search again by title + color + size to get the correct one
                            List<ProductDetail> matchedDetails = productDetailRepository
                                    .findByProductTitleAndColorAndSize(title, colorName, sizeLabel);
                            if (!matchedDetails.isEmpty()) {
                                productDetail = matchedDetails.get(0);
                                log.info("Found correct ProductDetail {} by title + color + size verification", productDetail.getId());
                            } else {
                                log.warn("Could not find matching ProductDetail by title + color + size. Using found one: {}", 
                                        productDetail.getId());
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error verifying ProductDetail match: {}", e.getMessage());
                    }
                }
                
                // CRITICAL: If we still don't have productDetail, we MUST find it before setting objectId
                // Never set objectId to productId - it MUST always be productDetailId
                // Try one more time with productIds from the query to find the correct ProductDetail
                if (productDetail == null && !productIds.isEmpty() && !colorName.isEmpty() && !sizeLabel.isEmpty()) {
                    log.warn("Still no ProductDetail found. Trying to find by productIds + color + size...");
                    try {
                        // Try to find ProductDetail using productIds from vector search
                        ProductDetail foundDetail = findExactProductDetailByColorAndSize(
                                new ArrayList<>(productIds), colorName, sizeLabel);
                        if (foundDetail != null) {
                            productDetail = foundDetail;
                            log.info("Found ProductDetail {} using productIds + color + size", productDetail.getId());
                        }
                    } catch (Exception e) {
                        log.warn("Error finding ProductDetail by productIds + color + size: {}", e.getMessage());
                    }
                }
                
                if (productDetail == null) {
                    log.error("CRITICAL: Could not find ProductDetail for title='{}', color='{}', size='{}', productDetailId={}. " +
                            "Trying database recommendations as fallback.", title, colorName, sizeLabel, productDetailId);
                    
                    // Try to find matching recommendation from database
                    if (!databaseRecommendations.isEmpty()) {
                        ChatbotResponse.ProductRecommendation matched = findMatchingByTitleColorAndSize(
                                title, colorName, sizeLabel, databaseRecommendations);
                        if (matched != null && matched.getObjectId() != null) {
                            // Verify that matched.getObjectId() is actually a productDetailId, not productId
                            // Try to find the ProductDetail to confirm
                            Optional<ProductDetail> verifyDetail = productDetailRepository
                                    .findActiveProductDetailByIdWithRelations(matched.getObjectId());
                            if (verifyDetail.isPresent()) {
                                // Confirmed it's a productDetailId - use it
                                parsedRecommendations.add(matched);
                                log.info("Used database recommendation with objectId={} (verified productDetailId) as fallback for title='{}'", 
                                        matched.getObjectId(), title);
                                continue; // Skip to next iteration
                            } else {
                                log.warn("Matched recommendation has objectId={} but it's not a valid productDetailId. Skipping.", 
                                        matched.getObjectId());
                            }
                        }
                    }
                    
                    // If still no match, skip this recommendation
                    log.warn("Skipping recommendation - no valid ProductDetail found for title='{}', color='{}', size='{}'", 
                            title, colorName, sizeLabel);
                    continue;
                }
                
                ChatbotResponse.ProductRecommendation recommendation = new ChatbotResponse.ProductRecommendation();
                
                // Use ProductDetail from database - objectId MUST be productDetailId
                recommendation.setObjectId(productDetail.getId()); // This is guaranteed to be productDetailId
                recommendation.setTitle(title); // Use GPT title
                recommendation.setDescription(
                        productDetail.getProduct() != null && 
                        productDetail.getProduct().getDescription() != null
                                ? productDetail.getProduct().getDescription()
                                : ChatbotConstants.EMPTY_STRING);
                
                // Get image from database if GPT doesn't provide
                if (imageUrl.isEmpty() || ChatbotConstants.EMPTY_STRING.equals(imageUrl)) {
                    imageUrl = getFirstImageUrl(productDetail.getId());
                }
                recommendation.setImageUrl(imageUrl);
                
                // Use data from database if GPT doesn't provide
                String finalColorName = colorName;
                String finalSizeLabel = sizeLabel;
                if (colorName.isEmpty() && productDetail.getColor() != null) {
                    finalColorName = productDetail.getColor().getName();
                }
                if (sizeLabel.isEmpty() && productDetail.getSize() != null) {
                    finalSizeLabel = productDetail.getSize().getLabel();
                }
                if (priceStr.isEmpty() && productDetail.getPrice() != null) {
                    priceStr = productDetail.getPrice().toString();
                }
                if (quantityStr.isEmpty() && productDetail.getQuantity() != null) {
                    quantityStr = productDetail.getQuantity().toString();
                }
                
                recommendation.setColor(finalColorName);
                recommendation.setSize(finalSizeLabel);
                recommendation.setPrice(parsePrice(priceStr));
                recommendation.setQuantity(parseQuantity(quantityStr));
                
                // Check for duplicates before adding
                boolean isDuplicate = parsedRecommendations.stream()
                        .anyMatch(r -> r.getObjectId() != null && r.getObjectId().equals(recommendation.getObjectId()));
                
                if (!isDuplicate) {
                    parsedRecommendations.add(recommendation);
                    log.debug("Added recommendation with objectId={} (productDetailId) for title='{}'", 
                            productDetail.getId(), title);
                } else {
                    log.warn("Skipping duplicate recommendation with objectId={} for title='{}'", 
                            recommendation.getObjectId(), title);
                }
            }
            
            // Fallback: If pattern doesn't match (GPT didn't follow format), try simpler pattern
            if (parsedRecommendations.isEmpty()) {
                log.warn("No products parsed with ID pattern. Trying fallback pattern...");
                return parseProductRecommendationsFallback(gptResponse, productIds, databaseRecommendations);
            }
            
            // If parsed recommendations are too few, supplement with database recommendations
            // Ensure we have at least 4-8 products for a complete outfit
            int minRecommendations = 4;
            if (parsedRecommendations.size() < minRecommendations && !databaseRecommendations.isEmpty()) {
                log.info("Only {} products parsed from GPT. Supplementing with database recommendations to reach minimum of {}", 
                        parsedRecommendations.size(), minRecommendations);
                
                // Get productDetailIds already in parsed recommendations to avoid duplicates
                Set<Long> parsedIds = parsedRecommendations.stream()
                        .map(ChatbotResponse.ProductRecommendation::getObjectId)
                        .collect(java.util.stream.Collectors.toSet());
                
                // Add database recommendations that are not already in parsed list
                // BUT verify that objectId is a valid productDetailId, not productId
                for (ChatbotResponse.ProductRecommendation dbRec : databaseRecommendations) {
                    if (parsedRecommendations.size() >= ChatbotConstants.RECOMMENDATION_LIMIT_OUTFIT) {
                        break;
                    }
                    if (dbRec.getObjectId() != null && !parsedIds.contains(dbRec.getObjectId())) {
                        // Verify this is a productDetailId by checking if ProductDetail exists
                        Optional<ProductDetail> verifyDetail = productDetailRepository
                                .findActiveProductDetailByIdWithRelations(dbRec.getObjectId());
                        if (verifyDetail.isPresent()) {
                            // Confirmed it's a productDetailId
                            parsedRecommendations.add(dbRec);
                            parsedIds.add(dbRec.getObjectId());
                        } else {
                            log.warn("Database recommendation has objectId={} which is not a valid productDetailId. Skipping.", 
                                    dbRec.getObjectId());
                        }
                    }
                }
            }
            
            // If still no products parsed but we have database recommendations, use them
            // BUT verify that all objectIds are productDetailIds, not productIds
            if (parsedRecommendations.isEmpty() && !databaseRecommendations.isEmpty()) {
                log.warn("No products parsed from GPT. Using database recommendations, but verifying objectIds are productDetailIds...");
                List<ChatbotResponse.ProductRecommendation> verifiedRecommendations = new ArrayList<>();
                for (ChatbotResponse.ProductRecommendation rec : databaseRecommendations) {
                    if (rec.getObjectId() != null) {
                        // Verify this is a productDetailId by checking if ProductDetail exists
                        Optional<ProductDetail> verifyDetail = productDetailRepository
                                .findActiveProductDetailByIdWithRelations(rec.getObjectId());
                        if (verifyDetail.isPresent()) {
                            // Confirmed it's a productDetailId
                            verifiedRecommendations.add(rec);
                        } else {
                            log.warn("Database recommendation has objectId={} which is not a valid productDetailId. Skipping.", 
                                    rec.getObjectId());
                        }
                    }
                }
                if (!verifiedRecommendations.isEmpty()) {
                    return verifiedRecommendations;
                }
            }
            
        } catch (Exception e) {
            log.error("Error parsing GPT response: {}", e.getMessage(), e);
            // Fallback to database recommendations
            if (!databaseRecommendations.isEmpty()) {
                return databaseRecommendations;
            }
            return parsedRecommendations;
        }
        
        return parsedRecommendations;
    }

    /**
     * Extract short message from GPT response
     */
    public String extractShortMessage(String gptResponse) {
        // Look for common patterns for short messages
        // Pattern 1: "Dưới đây là..." followed by products, then ending message
        Pattern pattern = Pattern.compile(
                ChatbotConstants.RegexPatterns.SHORT_MESSAGE_PATTERN_1,
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        
        Matcher matcher = pattern.matcher(gptResponse);
        if (matcher.find()) {
            return matcher.group(0).trim();
        }
        
        // Pattern 2: Look for ending message like "Nếu bạn cần..."
        pattern = Pattern.compile(
                ChatbotConstants.RegexPatterns.SHORT_MESSAGE_PATTERN_2,
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        
        matcher = pattern.matcher(gptResponse);
        if (matcher.find()) {
            return ChatbotConstants.SHORT_MESSAGE_PREFIX + matcher.group(0).trim();
        }
        
        // Fallback: Return first sentence or default message
        String[] sentences = gptResponse.split("[.!?]");
        if (sentences.length > 0 && sentences[0].length() < 200) {
            return sentences[0].trim() + ".";
        }
        
        return ChatbotConstants.DEFAULT_SHORT_MESSAGE;
    }

    /**
     * Find exact ProductDetail by productIds, color name, and size label
     * Tries all productIds until finds a match
     */
    private ProductDetail findExactProductDetailByColorAndSize(
            List<Long> productIds,
            String colorName,
            String sizeLabel) {
        
        if (productIds == null || productIds.isEmpty() || colorName == null || colorName.isEmpty() || 
            sizeLabel == null || sizeLabel.isEmpty()) {
            return null;
        }
        
        try {
            // Find color by name (case insensitive)
            var colorOpt = colorRepository.findByNameIgnoreCase(colorName);
            if (colorOpt.isEmpty()) {
                // Try Vietnamese variations
                if (colorName.equalsIgnoreCase("black") || colorName.equalsIgnoreCase("đen")) {
                    colorOpt = colorRepository.findByNameIgnoreCase("Đen");
                } else if (colorName.equalsIgnoreCase("blue") || colorName.equalsIgnoreCase("xanh")) {
                    colorOpt = colorRepository.findByNameIgnoreCase("Xanh");
                }
            }
            if (colorOpt.isEmpty()) {
                log.warn("Color not found: {}", colorName);
                return null;
            }
            Short colorId = colorOpt.get().getId();
            
            // Find size by code or label (case insensitive)
            var sizeOpt = sizeRepository.findByCodeIgnoreCase(sizeLabel);
            if (sizeOpt.isEmpty()) {
                // Try to find by label
                sizeOpt = sizeRepository.findAll().stream()
                        .filter(s -> s.getLabel() != null && 
                                s.getLabel().equalsIgnoreCase(sizeLabel))
                        .findFirst();
            }
            
            if (sizeOpt.isEmpty()) {
                log.warn("Size not found: {}", sizeLabel);
                return null;
            }
            Short sizeId = sizeOpt.get().getId();
            
            // Try each productId to find matching ProductDetail
            for (Long productId : productIds) {
                Optional<ProductDetail> detailOpt = productDetailRepository
                        .findByActiveProductAndColorAndSizeWithRelations(productId, colorId, sizeId);
                
                if (detailOpt.isPresent()) {
                    return detailOpt.get();
                }
            }
            
            // If no exact match, try to find any ProductDetail for these products with the color
            for (Long productId : productIds) {
                List<ProductDetail> details = productDetailRepository
                        .findActiveDetailsByProductIdWithProduct(productId);
                for (ProductDetail detail : details) {
                    if (detail.getColor() != null && detail.getColor().getId().equals(colorId) &&
                        detail.getSize() != null && detail.getSize().getId().equals(sizeId)) {
                        return detail;
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error finding ProductDetail by color and size: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Find exact ProductDetail by productId, color name, and size label
     */
    private ProductDetail findExactProductDetail(
            List<Long> productIds, 
            int index, 
            String colorName, 
            String sizeLabel) {
        
        if (index >= productIds.size()) {
            return null;
        }
        
        try {
            Long productId = productIds.get(index);
            
            // Find color by name (case insensitive)
            var colorOpt = colorRepository.findByNameIgnoreCase(colorName);
            if (colorOpt.isEmpty()) {
                log.warn("Color not found: {}", colorName);
                return null;
            }
            Short colorId = colorOpt.get().getId();
            
            // Find size by code or label (case insensitive)
            var sizeOpt = sizeRepository.findByCodeIgnoreCase(sizeLabel);
            if (sizeOpt.isEmpty()) {
                // Try to find by label
                sizeOpt = sizeRepository.findAll().stream()
                        .filter(s -> s.getLabel() != null && 
                                s.getLabel().equalsIgnoreCase(sizeLabel))
                        .findFirst();
            }
            
            if (sizeOpt.isEmpty()) {
                log.warn("Size not found: {}", sizeLabel);
                return null;
            }
            Short sizeId = sizeOpt.get().getId();
            
            // Find ProductDetail by productId, colorId, sizeId with eager loading
            Optional<ProductDetail> detailOpt = productDetailRepository
                    .findByActiveProductAndColorAndSizeWithRelations(productId, colorId, sizeId);
            
            if (detailOpt.isPresent()) {
                return detailOpt.get();
            }
            
            // Fallback: find any active detail for this product
            List<ProductDetail> details = productDetailRepository
                    .findActiveDetailsByProductIdWithProduct(productId);
            if (!details.isEmpty()) {
                log.warn("Exact ProductDetail not found for productId={}, color={}, size={}. Using first available.", 
                        productId, colorName, sizeLabel);
                return details.get(0);
            }
            
        } catch (Exception e) {
            log.error("Error finding ProductDetail: {}", e.getMessage(), e);
        }
        
        return null;
    }

    /**
     * Get first image URL for ProductDetail using native query
     */
    private String getFirstImageUrl(Long detailId) {
        try {
            Optional<String> imageUrlOpt = productDetailRepository.findFirstImageUrlByDetailId(detailId);
            if (imageUrlOpt.isPresent()) {
                return imageUrlOpt.get();
            }
        } catch (Exception e) {
            log.warn("Error getting image URL for detailId {}: {}", detailId, e.getMessage());
        }
        return ChatbotConstants.EMPTY_STRING;
    }

    private BigDecimal parsePrice(String priceStr) {
        try {
            // Remove "VND", commas, and spaces
            String cleaned = priceStr.replaceAll(ChatbotConstants.RegexPatterns.PRICE_CLEAN_PATTERN, "")
                    .replace(ChatbotConstants.DELIMITER_COMMA, ChatbotConstants.EMPTY_STRING);
            if (!cleaned.isEmpty()) {
                return new BigDecimal(cleaned);
            }
        } catch (Exception e) {
            log.warn("Error parsing price: {}", priceStr);
        }
        return null;
    }

    private Integer parseQuantity(String quantityStr) {
        try {
            return Integer.parseInt(quantityStr.trim());
        } catch (Exception e) {
            log.warn("Error parsing quantity: {}", quantityStr);
        }
        return null;
    }

    /**
     * Fallback parsing when GPT doesn't include productDetailId
     */
    private List<ChatbotResponse.ProductRecommendation> parseProductRecommendationsFallback(
            String gptResponse,
            Set<Long> productIds,
            List<ChatbotResponse.ProductRecommendation> databaseRecommendations) {
        
        List<ChatbotResponse.ProductRecommendation> parsedRecommendations = new ArrayList<>();
        
        try {
            // Simpler pattern without ID requirement
            Pattern productPattern = Pattern.compile(
                    ChatbotConstants.RegexPatterns.PRODUCT_PATTERN_WITHOUT_ID,
                    Pattern.DOTALL
            );
            
            Matcher matcher = productPattern.matcher(gptResponse);
            int productIndex = 0;
            List<Long> productIdList = new ArrayList<>(productIds);
            
            while (matcher.find()) {
                String title = matcher.group(1).trim();
                String colorName = matcher.group(2).trim();
                String sizeLabel = matcher.group(3).trim();
                String priceStr = matcher.group(4).trim();
                String quantityStr = matcher.group(5).trim();
                String imageUrl = matcher.group(6).trim();
                
                // Find exact ProductDetail by productId, color, and size
                ProductDetail productDetail = findExactProductDetail(
                        productIdList, productIndex, colorName, sizeLabel);
                
                ChatbotResponse.ProductRecommendation recommendation = new ChatbotResponse.ProductRecommendation();
                
                if (productDetail != null) {
                    recommendation.setObjectId(productDetail.getId());
                    recommendation.setTitle(title);
                    recommendation.setDescription(
                            productDetail.getProduct() != null && 
                            productDetail.getProduct().getDescription() != null
                                    ? productDetail.getProduct().getDescription()
                                    : ChatbotConstants.EMPTY_STRING);
                    if (imageUrl.isEmpty() || ChatbotConstants.EMPTY_STRING.equals(imageUrl)) {
                        imageUrl = getFirstImageUrl(productDetail.getId());
                    }
                    recommendation.setImageUrl(imageUrl);
                } else if (productIndex < databaseRecommendations.size()) {
                    ChatbotResponse.ProductRecommendation dbRec = databaseRecommendations.get(productIndex);
                    recommendation.setObjectId(dbRec.getObjectId());
                    recommendation.setTitle(title);
                    recommendation.setDescription(dbRec.getDescription());
                    recommendation.setImageUrl(imageUrl.isEmpty() || ChatbotConstants.EMPTY_STRING.equals(imageUrl) 
                            ? dbRec.getImageUrl() : imageUrl);
                } else if (!databaseRecommendations.isEmpty()) {
                    ChatbotResponse.ProductRecommendation first = databaseRecommendations.get(0);
                    recommendation.setObjectId(first.getObjectId());
                    recommendation.setTitle(title);
                    recommendation.setDescription(first.getDescription());
                    recommendation.setImageUrl(imageUrl.isEmpty() || ChatbotConstants.EMPTY_STRING.equals(imageUrl) 
                            ? first.getImageUrl() : imageUrl);
                }
                
                recommendation.setColor(colorName);
                recommendation.setSize(sizeLabel);
                recommendation.setPrice(parsePrice(priceStr));
                recommendation.setQuantity(parseQuantity(quantityStr));
                
                parsedRecommendations.add(recommendation);
                productIndex++;
            }
            
        } catch (Exception e) {
            log.error("Error in fallback parsing: {}", e.getMessage(), e);
        }
        
        // Remove duplicates from fallback parsed recommendations first
        List<ChatbotResponse.ProductRecommendation> uniqueFallbackRecommendations = new ArrayList<>();
        Set<Long> seenFallbackIds = new HashSet<>();
        for (ChatbotResponse.ProductRecommendation rec : parsedRecommendations) {
            if (rec.getObjectId() != null && !seenFallbackIds.contains(rec.getObjectId())) {
                uniqueFallbackRecommendations.add(rec);
                seenFallbackIds.add(rec.getObjectId());
            }
        }
        parsedRecommendations = uniqueFallbackRecommendations;
        log.debug("After removing duplicates from fallback: {} unique recommendations", parsedRecommendations.size());
        
        // If fallback parsing resulted in too few products, supplement with database recommendations
        int minRecommendations = 4;
        if (parsedRecommendations.size() < minRecommendations && !databaseRecommendations.isEmpty()) {
            log.info("Fallback parsing only found {} products. Supplementing with database recommendations.", 
                    parsedRecommendations.size());
            
            // Get productDetailIds already in parsed recommendations to avoid duplicates
            Set<Long> parsedIds = parsedRecommendations.stream()
                    .map(ChatbotResponse.ProductRecommendation::getObjectId)
                    .filter(id -> id != null)
                    .collect(java.util.stream.Collectors.toSet());
            
            // Add database recommendations that are not already in parsed list
            for (ChatbotResponse.ProductRecommendation dbRec : databaseRecommendations) {
                if (parsedRecommendations.size() >= ChatbotConstants.RECOMMENDATION_LIMIT_OUTFIT) {
                    break;
                }
                if (dbRec.getObjectId() != null && !parsedIds.contains(dbRec.getObjectId())) {
                    parsedRecommendations.add(dbRec);
                    parsedIds.add(dbRec.getObjectId());
                }
            }
        }
        
        return parsedRecommendations.isEmpty() ? databaseRecommendations : parsedRecommendations;
    }

    /**
     * Find matching recommendation by color and size
     */
    private ChatbotResponse.ProductRecommendation findMatchingByColorAndSize(
            String colorName,
            String sizeLabel,
            List<ChatbotResponse.ProductRecommendation> recommendations) {
        
        for (ChatbotResponse.ProductRecommendation rec : recommendations) {
            if (rec.getColor() != null && rec.getColor().equalsIgnoreCase(colorName) &&
                rec.getSize() != null && rec.getSize().equalsIgnoreCase(sizeLabel)) {
                return rec;
            }
        }
        
        return null;
    }
    
    /**
     * Find matching recommendation by title, color and size
     */
    private ChatbotResponse.ProductRecommendation findMatchingByTitleColorAndSize(
            String title,
            String colorName,
            String sizeLabel,
            List<ChatbotResponse.ProductRecommendation> recommendations) {
        
        // First try exact match by title + color + size
        for (ChatbotResponse.ProductRecommendation rec : recommendations) {
            if (rec.getTitle() != null && rec.getTitle().toLowerCase().contains(title.toLowerCase()) &&
                rec.getColor() != null && rec.getColor().equalsIgnoreCase(colorName) &&
                rec.getSize() != null && rec.getSize().equalsIgnoreCase(sizeLabel)) {
                return rec;
            }
        }
        
        // Fallback: match by color + size only
        return findMatchingByColorAndSize(colorName, sizeLabel, recommendations);
    }
}

