package com.spring.fit.backend.order.service;

import com.spring.fit.backend.order.domain.entity.Order;

/**
 * Service for managing product inventory operations
 */
public interface InventoryService {
    
    /**
     * Restore stock quantities for all products in an order
     * Used when payment fails or order is cancelled
     * 
     * @param order The order to restore stock for
     */
    void restoreStockForOrder(Order order);
    
    /**
     * Deduct stock quantities for all products in an order
     * Used when payment succeeds for orders with CANCELLED payment status
     * 
     * @param order The order to deduct stock for
     */
    void deductStockForOrder(Order order);
    
    /**
     * Check if there's enough stock for all products in an order
     * 
     * @param order The order to check stock for
     * @return true if all products have sufficient stock
     */
    boolean hasEnoughStockForOrder(Order order);
}