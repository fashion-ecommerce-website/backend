package com.spring.fit.backend.product.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColorRequest {

    @NotBlank(message = "Color name cannot be blank")
    @Size(max = 100, message = "Color name cannot exceed 100 characters")
    private String name;

    @Pattern(
            regexp = "^#([A-Fa-f0-9]{6})$",
            message = "Hex code must be a valid 7-character color code starting with # (e.g. #FFFFFF)"
    )
    private String hex;
}
