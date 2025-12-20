package com.spring.fit.backend.category.repository;

import com.spring.fit.backend.category.domain.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNull();

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByNameIgnoreCaseAndIsActive(String name, Boolean isActive);

    List<Category> findByParentId(Long parentId);

    boolean existsByName(String name);

    Page<Category> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Category> findAllByIsActive(Boolean isActive, Pageable pageable);

    Page<Category> findAllByNameContainingIgnoreCaseAndIsActive(String name, Boolean isActive, Pageable pageable);

    // 🔹 Fetch all categories tree
    @Query(value = """
            WITH RECURSIVE category_tree AS (
                SELECT id, parent_id, name, slug, is_active, created_at FROM categories WHERE id = :id
                UNION ALL
                SELECT c.id, c.parent_id, c.name, c.slug, c.is_active, c.created_at
                FROM categories c
                INNER JOIN category_tree ct ON c.parent_id = ct.id
            )
            SELECT * FROM category_tree
            """, nativeQuery = true)
    List<Category> findCategoryTree(@Param("id") Long id);


    // 🔹 Update status of categories
    @Modifying
    @Query("UPDATE Category c SET c.isActive = :status WHERE c.id IN :ids")
    void updateStatusByIds(List<Long> ids, boolean status);

    // 🔹 Fetch all categories with parent and children for ingestion
    @Query("""
        SELECT DISTINCT c FROM Category c
        LEFT JOIN FETCH c.parent
        LEFT JOIN FETCH c.children
        WHERE c.isActive = true
        """)
    List<Category> findAllActiveCategoriesWithRelations();

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.id = :id AND c.isActive = true")
    boolean existsActiveById(@Param("id") Long id);
}
