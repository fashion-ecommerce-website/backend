package com.spring.fit.backend.product.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColorResponse {
    private Short id;
    private String name;
    private String hex;
} 