package com.spring.fit.backend.user.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Status is required")
    private Boolean isActive;
}
