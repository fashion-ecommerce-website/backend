package com.spring.fit.backend.wishlist.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.product.domain.dto.response.ProductDetailResponse;
import com.spring.fit.backend.product.domain.dto.response.ProductDetailWithPromotionResponse;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.wishlist.domain.dto.WishlistToggleResponse;
import com.spring.fit.backend.wishlist.domain.entity.Wishlist;
import com.spring.fit.backend.wishlist.repository.WishlistRepository;
import com.spring.fit.backend.wishlist.service.WishlistService;
import com.spring.fit.backend.promotion.domain.dto.request.PromotionApplyRequest;
import com.spring.fit.backend.promotion.domain.dto.response.PromotionApplyResponse;
import com.spring.fit.backend.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductDetailRepository productDetailRepository;
    private final UserRepository userRepository;
    private final PromotionService promotionService;


    /**
     * Toggle wishlist (add / remove)
     */
    @Override
    @Transactional
    public WishlistToggleResponse toggleWishlist(String userEmail, Long detailId) {

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        ProductDetail detail = productDetailRepository.findById(detailId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found"));

        Optional<Wishlist> existing =
                wishlistRepository.findByUserAndProductDetail(user, detail);

        WishlistToggleResponse response = new WishlistToggleResponse();
        response.setProductDetail(toProductDetailResponse(detail));

        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            response.setWishlistId(existing.get().getWishlistId());
            response.setStatus(false);
            log.info("Removed from wishlist: userId={}, detailId={}", user.getId(), detailId);
        } else {
            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .productDetail(detail)
                    .build();

            Wishlist saved = wishlistRepository.save(wishlist);
            response.setWishlistId(saved.getWishlistId());
            response.setStatus(true);
            log.info("Added to wishlist: userId={}, detailId={}", user.getId(), detailId);
        }

        return response;
    }

    /**
     * Get wishlist of user
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductDetailResponse> getWishlistByUserEmail(String userEmail) {

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        return wishlistRepository.findAllByUser(user).stream()
                .map(w -> toProductDetailResponse(w.getProductDetail()))
                .collect(Collectors.toList());
    }

    /**
     * Get wishlist with promotion
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductDetailWithPromotionResponse> getWishlistByUserEmailWithPromotion(String userEmail) {

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        return wishlistRepository.findAllByUser(user).stream()
                .map(w -> toProductDetailWithPromotionResponse(w.getProductDetail()))
                .collect(Collectors.toList());
    }

    /**
     * Clear wishlist
     */
    @Override
    @Transactional
    public void clearWishlistByUser(String userEmail) {

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        wishlistRepository.deleteAllByUser(user);
        log.info("Cleared wishlist for userId={}", user.getId());
    }

    // ===================== MAPPING =====================

    private ProductDetailResponse toProductDetailResponse(ProductDetail detail) {
        return ProductDetailResponse.builder()
                .detailId(detail.getId())
                .title(detail.getProduct().getTitle())
                .price(detail.getPrice())
                .activeColor(detail.getColor().getName())
                .activeSize(detail.getSize().getLabel())
                .images(
                        detail.getProductImages().stream()
                                .map(pi -> pi.getImage().getUrl())
                                .toList()
                )
                .mapSizeToQuantity(
                        Map.of(
                                detail.getSize().getLabel(),
                                detail.getQuantity()
                        )
                )
                .build();
    }

    private ProductDetailWithPromotionResponse toProductDetailWithPromotionResponse(ProductDetail detail) {

        ProductDetailResponse base = toProductDetailResponse(detail);

        PromotionApplyResponse promo;
        try {
            promo = promotionService.applyPromotionForSku(
                    PromotionApplyRequest.builder()
                            .skuId(detail.getId())
                            .basePrice(detail.getPrice())
                            .build()
            );
        } catch (Exception e) {
            promo = PromotionApplyResponse.builder()
                    .basePrice(detail.getPrice())
                    .finalPrice(detail.getPrice())
                    .percentOff(0)
                    .build();
        }

        return ProductDetailWithPromotionResponse.builder()
                .detailId(base.getDetailId())
                .title(base.getTitle())
                .price(base.getPrice())
                .finalPrice(promo.getFinalPrice())
                .percentOff(promo.getPercentOff())
                .promotionId(promo.getPromotionId())
                .promotionName(promo.getPromotionName())
                .activeColor(base.getActiveColor())
                .activeSize(base.getActiveSize())
                .images(base.getImages())
                .mapSizeToQuantity(base.getMapSizeToQuantity())
                .build();
    }
}
