package com.spring.fit.backend.wishlist.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.product.domain.dto.ProductDetailResponse;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import com.spring.fit.backend.product.repository.ProductRepository;
import com.spring.fit.backend.product.service.ProductService;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import com.spring.fit.backend.wishlist.domain.dto.ProductWishlistResponse;
import com.spring.fit.backend.wishlist.domain.entity.Wishlist;
import com.spring.fit.backend.wishlist.repository.WishlistRepository;
import com.spring.fit.backend.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@jakarta.transaction.Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductService productService;

    @Override
    @Transactional(readOnly = true)
    public List<ProductWishlistResponse> getUserWishlist(String userEmail) {

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        // Lấy danh sách wishlist
        List<Wishlist> wishlists = wishlistRepository.findByUser_Id(user.getId());

        return wishlists.stream()
                .map(w -> mapToWishlistResponse(w.getProductDetail()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductWishlistResponse toggleWishlist(String userEmail, Long productId, short colorId, short sizeId) {

        UserEntity user = userRepository.findActiveUserByEmail(userEmail.trim())
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));

        // Tìm product detail dựa vào productId + color + size
        ProductDetail detail = productDetailRepository
                .findByActiveProductAndColorAndSize(productId, colorId, sizeId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "Product detail not found"));

        // Kiểm tra đã có trong wishlist chưa
        Optional<Wishlist> existing = wishlistRepository.findByUser_IdAndProductDetail_Id(user.getId(), detail.getId());

        if (existing.isPresent()) {
            // Nếu có thì xoá
            wishlistRepository.delete(existing.get());
            log.info("Removed from wishlist: detailId={}", detail.getId());
        } else {
            // Nếu chưa có thì thêm mới
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setProductDetail(detail);
            wishlist.setCreatedAt(LocalDateTime.now());
            wishlistRepository.save(wishlist);
            log.info("Added to wishlist: detailId={}", detail.getId());
        }

        // Trả về ProductWishlistResponse (có cả title + detail)
        return mapToWishlistResponse(detail);
    }


    private ProductWishlistResponse mapToWishlistResponse(ProductDetail detail) {
        return ProductWishlistResponse.builder()
                .detailId(detail.getId())
                .id(detail.getProduct().getId())
                .title(detail.getProduct().getTitle())
                .color(detail.getColor().getName())
                .size(detail.getSize().getCode())
                .price(detail.getPrice())
                .build();
    }
}
