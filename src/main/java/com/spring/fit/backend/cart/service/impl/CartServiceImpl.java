package com.spring.fit.backend.cart.service.impl;

import com.spring.fit.backend.cart.domain.dto.AddToCartRequest;
import com.spring.fit.backend.cart.domain.dto.CartDetailResponse;
import com.spring.fit.backend.cart.domain.dto.UpdateCartItemRequest;
import com.spring.fit.backend.cart.domain.entity.CartDetail;
import com.spring.fit.backend.cart.repository.CartDetailRepository;
import com.spring.fit.backend.cart.service.CartService;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.product.repository.ProductRepository;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    private enum ChangeType {
        COLOR_CHANGE, SIZE_CHANGE, NO_CHANGE
    }

    private final CartDetailRepository cartDetailRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public CartDetailResponse addToCart(String userEmail, AddToCartRequest request) {
        log.info("Inside CartServiceImpl.addToCart userEmail={}, productDetailId={}, quantity={}", 
                userEmail, request.getProductDetailId(), request.getQuantity());

        try {
            // Find user
            UserEntity user = findUserByEmail(userEmail);

            // Find product detail
            ProductDetail productDetail = productRepository.findById(request.getProductDetailId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, 
                        "Cannot found product with ID: " + request.getProductDetailId()));

            // Check if product detail is active
            if (!productDetail.getIsActive()) {
                throw new ErrorException(HttpStatus.BAD_REQUEST, "Product is unavailable");
            }

            // Check stock quantity
            if (productDetail.getQuantity() < request.getQuantity()) {
                throw new ErrorException(HttpStatus.BAD_REQUEST, 
                    "The quantity (" + request.getQuantity() + ") exceed stock (" + productDetail.getQuantity() + ")");
            }

            // Check if product already exists in cart
            Optional<CartDetail> existingCartDetail = cartDetailRepository
                    .findByUserIdAndProductDetailId(user.getId(), request.getProductDetailId());

            CartDetail cartDetail;
            if (existingCartDetail.isPresent()) {
                // If exists, update quantity
                cartDetail = existingCartDetail.get();
                int newQuantity = cartDetail.getQuantity() + request.getQuantity();
                
                // Check stock quantity again after adding
                if (productDetail.getQuantity() < newQuantity) {
                    throw new ErrorException(HttpStatus.BAD_REQUEST, 
                        "The total quantity (" + newQuantity + ") exceed stock (" + productDetail.getQuantity() + ")");
                }
                
                cartDetail.setQuantity(newQuantity);
                log.debug("Inside CartServiceImpl.addToCart updatedExisting cartDetailId={}, newQuantity={}", cartDetail.getId(), newQuantity);
            } else {
                // If not exists, create new
                cartDetail = CartDetail.builder()
                        .user(user)
                        .productDetail(productDetail)
                        .quantity(request.getQuantity())
                        .build();
                log.debug("Inside CartServiceImpl.addToCart createdNew userId={}, productDetailId={}, quantity={}", user.getId(), cartDetail.getProductDetail().getId(), cartDetail.getQuantity());
            }

            cartDetail = cartDetailRepository.save(cartDetail);
            
            CartDetailResponse response = CartDetailResponse.fromEntity(cartDetail);
            log.info("Inside CartServiceImpl.addToCart success cartDetailId={}", cartDetail.getId());
            
            return response;
            
        } catch (Exception e) {
            log.error("Inside CartServiceImpl.addToCart error userEmail={}, productDetailId={}, message={}", 
                    userEmail, request.getProductDetailId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartDetailResponse> getCartItems(String userEmail) {
        log.info("Inside CartServiceImpl.getCartItems userEmail={}", userEmail);

        try {
            UserEntity user = findUserByEmail(userEmail);
            List<CartDetail> cartDetails = cartDetailRepository.findByUserIdWithDetails(user.getId());

            List<CartDetailResponse> response = cartDetails.stream()
                    .map(CartDetailResponse::fromEntity)
                    .collect(Collectors.toList());

            log.info("Inside CartServiceImpl.getCartItems success userEmail={}, itemCount={}", userEmail, response.size());
            return response;
            
        } catch (Exception e) {
            log.error("Inside CartServiceImpl.getCartItems error userEmail={}, message={}", userEmail, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public CartDetailResponse updateCartItem(String userEmail, UpdateCartItemRequest request) {
        log.info("Inside CartServiceImpl.updateCartItem userEmail={}, cartDetailId={}, newProductDetailId={}, quantity={}", 
                userEmail, request.getCartDetailId(), request.getNewProductDetailId(), request.getQuantity());

        try {
            UserEntity user = findUserByEmail(userEmail);
            CartDetail currentCartDetail = validateAndGetCurrentCartDetail(request.getCartDetailId(), user.getId());
            ProductDetail newProductDetail = validateAndGetNewProductDetail(request.getNewProductDetailId(), request.getQuantity());
            ProductDetail currentProductDetail = currentCartDetail.getProductDetail();
            
            validateSameProduct(currentProductDetail, newProductDetail);

            // Determine change type and handle accordingly
            ChangeType changeType = determineChangeType(currentProductDetail, newProductDetail);
            
            return switch (changeType) {
                case COLOR_CHANGE, SIZE_CHANGE -> handleProductChange(user, currentCartDetail, newProductDetail, request.getQuantity());
                case NO_CHANGE -> handleQuantityUpdate(currentCartDetail, request.getQuantity());
            };
            
        } catch (Exception e) {
            log.error("Inside CartServiceImpl.updateCartItem error userEmail={}, cartDetailId={}, message={}", 
                    userEmail, request.getCartDetailId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void removeFromCart(String userEmail, Long cartDetailId) {
        log.info("Inside CartServiceImpl.removeFromCart userEmail={}, cartDetailId={}", userEmail, cartDetailId);

        try {
            UserEntity user = findUserByEmail(userEmail);

            // Check if cart detail exists and belongs to user
            CartDetail cartDetail = cartDetailRepository.findByIdAndUserId(cartDetailId, user.getId())
                    .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, 
                        "Cart item not found or you don't have permission to access"));

            cartDetailRepository.delete(cartDetail);
            log.info("Inside CartServiceImpl.removeFromCart success cartDetailId={}", cartDetailId);
            
        } catch (Exception e) {
            log.error("Inside CartServiceImpl.removeFromCart error userEmail={}, cartDetailId={}, message={}", 
                    userEmail, cartDetailId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void removeMultipleFromCart(String userEmail, List<Long> cartDetailIds) {
        log.info("Inside CartServiceImpl.removeMultipleFromCart userEmail={}, itemCount={}", userEmail, cartDetailIds.size());

        try {
            UserEntity user = findUserByEmail(userEmail);

            int deletedCount = cartDetailRepository.deleteByIdsAndUserId(cartDetailIds, user.getId());
            
            if (deletedCount == 0) {
                throw new ErrorException(HttpStatus.NOT_FOUND, 
                    "No cart items found or you don't have permission to access");
            }

            log.info("Inside CartServiceImpl.removeMultipleFromCart success userEmail={}, itemCount={}", userEmail, deletedCount);
            
        } catch (Exception e) {
            log.error("Inside CartServiceImpl.removeMultipleFromCart error userEmail={}, message={}", userEmail, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void clearCart(String userEmail) {
        log.info("Inside CartServiceImpl.clearCart userEmail={}", userEmail);

        try {
            UserEntity user = findUserByEmail(userEmail);
            int deletedCount = cartDetailRepository.deleteAllByUserId(user.getId());

            log.info("Inside CartServiceImpl.clearCart success userEmail={}, deletedItems={}", userEmail, deletedCount);
            
        } catch (Exception e) {
            log.error("Inside CartServiceImpl.clearCart error userEmail={}, message={}", userEmail, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getCartItemCount(String userEmail) {
        log.debug("Inside CartServiceImpl.getCartItemCount userEmail={}", userEmail);
        
        try {
            UserEntity user = findUserByEmail(userEmail);
            long count = cartDetailRepository.countByUserId(user.getId());
            
            log.debug("Inside CartServiceImpl.getCartItemCount success userEmail={}, count={}", userEmail, count);
            return count;
            
        } catch (Exception e) {
            log.error("Inside CartServiceImpl.getCartItemCount error userEmail={}, message={}", userEmail, e.getMessage(), e);
            throw e;
        }
    }

    // Helper methods for updateCartItemWithProductChange
    private CartDetail validateAndGetCurrentCartDetail(Long cartDetailId, Long userId) {
        return cartDetailRepository.findByIdAndUserId(cartDetailId, userId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, 
                    "Cart item not found or you don't have permission to access"));
    }

    private ProductDetail validateAndGetNewProductDetail(Long productDetailId, Integer quantity) {
        ProductDetail productDetail = productRepository.findById(productDetailId)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, 
                    "Product not found with ID: " + productDetailId));

        if (!productDetail.getIsActive()) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Product is not available");
        }

        if (productDetail.getQuantity() < quantity) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, 
                "Requested quantity (" + quantity + ") exceeds available stock (" + productDetail.getQuantity() + ")");
        }

        return productDetail;
    }

    private void validateSameProduct(ProductDetail current, ProductDetail newProduct) {
        if (!current.getProduct().getId().equals(newProduct.getProduct().getId())) {
            throw new ErrorException(HttpStatus.BAD_REQUEST, "Cannot change to a different product");
        }
    }

    private ChangeType determineChangeType(ProductDetail current, ProductDetail newProduct) {
        boolean colorChanged = !current.getColor().getId().equals(newProduct.getColor().getId());
        boolean sizeChanged = !current.getSize().getId().equals(newProduct.getSize().getId());

        if (colorChanged) return ChangeType.COLOR_CHANGE;
        if (sizeChanged) return ChangeType.SIZE_CHANGE;
        return ChangeType.NO_CHANGE;
    }

    private CartDetailResponse handleProductChange(UserEntity user, CartDetail currentItem, ProductDetail newProductDetail, Integer quantity) {
        log.debug("Product variant changed: removing old item and creating new one");
        
        CartDetail resultItem = createOrMergeCartItem(user, newProductDetail, quantity);
        cartDetailRepository.delete(currentItem);
        
        log.info("Successfully updated cart item with product change");
        return CartDetailResponse.fromEntity(resultItem);
    }

    private CartDetailResponse handleQuantityUpdate(CartDetail currentItem, Integer quantity) {
        log.debug("Same color and size: updating quantity");
        
        currentItem.setQuantity(quantity);
        cartDetailRepository.save(currentItem);
        
        log.info("Successfully updated cart item quantity: cartDetailId={}, newQuantity={}", 
                currentItem.getId(), quantity);
        return CartDetailResponse.fromEntity(currentItem);
    }

    private CartDetail createOrMergeCartItem(UserEntity user, ProductDetail productDetail, Integer quantity) {
        Optional<CartDetail> existingItem = cartDetailRepository
                .findByUserIdAndProductDetailId(user.getId(), productDetail.getId());
        
        if (existingItem.isPresent()) {
            CartDetail item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            
            if (productDetail.getQuantity() < newQuantity) {
                throw new ErrorException(HttpStatus.BAD_REQUEST, 
                    "Total quantity (" + newQuantity + ") exceeds available stock (" + productDetail.getQuantity() + ")");
            }
            
            item.setQuantity(newQuantity);
            return cartDetailRepository.save(item);
        } else {
            CartDetail newItem = CartDetail.builder()
                    .user(user)
                    .productDetail(productDetail)
                    .quantity(quantity)
                    .build();
            return cartDetailRepository.save(newItem);
        }
    }

    private UserEntity findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ErrorException(HttpStatus.NOT_FOUND, "User not found"));
    }
}