package com.spring.fit.backend.category.domain.dto;

import com.spring.fit.backend.category.domain.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private Boolean isActive;
    private List<CategoryResponse> children;

    // Map tất cả category sang DTO phẳng
    public static Map<Long, CategoryResponse> mapAll(List<Category> categories) {
        Map<Long, CategoryResponse> map = new HashMap<>();
        for (Category c : categories) {
            map.put(c.getId(), new CategoryResponse(c.getId(), c.getName(), c.getSlug(),c.getIsActive(), new ArrayList<>()));
        }
        return map;
    }

    // Build cây từ rootId
    public static CategoryResponse buildTree(Long rootId, List<Category> allCategories) {
        // ✅ Lọc chỉ các category active
        List<Category> activeCategories = allCategories.stream()
                .filter(Category::getIsActive)
                .collect(Collectors.toList());

        Map<Long, CategoryResponse> map = mapAll(activeCategories);

        for (Category c : activeCategories) {
            if (c.getParent() != null && c.getParent().getIsActive()) {
                CategoryResponse parentDto = map.get(c.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(map.get(c.getId()));
                }
            }
        }

        CategoryResponse root = map.get(rootId);
        if (root != null) {
            setEmptyChildrenToNull(root);
        }
        return root;
    }

    // Build cây cho tất cả root
    public static List<CategoryResponse> buildTree(List<Category> allCategories) {
        List<Category> activeCategories = allCategories.stream()
                .filter(Category::getIsActive)
                .collect(Collectors.toList());

        Map<Long, CategoryResponse> map = mapAll(activeCategories);

        for (Category c : activeCategories) {
            if (c.getParent() != null && c.getParent().getIsActive()) {
                CategoryResponse parentDto = map.get(c.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(map.get(c.getId()));
                }
            }
        }

        List<CategoryResponse> roots = activeCategories.stream()
                .filter(c -> c.getParent() == null)
                .map(c -> map.get(c.getId()))
                .collect(Collectors.toList());

        roots.forEach(CategoryResponse::setEmptyChildrenToNull);
        return roots;
    }


    private static void setEmptyChildrenToNull(CategoryResponse node) {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            node.setChildren(null);
        } else {
            node.getChildren().forEach(CategoryResponse::setEmptyChildrenToNull);
        }
    }
}

