package com.spring.fit.backend.common.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RateLimitResponse extends ErrorResponse {

    private Integer retryAfter;
    private Integer hourlyLimit;
    private Integer currentUsage;
}
