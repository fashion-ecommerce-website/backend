package com.spring.fit.backend.order.service.impl;

import com.spring.fit.backend.common.exception.ErrorException;
import com.spring.fit.backend.order.domain.entity.Order;
import com.spring.fit.backend.order.domain.entity.OrderDetail;
import com.spring.fit.backend.order.service.InventoryService;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import com.spring.fit.backend.product.repository.ProductDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final ProductDetailRepository productDetailRepository;

    @Override
    @Transactional
    public void restoreStockForOrder(Order order) {
        log.info("Restoring stock for order: {}", order.getId());
        
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            log.warn("No order details found for order: {}", order.getId());
            return;
        }

        List<String> restoredProducts = new ArrayList<>();
        
        for (OrderDetail detail : order.getOrderDetails()) {
            ProductDetail productDetail = detail.getProductDetail();
            int quantityToRestore = detail.getQuantity();
            int currentStock = productDetail.getQuantity();
            int newStock = currentStock + quantityToRestore;
            
            productDetail.setQuantity(newStock);
            productDetailRepository.save(productDetail);
            
            String productInfo = String.format("%s (Color: %s, Size: %s): %d -> %d",
                    detail.getTitle(),
                    detail.getColorLabel(),
                    detail.getSizeLabel(),
                    currentStock,
                    newStock);
            restoredProducts.add(productInfo);
            
            log.debug("Restored stock for product detail {}: {} + {} = {}", 
                    productDetail.getId(), currentStock, quantityToRestore, newStock);
        }
        
        log.info("Successfully restored stock for order {}. Products: {}", 
                order.getId(), String.join(", ", restoredProducts));
    }

    @Override
    @Transactional
    public void deductStockForOrder(Order order) {
        log.info("Deducting stock for order: {}", order.getId());
        
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            log.warn("No order details found for order: {}", order.getId());
            return;
        }

        // First, check if all products have enough stock
        List<String> insufficientStockErrors = new ArrayList<>();
        
        for (OrderDetail detail : order.getOrderDetails()) {
            ProductDetail productDetail = detail.getProductDetail();
            int requestedQuantity = detail.getQuantity();
            int currentStock = productDetail.getQuantity();
            
            if (currentStock < requestedQuantity) {
                String error = String.format("%s (Color: %s, Size: %s) - Requested: %d, Available: %d",
                        detail.getTitle(),
                        detail.getColorLabel(),
                        detail.getSizeLabel(),
                        requestedQuantity,
                        currentStock);
                insufficientStockErrors.add(error);
            }
        }
        
        // If any product doesn't have enough stock, throw exception
        if (!insufficientStockErrors.isEmpty()) {
            String errorMessage = "Insufficient stock for products: " + String.join("; ", insufficientStockErrors);
            log.error("Cannot deduct stock for order {}: {}", order.getId(), errorMessage);
            throw new ErrorException(HttpStatus.BAD_REQUEST, errorMessage);
        }
        
        // All products have enough stock, proceed with deduction
        List<String> deductedProducts = new ArrayList<>();
        
        for (OrderDetail detail : order.getOrderDetails()) {
            ProductDetail productDetail = detail.getProductDetail();
            int quantityToDeduct = detail.getQuantity();
            int currentStock = productDetail.getQuantity();
            int newStock = currentStock - quantityToDeduct;
            
            productDetail.setQuantity(newStock);
            productDetailRepository.save(productDetail);
            
            String productInfo = String.format("%s (Color: %s, Size: %s): %d -> %d",
                    detail.getTitle(),
                    detail.getColorLabel(),
                    detail.getSizeLabel(),
                    currentStock,
                    newStock);
            deductedProducts.add(productInfo);
            
            log.debug("Deducted stock for product detail {}: {} - {} = {}", 
                    productDetail.getId(), currentStock, quantityToDeduct, newStock);
        }
        
        log.info("Successfully deducted stock for order {}. Products: {}", 
                order.getId(), String.join(", ", deductedProducts));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEnoughStockForOrder(Order order) {
        log.debug("Checking stock availability for order: {}", order.getId());
        
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            log.warn("No order details found for order: {}", order.getId());
            return true;
        }

        for (OrderDetail detail : order.getOrderDetails()) {
            ProductDetail productDetail = detail.getProductDetail();
            int requestedQuantity = detail.getQuantity();
            int currentStock = productDetail.getQuantity();
            
            if (currentStock < requestedQuantity) {
                log.warn("Insufficient stock for product detail {}: requested {}, available {}", 
                        productDetail.getId(), requestedQuantity, currentStock);
                return false;
            }
        }
        
        log.debug("All products have sufficient stock for order: {}", order.getId());
        return true;
    }
}
