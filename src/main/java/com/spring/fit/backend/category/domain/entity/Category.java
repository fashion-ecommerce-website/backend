package com.spring.fit.backend.category.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spring.fit.backend.product.domain.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "categories")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parent;

    @OneToMany(mappedBy = "parent")
    @JsonIgnore
    private Set<Category> children;


    @Column(nullable = false, columnDefinition = "text")
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Quan hệ nhiều-nhiều ngược với Product
    @ManyToMany(mappedBy = "categories")
    private Set<Product> products;


}