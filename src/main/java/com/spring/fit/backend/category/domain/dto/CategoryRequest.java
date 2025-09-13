package com.spring.fit.backend.category.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must be at most 255 characters")
    private String slug;
    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "active|inactive", message = "Status must be 'active' or 'inactive'")
    private String status;
    private Long parentId;    // id của category cha (nếu có)

}
