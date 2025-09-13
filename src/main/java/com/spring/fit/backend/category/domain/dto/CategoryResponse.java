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
    private String status;
    private List<CategoryResponse> children;

    // Map tất cả category sang DTO phẳng
    public static Map<Long, CategoryResponse> mapAll(List<Category> categories) {
        Map<Long, CategoryResponse> map = new HashMap<>();
        for (Category c : categories) {
            map.put(c.getId(), new CategoryResponse(c.getId(), c.getName(), c.getSlug(),c.getStatus(), new ArrayList<>()));
        }
        return map;
    }

    // Build cây từ rootId
    public static CategoryResponse buildTree(Long rootId, List<Category> allCategories) {
        Map<Long, CategoryResponse> map = mapAll(allCategories);

        for (Category c : allCategories) {
            if (c.getParent() != null) {
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
        Map<Long, CategoryResponse> map = mapAll(allCategories);

        for (Category c : allCategories) {
            if (c.getParent() != null) {
                CategoryResponse parentDto = map.get(c.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(map.get(c.getId()));
                }
            }
        }

        List<CategoryResponse> roots = allCategories.stream()
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

