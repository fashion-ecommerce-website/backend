package com.spring.fit.backend.category.controller;

import com.spring.fit.backend.category.domain.dto.CategoryRequest;
import com.spring.fit.backend.category.domain.dto.CategoryResponse;
import com.spring.fit.backend.category.service.CategoryService;
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

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryResponse>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<CategoryResponse> getCategoryTreeByParent(@PathVariable Long parentId) {
        CategoryResponse category = categoryService.getCategoryTreeByParent(parentId);
        return ResponseEntity.ok(category);
    }

    // Tìm category theo slug
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponse category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(category);
    }

    // Tìm category theo tên
    @GetMapping("/name/{name}")
    public ResponseEntity<CategoryResponse> getCategoryByName(@PathVariable String name) {
        CategoryResponse category = categoryService.getCategoryByName(name);
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

}
