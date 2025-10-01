package com.spring.fit.backend.wishlist.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.product.domain.dto.response.ProductDetailResponse;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.wishlist.domain.dto.WishlistToggleResponse;
import com.spring.fit.backend.wishlist.domain.entity.Wishlist;
import com.spring.fit.backend.wishlist.repository.WishlistRepository;
import com.spring.fit.backend.wishlist.service.WishlistService;
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

    /**
     * Toggle (add/remove) wishlist item cho user
     */
    @Override
    @Transactional
    public WishlistToggleResponse toggleWishlist(String userEmail, Long detailId) {
        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        ProductDetail detail = productDetailRepository.findById(detailId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found"));

        Optional<Wishlist> existing = wishlistRepository.findByUserIdAndDetailId(user.getId(), detailId);

        WishlistToggleResponse response = new WishlistToggleResponse();
        response.setProductDetail(toProductDetailResponse(detail));

        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            response.setWishlistId(existing.get().getWishlistId());
            response.setStatus(false);
            log.info("Removed from wishlist: userId={}, detailId={}", user.getId(), detailId);
        } else {
            Wishlist wishlist = Wishlist.builder()
                    .userId(user.getId())
                    .detailId(detailId)
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .build();

            Wishlist saved = wishlistRepository.save(wishlist);
            response.setWishlistId(saved.getWishlistId());
            response.setStatus(true);
            log.info("Added to wishlist: userId={}, detailId={}", user.getId(), detailId);
        }

        return response;
    }

    /**
     * Lấy toàn bộ wishlist của user
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductDetailResponse> getWishlistByUserEmail(String userEmail) {
        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        return wishlistRepository.findAllByUserId(user.getId()).stream()
                .map(w -> productDetailRepository.findById(w.getDetailId())
                        .map(this::toProductDetailResponse)
                        .orElse(null))
                .filter(d -> d != null)
                .collect(Collectors.toList());
    }

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

    @Override
    @Transactional
    public void clearWishlistByUser(String userEmail) {
        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        List<Wishlist> wishlists = wishlistRepository.findAllByUserId(user.getId());

        if (wishlists.isEmpty()) {
            log.info("No wishlist items to remove for userId={}", user.getId());
            return;
        }

        wishlistRepository.deleteAll(wishlists);
        log.info("Cleared all wishlist items for userId={}", user.getId());
    }
}
