package com.spring.fit.backend.product.repository;

import com.spring.fit.backend.category.domain.entity.Category;
import com.spring.fit.backend.product.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductMainRepository extends org.springframework.data.jpa.repository.JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.isActive = true")
    Optional<Product> findActiveProductById(@Param("id") Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.categories LEFT JOIN FETCH p.details d " +
            "LEFT JOIN FETCH d.color LEFT JOIN FETCH d.size LEFT JOIN FETCH d.productImages pi " +
            "LEFT JOIN FETCH pi.image WHERE p.id = :id AND p.isActive = true")
    Optional<Product> findActiveProductByIdWithDetails(@Param("id") Long id);

    @Query("""
        SELECT p FROM Product p
        LEFT JOIN FETCH p.categories c
        WHERE p.isActive = true
        ORDER BY p.updatedAt DESC
        """)
    Page<Product> findAllActiveProducts(Pageable pageable);

    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.categories c
        LEFT JOIN FETCH c.parent
        LEFT JOIN FETCH p.details d
        LEFT JOIN FETCH d.color
        LEFT JOIN FETCH d.size
        LEFT JOIN FETCH d.productImages pi
        LEFT JOIN FETCH pi.image
        WHERE p.isActive = true
        """)
    List<Product> findAllActiveProductsWithDetails();

    @Query(value = """
    SELECT p.*
    FROM products p
    JOIN (
      SELECT DISTINCT p.id
      FROM products p
      LEFT JOIN product_categories pc ON p.id = pc.product_id
      LEFT JOIN categories c ON c.id = pc.category_id
      LEFT JOIN product_details pd ON p.id = pd.product_id
      LEFT JOIN colors col ON col.id = pd.color_id
      LEFT JOIN sizes size ON size.id = pd.size_id
      WHERE (:categorySlug IS NULL OR c.slug = :categorySlug)
        AND (:title IS NULL OR LOWER(p.title) LIKE LOWER(:title))
        AND (:isActive IS NULL OR p.is_active = :isActive)
    ) x ON x.id = p.id
    ORDER BY
        CASE WHEN :sortField = 'title' AND :sortDirection = 'asc' THEN p.title END ASC,
        CASE WHEN :sortField = 'title' AND :sortDirection = 'desc' THEN p.title END DESC,
        CASE WHEN :sortField = 'createdAt' AND :sortDirection = 'asc' THEN p.created_at END ASC,
        CASE WHEN :sortField = 'createdAt' AND :sortDirection = 'desc' THEN p.created_at END DESC
    """, nativeQuery = true)
    Page<Product> findAllProductsWithFilter(
            @Param("categorySlug") String categorySlug,
            @Param("title") String title,
            @Param("isActive") Boolean isActive,
            @Param("sortField") String sortField,
            @Param("sortDirection") String sortDirection,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) > 0 FROM Product p JOIN p.categories c WHERE c.id IN :ids")
    boolean existsByCategoryIds(@Param("ids") List<Long> ids);

    boolean existsByTitleIgnoreCaseAndDescriptionIgnoreCaseAndCategoriesIn(
            String title,
            String description,
            Set<Category> categories
    );

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.id = :id AND p.isActive = true")
    boolean existsActiveById(@Param("id") Long id);
}