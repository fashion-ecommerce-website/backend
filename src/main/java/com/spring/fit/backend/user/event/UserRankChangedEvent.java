package com.spring.fit.backend.user.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRankChangedEvent {
    private final Long userId;
    private final Short oldRankId;
    private final Short newRankId;
    private final String userEmail;
}