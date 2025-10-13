package com.spring.fit.backend.category.controller;

import com.spring.fit.backend.category.domain.dto.CategoryRequest;
import com.spring.fit.backend.category.domain.dto.CategoryResponse;
import com.spring.fit.backend.category.service.CategoryService;
import com.spring.fit.backend.common.model.response.PageResult;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    // Lấy tất cả root category (cha cấp cao nhất) kèm cây con bên trong
    private final CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<PageResult<CategoryResponse>> listAllCategories(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be >= 0") int page,

            @RequestParam(defaultValue = "12")
            @Min(value = 1, message = "PageSize must be >= 1")
            @Max(value = 100, message = "PageSize cannot exceed 100") int pageSize
    ) {
        PageResult<CategoryResponse> result = categoryService.getAllCategories(page, pageSize);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/tree")
    public ResponseEntity<List<CategoryResponse>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }

    @GetMapping("/active-tree")
    public ResponseEntity<List<CategoryResponse>> getCategoryActiveTree() {
        return ResponseEntity.ok(categoryService.getActiveCategoryTree());
    }
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<CategoryResponse> getCategoryTreeByParent(@PathVariable Long parentId) {
        CategoryResponse category = categoryService.getCategoryTreeByParent(parentId);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        CategoryResponse created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request
    ) {
        CategoryResponse updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> changeCategoryStatus(@PathVariable Long id) {
        categoryService.toggleCategoryStatus(id);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    @GetMapping("/search")
    public ResponseEntity<PageResult<CategoryResponse>> searchCategories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) @Max(100) int pageSize
    ) {
        PageResult<CategoryResponse> result = categoryService.searchCategories(name, isActive, page, pageSize);
        return ResponseEntity.ok(result);
    }


}
