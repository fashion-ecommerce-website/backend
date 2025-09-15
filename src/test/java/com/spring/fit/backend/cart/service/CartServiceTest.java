package com.spring.fit.backend.cart.service;

import com.spring.fit.backend.cart.domain.dto.AddToCartRequest;
import com.spring.fit.backend.cart.domain.dto.CartDetailResponse;
import com.spring.fit.backend.cart.domain.entity.CartDetail;
import com.spring.fit.backend.cart.repository.CartDetailRepository;
import com.spring.fit.backend.cart.service.impl.CartServiceImpl;
import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.product.domain.entity.Product;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.product.domain.entity.Color;
import com.spring.fit.backend.product.domain.entity.Size;
import com.spring.fit.backend.product.repository.ProductRepository;
import com.spring.fit.backend.security.domain.entity.UserEntity;
import com.spring.fit.backend.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartDetailRepository cartDetailRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private UserEntity user;
    private Product product;
    private ProductDetail productDetail;
    private Color color;
    private Size size;
    private AddToCartRequest addToCartRequest;

    @BeforeEach
    void setUp() {
        // Setup user
        user = new UserEntity();
        user.setId(1L);
        user.setEmail("test@example.com");

        // Setup color
        color = new Color();
        color.setId((short) 1);
        color.setName("Đỏ");

        // Setup size
        size = new Size();
        size.setId((short) 1);
        size.setCode("M");
        size.setLabel("Medium");

        // Setup product
        product = new Product();
        product.setId(1L);
        product.setTitle("Áo thun test");
        product.setIsActive(true);

        // Setup product detail
        productDetail = new ProductDetail();
        productDetail.setId(1L);
        productDetail.setProduct(product);
        productDetail.setColor(color);
        productDetail.setSize(size);
        productDetail.setPrice(BigDecimal.valueOf(299000));
        productDetail.setQuantity(10);
        productDetail.setIsActive(true);
        productDetail.setSlug("ao-thun-test-do-m");

        // Setup request
        addToCartRequest = AddToCartRequest.builder()
                .productDetailId(1L)
                .quantity(2)
                .build();
    }

    @Test
    void addToCart_ShouldCreateNewCartDetail_WhenProductNotInCart() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(productDetail));
        when(cartDetailRepository.findByUserIdAndProductDetailId(1L, 1L)).thenReturn(Optional.empty());

        CartDetail savedCartDetail = CartDetail.builder()
                .id(1L)
                .user(user)
                .productDetail(productDetail)
                .quantity(2)
                .build();

        when(cartDetailRepository.save(any(CartDetail.class))).thenReturn(savedCartDetail);

        // When
        CartDetailResponse response = cartService.addToCart("test@example.com", addToCartRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getProductDetailId());
        assertEquals("Áo thun test", response.getProductTitle());
        assertEquals("Đỏ", response.getColorName());
        assertEquals("Medium", response.getSizeName());
        assertEquals(BigDecimal.valueOf(299000), response.getPrice());
        assertEquals(2, response.getQuantity());
        assertEquals(10, response.getAvailableQuantity());

        verify(cartDetailRepository).save(any(CartDetail.class));
    }

    @Test
    void addToCart_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ErrorException exception = assertThrows(ErrorException.class, () -> {
            cartService.addToCart("test@example.com", addToCartRequest);
        });

        assertEquals("Không tìm thấy sản phẩm với ID: 1", exception.getMessage());
    }

    @Test
    void addToCart_ShouldThrowException_WhenQuantityExceedsStock() {
        // Given
        addToCartRequest.setQuantity(15); // Vượt quá số lượng tồn kho (10)

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(productDetail));
        when(cartDetailRepository.findByUserIdAndProductDetailId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        ErrorException exception = assertThrows(ErrorException.class, () -> {
            cartService.addToCart("test@example.com", addToCartRequest);
        });

        assertTrue(exception.getMessage().contains("vượt quá số lượng tồn kho"));
    }

    @Test
    void addToCart_ShouldUpdateQuantity_WhenProductAlreadyInCart() {
        // Given
        CartDetail existingCartDetail = CartDetail.builder()
                .id(1L)
                .user(user)
                .productDetail(productDetail)
                .quantity(3)
                .build();

        CartDetail updatedCartDetail = CartDetail.builder()
                .id(1L)
                .user(user)
                .productDetail(productDetail)
                .quantity(5) // 3 + 2
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(productDetail));
        when(cartDetailRepository.findByUserIdAndProductDetailId(1L, 1L)).thenReturn(Optional.of(existingCartDetail));
        when(cartDetailRepository.save(any(CartDetail.class))).thenReturn(updatedCartDetail);

        // When
        CartDetailResponse response = cartService.addToCart("test@example.com", addToCartRequest);

        // Then
        assertNotNull(response);
        assertEquals(5, response.getQuantity());

        verify(cartDetailRepository).save(existingCartDetail);
        assertEquals(5, existingCartDetail.getQuantity());
    }

    @Test
    void addToCart_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        ErrorException exception = assertThrows(ErrorException.class, () -> {
            cartService.addToCart("test@example.com", addToCartRequest);
        });

        assertEquals("Không tìm thấy user", exception.getMessage());
    }
}

