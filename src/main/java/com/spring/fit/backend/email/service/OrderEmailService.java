package com.spring.fit.backend.email.service;

import com.spring.fit.backend.order.domain.entity.Order;

public interface OrderEmailService {
    void sendOrderDetailsEmail(Order order);
}

