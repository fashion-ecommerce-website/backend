package com.spring.fit.backend.user.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class UserRankUpdatedEvent {
    private final Long userId;
    private final Short previousRankId;
    private final Short newRankId;
    private final BigDecimal totalSpent;
}