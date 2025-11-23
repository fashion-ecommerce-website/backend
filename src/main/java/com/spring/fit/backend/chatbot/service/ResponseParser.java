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
                String colorName = matcher.group(3).trim();
                String sizeLabel = matcher.group(4).trim();
                String priceStr = matcher.group(5).trim();
                String quantityStr = matcher.group(6).trim();
                String imageUrl = matcher.group(7).trim();
                
                // Parse productDetailId from GPT response
                Long productDetailId = null;
                try {
                    productDetailId = Long.parseLong(productDetailIdStr);
                } catch (NumberFormatException e) {
                    log.warn("Invalid productDetailId in GPT response: {}", productDetailIdStr);
                }
                
                // Get ProductDetail from database using productDetailId with eager loading
                ProductDetail productDetail = null;
                if (productDetailId != null) {
                    productDetail = productDetailRepository
                            .findActiveProductDetailByIdWithRelations(productDetailId)
                            .orElse(null);
                }
                
                ChatbotResponse.ProductRecommendation recommendation = new ChatbotResponse.ProductRecommendation();
                
                if (productDetail != null) {
                    // Use ProductDetail from database
                    recommendation.setObjectId(productDetail.getId());
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
                    if (colorName.isEmpty() && productDetail.getColor() != null) {
                        colorName = productDetail.getColor().getName();
                    }
                    if (sizeLabel.isEmpty() && productDetail.getSize() != null) {
                        sizeLabel = productDetail.getSize().getLabel();
                    }
                    if (priceStr.isEmpty() && productDetail.getPrice() != null) {
                        priceStr = productDetail.getPrice().toString();
                    }
                    if (quantityStr.isEmpty() && productDetail.getQuantity() != null) {
                        quantityStr = productDetail.getQuantity().toString();
                    }
                } else {
                    // Fallback: try to find by color and size if productDetailId not found
                    log.warn("ProductDetail not found for ID: {}. Trying to find by color and size.", productDetailId);
                    // This will be handled by fallback logic below
                }
                
                // If still no productDetail found, use fallback
                if (productDetail == null && !databaseRecommendations.isEmpty()) {
                    // Try to find matching recommendation
                    ChatbotResponse.ProductRecommendation matched = findMatchingByColorAndSize(
                            colorName, sizeLabel, databaseRecommendations);
                    if (matched != null) {
                        recommendation.setObjectId(matched.getObjectId());
                        recommendation.setTitle(title);
                        recommendation.setDescription(matched.getDescription());
                        recommendation.setImageUrl(imageUrl.isEmpty() || ChatbotConstants.EMPTY_STRING.equals(imageUrl) 
                            ? matched.getImageUrl() : imageUrl);
                    } else {
                        // Use first available
                        ChatbotResponse.ProductRecommendation first = databaseRecommendations.get(0);
                        recommendation.setObjectId(first.getObjectId());
                        recommendation.setTitle(title);
                        recommendation.setDescription(first.getDescription());
                        recommendation.setImageUrl(imageUrl.isEmpty() || ChatbotConstants.EMPTY_STRING.equals(imageUrl) 
                                ? first.getImageUrl() : imageUrl);
                    }
                }
                
                recommendation.setColor(colorName);
                recommendation.setSize(sizeLabel);
                recommendation.setPrice(parsePrice(priceStr));
                recommendation.setQuantity(parseQuantity(quantityStr));
                
                parsedRecommendations.add(recommendation);
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
                for (ChatbotResponse.ProductRecommendation dbRec : databaseRecommendations) {
                    if (parsedRecommendations.size() >= ChatbotConstants.RECOMMENDATION_LIMIT_OUTFIT) {
                        break;
                    }
                    if (!parsedIds.contains(dbRec.getObjectId())) {
                        parsedRecommendations.add(dbRec);
                        parsedIds.add(dbRec.getObjectId());
                    }
                }
            }
            
            // If still no products parsed but we have database recommendations, use them
            if (parsedRecommendations.isEmpty() && !databaseRecommendations.isEmpty()) {
                log.warn("No products parsed from GPT. Using database recommendations.");
                return databaseRecommendations;
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
        
        // If fallback parsing resulted in too few products, supplement with database recommendations
        int minRecommendations = 4;
        if (parsedRecommendations.size() < minRecommendations && !databaseRecommendations.isEmpty()) {
            log.info("Fallback parsing only found {} products. Supplementing with database recommendations.", 
                    parsedRecommendations.size());
            
            // Get productDetailIds already in parsed recommendations to avoid duplicates
            Set<Long> parsedIds = parsedRecommendations.stream()
                    .map(ChatbotResponse.ProductRecommendation::getObjectId)
                    .collect(java.util.stream.Collectors.toSet());
            
            // Add database recommendations that are not already in parsed list
            for (ChatbotResponse.ProductRecommendation dbRec : databaseRecommendations) {
                if (parsedRecommendations.size() >= ChatbotConstants.RECOMMENDATION_LIMIT_OUTFIT) {
                    break;
                }
                if (!parsedIds.contains(dbRec.getObjectId())) {
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
}

