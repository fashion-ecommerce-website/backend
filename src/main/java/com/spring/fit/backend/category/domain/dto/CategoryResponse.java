package com.spring.fit.backend.category.domain.dto;

import com.spring.fit.backend.category.domain.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private List<CategoryResponse> children;

    public static CategoryResponse mapToResponseRecursively(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<CategoryResponse> childResponses = category.getChildren().stream()
                    .map(CategoryResponse::mapToResponseRecursively)
                    .toList();
            response.setChildren(childResponses);
        }

        return response;
    }
    public static CategoryResponse mapToResponse(Category category) {
        if (category == null) return null;

        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            // ⚡ Sao chép danh sách con ra list mới để tránh ConcurrentModificationException
            List<Category> childrenCopy = new ArrayList<>(category.getChildren());

            response.setChildren(
                    childrenCopy.stream()
                            .map(CategoryResponse::mapToResponse)
                            .collect(Collectors.toList())
            );
        }

        return response;
    }
}
