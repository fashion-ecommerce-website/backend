package com.spring.fit.backend.recommendation.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.product.domain.dto.response.ProductCardWithPromotionResponse;
import com.spring.fit.backend.product.domain.entity.Product;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import com.spring.fit.backend.product.repository.ProductMainRepository;
import com.spring.fit.backend.product.service.ProductService;
import com.spring.fit.backend.recommendation.client.AiRecommendationClient;
import com.spring.fit.backend.recommendation.domain.entity.Interaction;
import com.spring.fit.backend.recommendation.domain.enums.ActionType;
import com.spring.fit.backend.recommendation.repository.InteractionRepository;
import com.spring.fit.backend.recommendation.service.RecommendationService;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.user.service.RecentViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final InteractionRepository interactionRepository;
    private final UserRepository userRepository;
    private final ProductMainRepository productMainRepository;
    private final ProductDetailRepository productDetailRepository;
    private final AiRecommendationClient aiRecommendationClient;
    private final RecentViewService recentViewService;
    private final ProductService productService;

    @Override
    @Transactional
    public void recordInteraction(ActionType actionType, Long userId, Long productId, Integer count) {
        log.info("Recording interaction - userId: {}, productId: {}, actionType: {}, count: {}", 
                 userId, productId, actionType, count);

        // Validate user and product
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));
        
        Product product = productMainRepository.findById(productId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product not found"));

        // Convert enum to string for database storage
        String actionTypeStr = actionType.name();

        // Tìm hoặc tạo interaction record
        Interaction interaction = interactionRepository
                .findByUserIdAndProductIdAndActionType(userId, productId, actionTypeStr)
                .orElse(Interaction.builder()
                        .user(user)
                        .product(product)
                        .actionType(actionTypeStr)
                        .count(0)
                        .build());

        // Tăng count
        interaction.setCount(interaction.getCount() + count);
        interactionRepository.save(interaction);

        log.info("Saved interaction to database: userId={}, productId={}, actionType={}, totalCount={}",
                 userId, productId, actionTypeStr, interaction.getCount());
    }

    @Override
    public List<ProductCardWithPromotionResponse> getRecommendationsForYou(Long userId, List<Long> sessionHistory, int limit) {
        log.info("Getting recommendations for user_id: {}, has_session_history: {}", 
                 userId, sessionHistory != null && !sessionHistory.isEmpty());

        // Lấy danh sách productId được đề xuất từ AI recommendation
        List<Long> productIds = getRecommendationIds(userId, sessionHistory, limit);

        // Chuyển đổi danh sách productId thành danh sách sản phẩm với promotion
        // Mỗi productId sẽ được lấy một detailId bất kỳ (active) của product đó
        List<ProductCardWithPromotionResponse> recommendations = productService.getProductsWithPromotionByProductIds(
                productIds, userId);

        log.info("Returning {} recommendations with promotion for user_id: {}", 
                 recommendations.size(), userId);
        
        return recommendations;
    }

    private List<Long> getRecommendationIds(Long userId, List<Long> sessionHistory, int limit) {
        // Kịch bản 1: User "ấm" - Gọi personalized recommendations
        if (userId != null) {
            List<Long> personalizedRecs = aiRecommendationClient.getUserRecommendations(userId);
            
            if (personalizedRecs != null && !personalizedRecs.isEmpty()) {
                log.info("User 'hot' - Found {} personalized recommendations", personalizedRecs.size());
                return personalizedRecs.stream().limit(limit).toList();
            }
            
            log.info("User 'hot' - No personalized recommendations (404), moving to scenario 2");
        }

        // Kịch bản 2: User "mới" có xem hàng - Lấy từ recent views
        Long lastDetailId = null;
        
        if (userId != null) {
            List<Long> recentIds = recentViewService.getRecentIds(userId);
            if (recentIds != null && !recentIds.isEmpty()) {
                lastDetailId = recentIds.get(0);
                log.info("Using last detailId from recent views: {}", lastDetailId);
            }
        }
        
        if (lastDetailId != null) {
            // Lấy productId từ detailId
            Optional<Long> productIdOpt = productDetailRepository.findProductIdByDetailId(lastDetailId);
            if (productIdOpt.isPresent()) {
                Long productId = productIdOpt.get();
                log.info("Converted detailId {} to productId {}", lastDetailId, productId);
                
                // Gọi AI recommendation với productId
                List<Long> similarItems = aiRecommendationClient.getSimilarItems(productId, limit);
                if (similarItems != null && !similarItems.isEmpty()) {
                    log.info("New user has viewed products - Found {} similar items", similarItems.size());
                    return similarItems;
                }
            } else {
                log.warn("Could not find productId for detailId: {}", lastDetailId);
            }
        }

        // Kịch bản 3: User "lạnh" hoàn toàn - Lấy most popular
        log.info("User 'cold' - Getting most popular items");
        List<Long> popularItems = aiRecommendationClient.getMostPopular(limit);
        if (popularItems != null && !popularItems.isEmpty()) {
            log.info("Found {} popular items", popularItems.size());
            return popularItems;
        }

        // Fallback cuối cùng: Trả về empty list
        log.warn("All scenarios failed, returning empty list");
        return new ArrayList<>();
    }

    @Override
    public List<ProductCardWithPromotionResponse> getSimilarItems(Long itemId, Long userId, int limit) {
        log.info("Getting similar items for item_id: {}, user_id: {}, limit: {}", itemId, userId, limit);
        
        // Lấy danh sách productId tương tự từ AI recommendation
        // itemId ở đây là productId (AI recommendation service nhận productId)
        List<Long> similarProductIds = aiRecommendationClient.getSimilarItems(itemId, limit);
        
        if (similarProductIds == null || similarProductIds.isEmpty()) {
            log.warn("No similar items found for item_id: {}", itemId);
            return new ArrayList<>();
        }
        
        // Chuyển đổi danh sách productId thành danh sách sản phẩm với promotion
        // Mỗi productId sẽ được lấy một detailId bất kỳ (active) của product đó
        List<ProductCardWithPromotionResponse> similarItems = productService.getProductsWithPromotionByProductIds(
                similarProductIds, userId);
        
        log.info("Returning {} similar items with promotion for item_id: {}", 
                 similarItems.size(), itemId);
        
        return similarItems;
    }

    @Override
    public void trainModel() {
        log.info("Starting model training process...");
        try {
            aiRecommendationClient.trainModel();
            log.info("Model training process completed successfully");
        } catch (Exception e) {
            log.error("Error during model training: {}", e.getMessage(), e);
            throw new ErrorException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Failed to train model: " + e.getMessage());
        }
    }
}
