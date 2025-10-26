package com.spring.fit.backend.product.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSizeRequest {

    @NotNull(message = "Size ID cannot be null")
    @Positive(message = "Size ID must be positive")
    private Short id;

    @NotBlank(message = "Size code cannot be blank")
    @Size(max = 50, message = "Size code cannot exceed 50 characters")
    private String code;

    @NotBlank(message = "Size label cannot be blank")
    @Size(max = 100, message = "Size label cannot exceed 100 characters")
    private String label;

    private Boolean isActive;
}
