package com.spring.fit.backend.product.repository;

import com.spring.fit.backend.product.domain.dto.response.ProductCardView;
import com.spring.fit.backend.product.domain.dto.response.SizeQuantityView;
import com.spring.fit.backend.product.domain.entity.ProductDetail;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

@Repository
public interface ProductRepository extends JpaRepository<ProductDetail, Long> {

    @Query(
            value = """
        WITH filtered AS (
          SELECT d.id, d.product_id, d.color_id, d.size_id, d.price, d.quantity,
                 d.slug AS product_slug,
                 p.title AS product_title,
                 c.name  AS color_name,
                 p.created_at
          FROM product_details d
          JOIN products p ON p.id = d.product_id AND p.is_active = TRUE
          JOIN colors   c ON c.id = d.color_id
          JOIN sizes    s ON s.id = d.size_id
          JOIN product_categories pc ON pc.product_id = p.id
          JOIN categories cat ON cat.id = pc.category_id AND cat.is_active = TRUE
          WHERE d.is_active = TRUE
            AND (:title IS NULL OR p.title ILIKE CONCAT('%%', :title, '%%'))
            AND (:colorsEmpty = TRUE OR c.name IN (:colors))
            AND (:sizesEmpty  = TRUE OR s.code IN (:sizes))
            AND (:category IS NULL OR cat.slug = :category)
        )
        , one_per_color AS (
          SELECT DISTINCT ON (product_id, color_id)
                 id            AS detail_id,
                 product_title,
                 product_slug,
                 color_name,
                 price,
                 quantity,
                 product_id,
                 color_id,
                 created_at
          FROM filtered
          ORDER BY product_id, color_id, price   -- pick cheapest per (product,color)
        )
        , promotion_for_sku AS (
          SELECT DISTINCT ON (opc.detail_id)
            opc.detail_id,
            opc.product_id,
            opc.price AS base_price,
            promo.id AS promotion_id,
            promo.name AS promotion_name,
            CASE 
              WHEN promo.type = 'PERCENT' THEN opc.price * promo.value / 100
              ELSE promo.value
            END AS discount_amount
          FROM one_per_color opc
          JOIN promotions promo ON promo.is_active = TRUE 
            AND promo.start_at <= :currentTime 
            AND promo.end_at >= :currentTime
          JOIN promotion_targets pt ON pt.promotion_id = promo.id
            AND (
              (pt.target_type = 'SKU' AND pt.target_id = opc.detail_id)
              OR (pt.target_type = 'PRODUCT' AND pt.target_id = opc.product_id)
              OR (pt.target_type = 'CATEGORY' AND pt.target_id IN (
                SELECT category_id FROM product_categories WHERE product_id = opc.product_id
              ))
            )
          ORDER BY opc.detail_id
        )
        , products_with_final_price AS (
          SELECT
            opc.product_id,
            opc.detail_id,
            opc.product_title,
            opc.product_slug,
            opc.color_name,
            opc.price,
            opc.quantity,
            opc.color_id,
            opc.created_at,
            GREATEST(opc.price - COALESCE(pfs.discount_amount, 0), 0) AS final_price,
            COALESCE(pfs.discount_amount, 0) AS discount_amount,
            pfs.promotion_id,
            pfs.promotion_name
          FROM one_per_color opc
          LEFT JOIN promotion_for_sku pfs ON pfs.detail_id = opc.detail_id
        )
        SELECT
          pwfp.product_id                 AS productId,
          pwfp.detail_id                  AS detailId,
          pwfp.product_title              AS productTitle,
          pwfp.product_slug               AS productSlug,
          pwfp.color_name                 AS colorName,
          pwfp.price                      AS price,
          pwfp.quantity                   AS quantity,
          -- all unique colors of this product:
          (SELECT ARRAY(
               SELECT DISTINCT c2.name
               FROM product_details d2
               JOIN colors c2 ON c2.id = d2.color_id
               WHERE d2.product_id = pwfp.product_id AND d2.is_active = TRUE
               ORDER BY c2.name
           ))                              AS colors,
          -- first 2 image urls of this detail:
          (SELECT ARRAY(
               SELECT i.url
               FROM product_images pi
               JOIN images i ON i.id = pi.image_id
               WHERE pi.detail_id = pwfp.detail_id
               ORDER BY pi.created_at
               LIMIT 2
           ))                              AS imageUrls,
          -- promotion fields
          pwfp.final_price                 AS finalPrice,
          CASE 
            WHEN pwfp.price > 0 THEN CAST(FLOOR(pwfp.discount_amount * 100 / pwfp.price) AS INTEGER)
            ELSE 0
          END                              AS percentOff,
          pwfp.promotion_id                AS promotionId,
          pwfp.promotion_name              AS promotionName
        FROM products_with_final_price pwfp
        WHERE (:minPrice IS NULL OR pwfp.final_price >= :minPrice)
          AND (:maxPrice IS NULL OR pwfp.final_price <= :maxPrice)
        ORDER BY
          CASE WHEN :sortBy = 'productTitle' AND :sortDir = 'asc'  THEN pwfp.product_title END ASC,
          CASE WHEN :sortBy = 'productTitle' AND :sortDir = 'desc' THEN pwfp.product_title END DESC,
          CASE WHEN :sortBy = 'price'        AND :sortDir = 'asc'  THEN pwfp.final_price END ASC,
          CASE WHEN :sortBy = 'price'        AND :sortDir = 'desc' THEN pwfp.final_price END DESC,
          CASE WHEN :sortBy = 'createdAt' AND :sortDir = 'asc' THEN pwfp.created_at END ASC,
          CASE WHEN :sortBy = 'createdAt' AND :sortDir = 'desc' THEN pwfp.created_at END DESC,
          -- fallback mặc định khi client không gửi sort
          pwfp.product_title ASC, pwfp.detail_id ASC
        
        """,
            countQuery = """
    WITH filtered AS (
      SELECT d.id, d.product_id, d.color_id, d.price
      FROM product_details d
      JOIN products p ON p.id = d.product_id AND p.is_active = TRUE
      JOIN colors   c ON c.id = d.color_id
      JOIN sizes    s ON s.id = d.size_id
      JOIN product_categories pc ON pc.product_id = p.id
      JOIN categories cat ON cat.id = pc.category_id AND cat.is_active = TRUE
      WHERE d.is_active = TRUE
        AND (:title IS NULL OR p.title ILIKE CONCAT('%%', :title, '%%'))
        AND (:colorsEmpty = TRUE OR c.name IN (:colors))
        AND (:sizesEmpty  = TRUE OR s.code IN (:sizes))
        AND (:category IS NULL OR cat.slug = :category)
    )
    , one_per_color AS (
      SELECT DISTINCT ON (product_id, color_id)
             id AS detail_id,
             product_id,
             price
      FROM filtered
      ORDER BY product_id, color_id, price
    )
    , promotion_for_sku AS (
      SELECT DISTINCT ON (opc.detail_id)
        opc.detail_id,
        CASE 
          WHEN promo.type = 'PERCENT' THEN opc.price * promo.value / 100
          ELSE promo.value
        END AS discount_amount
      FROM one_per_color opc
      JOIN promotions promo ON promo.is_active = TRUE 
        AND promo.start_at <= :currentTime 
        AND promo.end_at >= :currentTime
      JOIN promotion_targets pt ON pt.promotion_id = promo.id
        AND (
          (pt.target_type = 'SKU' AND pt.target_id = opc.detail_id)
          OR (pt.target_type = 'PRODUCT' AND pt.target_id = opc.product_id)
          OR (pt.target_type = 'CATEGORY' AND pt.target_id IN (
            SELECT category_id FROM product_categories WHERE product_id = opc.product_id
          ))
        )
      ORDER BY opc.detail_id
    )
    , products_with_final_price AS (
      SELECT
        opc.product_id,
        opc.detail_id,
        GREATEST(opc.price - COALESCE(pfs.discount_amount, 0), 0) AS final_price
      FROM one_per_color opc
      LEFT JOIN promotion_for_sku pfs ON pfs.detail_id = opc.detail_id
    )
    SELECT COUNT(*) FROM products_with_final_price pwfp
    WHERE (:minPrice IS NULL OR pwfp.final_price >= :minPrice)
      AND (:maxPrice IS NULL OR pwfp.final_price <= :maxPrice)
  """,
            nativeQuery = true
    )
    Page<ProductCardView> filterProducts(
            @Param("category") String categorySlug,
            @Param("title") String title,
            @Param("colors") List<String> colors,
            @Param("sizes") List<String>  sizes,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("colorsEmpty") boolean colorsEmpty,
            @Param("sizesEmpty")  boolean sizesEmpty,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir,
            @Param("currentTime") java.time.LocalDateTime currentTime,
            Pageable pageable
    );

    @Query(
            value = """
        WITH filtered AS (
          SELECT d.id, d.product_id, d.color_id, d.size_id, d.price, d.quantity,
                 d.slug AS product_slug,
                 p.title AS product_title,
                 c.name  AS color_name
          FROM product_details d
          JOIN products p ON p.id = d.product_id AND p.is_active = TRUE
          JOIN colors   c ON c.id = d.color_id
          JOIN sizes    s ON s.id = d.size_id
          JOIN product_categories pc ON pc.product_id = p.id
          JOIN categories cat ON cat.id = pc.category_id AND cat.is_active = TRUE
          WHERE d.is_active = TRUE AND d.id IN (:ids)
        )
        , one_per_color AS (
          SELECT DISTINCT ON (product_id, color_id)
                 id            AS detail_id,
                 product_title,
                 product_slug,
                 color_name,
                 price,
                 quantity,
                 product_id,
                 color_id
          FROM filtered
          ORDER BY product_id, color_id, price   -- pick cheapest per (product,color)
        )
        SELECT
          opc.product_id                 AS productId,
          opc.detail_id                  AS detailId,
          opc.product_title              AS productTitle,
          opc.product_slug               AS productSlug,
          opc.color_name                 AS colorName,
          opc.price                      AS price,
          opc.quantity                   AS quantity,
          -- all unique colors of this product:
          (SELECT ARRAY(
               SELECT DISTINCT c2.name
               FROM product_details d2
               JOIN colors c2 ON c2.id = d2.color_id
               WHERE d2.product_id = opc.product_id AND d2.is_active = TRUE
               ORDER BY c2.name
           ))                              AS colors,
          -- first 2 image urls of this detail:
          (SELECT ARRAY(
               SELECT i.url
               FROM product_images pi
               JOIN images i ON i.id = pi.image_id
               WHERE pi.detail_id = opc.detail_id
               ORDER BY pi.created_at
               LIMIT 2
           ))                              AS imageUrls
        FROM one_per_color opc
        """,
        nativeQuery = true
    )
    List<ProductCardView> findRecentlyViewedProduct(@Param("ids") List<Integer> id);

    @Query(
            value = """
        WITH filtered AS (
          SELECT d.id, d.product_id, d.color_id, d.size_id, d.price, d.quantity,
                 d.slug AS product_slug,
                 p.title AS product_title,
                 c.name  AS color_name
          FROM product_details d
          JOIN products p ON p.id = d.product_id AND p.is_active = TRUE
          JOIN colors   c ON c.id = d.color_id
          JOIN sizes    s ON s.id = d.size_id
          WHERE d.is_active = TRUE AND p.id IN (:productIds)
        )
        , one_per_product AS (
          SELECT DISTINCT ON (product_id)
                 id            AS detail_id,
                 product_id,
                 product_title,
                 product_slug,
                 color_name,
                 price,
                 quantity,
                 color_id
          FROM filtered
          ORDER BY product_id, price ASC   -- pick cheapest detail per product
        )
        SELECT
          opp.product_id                 AS productId,
          opp.detail_id                  AS detailId,
          opp.product_title              AS productTitle,
          opp.product_slug               AS productSlug,
          opp.color_name                 AS colorName,
          opp.price                      AS price,
          opp.quantity                   AS quantity,
          -- all unique colors of this product:
          (SELECT ARRAY(
               SELECT DISTINCT c2.name
               FROM product_details d2
               JOIN colors c2 ON c2.id = d2.color_id
               WHERE d2.product_id = opp.product_id AND d2.is_active = TRUE
               ORDER BY c2.name
           ))                              AS colors,
          -- first 2 image urls of this detail:
          (SELECT ARRAY(
               SELECT i.url
               FROM product_images pi
               JOIN images i ON i.id = pi.image_id
               WHERE pi.detail_id = opp.detail_id
               ORDER BY pi.created_at
               LIMIT 2
           ))                              AS imageUrls
        FROM one_per_product opp
        ORDER BY opp.product_id
        """,
        nativeQuery = true
    )
    List<ProductCardView> findProductsByProductIds(@Param("productIds") List<Long> productIds);


    @Query(value = """
        SELECT DISTINCT c.name
        FROM product_details d
        JOIN colors c ON c.id = d.color_id
        WHERE d.product_id = (
            SELECT pd.product_id FROM product_details pd WHERE pd.id = :detailId
        )
          AND d.is_active = TRUE
        ORDER BY c.name
        """, nativeQuery = true)
    List<String> findAllColorsByDetailId(@Param("detailId") Long detailId);

    @Query(value = """
        SELECT s.code AS sizeCode, COALESCE(d.quantity, 0) AS quantity
        FROM product_details d
        JOIN sizes s ON s.id = d.size_id
        WHERE d.product_id = (
            SELECT pd.product_id FROM product_details pd WHERE pd.id = :detailId
        )
          AND d.color_id = (
            SELECT pd.color_id FROM product_details pd WHERE pd.id = :detailId
          )
          AND d.is_active = TRUE
        ORDER BY s.code
        """, nativeQuery = true)
    List<SizeQuantityView> findSizeQuantityByDetailId(@Param("detailId") Long detailId);

    @Query(value = """
        SELECT i.url
        FROM product_images pi
        JOIN images i ON i.id = pi.image_id
        WHERE pi.detail_id = :detailId
        ORDER BY pi.created_at
        """, nativeQuery = true)
    List<String> findImageUrlsByDetailId(@Param("detailId") Long detailId);

    @Query(value = """
        SELECT d.id
        FROM product_details d
        WHERE d.product_id = (
            SELECT pd.product_id FROM product_details pd WHERE pd.id = :baseDetailId
        )
          AND d.color_id = (
            SELECT c.id FROM colors c WHERE LOWER(c.name) = LOWER(:colorName)
          )
          AND d.is_active = TRUE
        ORDER BY d.price ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<Long> findDetailIdForColor(@Param("baseDetailId") Long baseDetailId, @Param("colorName") String colorName);

    @Query(value = """
        SELECT d.id
        FROM product_details d
        WHERE d.product_id = (
            SELECT pd.product_id FROM product_details pd WHERE pd.id = :baseDetailId
        )
          AND d.color_id = (
            SELECT c.id FROM colors c WHERE LOWER(c.name) = LOWER(:colorName)
          )
          AND d.size_id = (
            SELECT s.id FROM sizes s WHERE LOWER(s.code) = LOWER(:size) OR LOWER(s.label) = LOWER(:size)
          )
          AND d.is_active = TRUE
        ORDER BY d.price ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<Long> findDetailIdForColorAndSize(@Param("baseDetailId") Long baseDetailId,
                                               @Param("colorName") String colorName,
                                               @Param("size") String size);

    @Query(value = """
    SELECT i.url
    FROM product_images pi
    JOIN product_details pd ON pi.detail_id = pd.id
    JOIN images i ON i.id = pi.image_id
    WHERE pd.product_id = :productId
    ORDER BY pi.created_at ASC
    LIMIT 1
    """, nativeQuery = true)
    List<String> findFirstImageUrlByProductIdForAdmin(@Param("productId") Long productId);

    @Query(value = """
    SELECT pd.id
    FROM product_images pi
    JOIN product_details pd ON pi.detail_id = pd.id
    WHERE pd.product_id = :productId
    ORDER BY pi.created_at ASC
    LIMIT 1
    """, nativeQuery = true)
    Long findFirstDetailIdByProductIdForAdmin(@Param("productId") Long productId);

    @Query(value = """
    SELECT DISTINCT c.name
    FROM product_details d
    JOIN colors c ON c.id = d.color_id
    WHERE d.product_id = :productId
      AND d.is_active = TRUE
    ORDER BY c.name
    """, nativeQuery = true)
    List<String> findAllColorsByProductId(@Param("productId") Long productId);

    @Query(value = """
    SELECT DISTINCT s.code
    FROM product_details d
    JOIN sizes s ON s.id = d.size_id
    WHERE d.product_id = :productId
      AND d.is_active = TRUE
    ORDER BY s.code
    """, nativeQuery = true)
    List<String> findAllSizesByProductId(@Param("productId") Long productId);

    @Query(value = """
        SELECT d.*
        FROM product_details d
        JOIN products p ON p.id = d.product_id
        WHERE d.id = :id
          AND d.is_active = TRUE
          AND p.is_active = TRUE
        """, nativeQuery = true)
    Optional<ProductDetail> findActiveDetailById(@Param("id") Long id);

    @Query(
            value = """
        WITH filtered AS (
          SELECT d.id, d.product_id, d.color_id, d.size_id, d.price, d.quantity,
                 d.slug AS product_slug,
                 p.title AS product_title,
                 c.name  AS color_name
          FROM product_details d
          JOIN products p ON p.id = d.product_id AND p.is_active = TRUE
          JOIN colors   c ON c.id = d.color_id
          JOIN sizes    s ON s.id = d.size_id
          JOIN product_categories pc ON pc.product_id = p.id
          JOIN categories cat ON cat.id = pc.category_id AND cat.is_active = TRUE
          WHERE d.is_active = TRUE
            AND (:title IS NULL OR p.title ILIKE CONCAT('%%', :title, '%%'))
            AND (:colorsEmpty = TRUE OR c.name IN (:colors))
            AND (:sizesEmpty  = TRUE OR s.code IN (:sizes))
        )
        , one_per_color AS (
          SELECT DISTINCT ON (product_id, color_id)
                 id            AS detail_id,
                 product_title,
                 product_slug,
                 color_name,
                 price,
                 quantity,
                 product_id,
                 color_id
          FROM filtered
          ORDER BY product_id, color_id, price
        )
        , promotion_for_sku AS (
          SELECT DISTINCT ON (opc.detail_id)
            opc.detail_id,
            opc.product_id,
            opc.price AS base_price,
            promo.id AS promotion_id,
            promo.name AS promotion_name,
            CASE 
              WHEN promo.type = 'PERCENT' THEN opc.price * promo.value / 100
              ELSE promo.value
            END AS discount_amount
          FROM one_per_color opc
          JOIN promotions promo ON promo.is_active = TRUE 
            AND promo.start_at <= :currentTime 
            AND promo.end_at >= :currentTime
          JOIN promotion_targets pt ON pt.promotion_id = promo.id
            AND (
              (pt.target_type = 'SKU' AND pt.target_id = opc.detail_id)
              OR (pt.target_type = 'PRODUCT' AND pt.target_id = opc.product_id)
              OR (pt.target_type = 'CATEGORY' AND pt.target_id IN (
                SELECT category_id FROM product_categories WHERE product_id = opc.product_id
              ))
            )
          ORDER BY opc.detail_id
        )
        , products_with_promotion AS (
          SELECT
            opc.product_id,
            opc.detail_id,
            opc.product_title,
            opc.product_slug,
            opc.color_name,
            opc.price,
            opc.quantity,
            COALESCE(pfs.discount_amount, 0) AS discount_amount,
            GREATEST(opc.price - COALESCE(pfs.discount_amount, 0), 0) AS final_price,
            pfs.promotion_id,
            pfs.promotion_name
          FROM one_per_color opc
          JOIN promotion_for_sku pfs ON pfs.detail_id = opc.detail_id
          WHERE GREATEST(opc.price - COALESCE(pfs.discount_amount, 0), 0) < opc.price
        )
        SELECT
          pwp.product_id                   AS productId,
          pwp.detail_id                    AS detailId,
          pwp.product_title                AS productTitle,
          pwp.product_slug                 AS productSlug,
          pwp.color_name                   AS colorName,
          pwp.price                        AS price,
          pwp.quantity                     AS quantity,
          (SELECT ARRAY(
               SELECT DISTINCT c2.name
               FROM product_details d2
               JOIN colors c2 ON c2.id = d2.color_id
               WHERE d2.product_id = pwp.product_id AND d2.is_active = TRUE
               ORDER BY c2.name
           ))                              AS colors,
          (SELECT ARRAY(
               SELECT i.url
               FROM product_images pi
               JOIN images i ON i.id = pi.image_id
               WHERE pi.detail_id = pwp.detail_id
               ORDER BY pi.created_at
               LIMIT 2
           ))                              AS imageUrls,
          pwp.final_price                  AS finalPrice,
          CASE 
            WHEN pwp.price > 0 THEN CAST(FLOOR(pwp.discount_amount * 100 / pwp.price) AS INTEGER)
            ELSE 0
          END                              AS percentOff,
          pwp.promotion_id                 AS promotionId,
          pwp.promotion_name               AS promotionName
        FROM products_with_promotion pwp
        WHERE (:minPrice IS NULL OR pwp.final_price >= :minPrice)
          AND (:maxPrice IS NULL OR pwp.final_price <= :maxPrice)
        ORDER BY
          CASE WHEN :sortBy = 'productTitle' AND :sortDir = 'asc'  THEN pwp.product_title END ASC,
          CASE WHEN :sortBy = 'productTitle' AND :sortDir = 'desc' THEN pwp.product_title END DESC,
          CASE WHEN :sortBy = 'price'        AND :sortDir = 'asc'  THEN pwp.final_price END ASC,
          CASE WHEN :sortBy = 'price'        AND :sortDir = 'desc' THEN pwp.final_price END DESC,
          pwp.product_title ASC, pwp.detail_id ASC
        """,
            countQuery = """
        WITH filtered AS (
          SELECT d.id, d.product_id, d.color_id, d.price
          FROM product_details d
          JOIN products p ON p.id = d.product_id AND p.is_active = TRUE
          JOIN colors   c ON c.id = d.color_id
          JOIN sizes    s ON s.id = d.size_id
          JOIN product_categories pc ON pc.product_id = p.id
          JOIN categories cat ON cat.id = pc.category_id AND cat.is_active = TRUE
          WHERE d.is_active = TRUE
            AND (:title IS NULL OR p.title ILIKE CONCAT('%%', :title, '%%'))
            AND (:colorsEmpty = TRUE OR c.name IN (:colors))
            AND (:sizesEmpty  = TRUE OR s.code IN (:sizes))
        )
        , one_per_color AS (
          SELECT DISTINCT ON (product_id, color_id)
                 id            AS detail_id,
                 product_id,
                 price
          FROM filtered
          ORDER BY product_id, color_id, price
        )
        , promotion_for_sku AS (
          SELECT DISTINCT ON (opc.detail_id)
            opc.detail_id,
            CASE 
              WHEN promo.type = 'PERCENT' THEN opc.price * promo.value / 100
              ELSE promo.value
            END AS discount_amount
          FROM one_per_color opc
          JOIN promotions promo ON promo.is_active = TRUE 
            AND promo.start_at <= :currentTime 
            AND promo.end_at >= :currentTime
          JOIN promotion_targets pt ON pt.promotion_id = promo.id
            AND (
              (pt.target_type = 'SKU' AND pt.target_id = opc.detail_id)
              OR (pt.target_type = 'PRODUCT' AND pt.target_id = opc.product_id)
              OR (pt.target_type = 'CATEGORY' AND pt.target_id IN (
                SELECT category_id FROM product_categories WHERE product_id = opc.product_id
              ))
            )
          ORDER BY opc.detail_id
        )
        , products_with_promotion AS (
          SELECT
            opc.detail_id,
            GREATEST(opc.price - COALESCE(pfs.discount_amount, 0), 0) AS final_price
          FROM one_per_color opc
          JOIN promotion_for_sku pfs ON pfs.detail_id = opc.detail_id
          WHERE GREATEST(opc.price - COALESCE(pfs.discount_amount, 0), 0) < opc.price
        )
        SELECT COUNT(*) FROM products_with_promotion pwp
        WHERE (:minPrice IS NULL OR pwp.final_price >= :minPrice)
          AND (:maxPrice IS NULL OR pwp.final_price <= :maxPrice)
        """,
            nativeQuery = true
    )
    Page<ProductCardView> filterDiscountedProducts(
            @Param("title") String title,
            @Param("colors") List<String> colors,
            @Param("sizes") List<String>  sizes,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("colorsEmpty") boolean colorsEmpty,
            @Param("sizesEmpty")  boolean sizesEmpty,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir,
            @Param("currentTime") java.time.LocalDateTime currentTime,
            Pageable pageable
    );

    @Query(value = """
        WITH RECURSIVE category_tree AS (
            SELECT id FROM categories WHERE id = :rootCategoryId AND is_active = TRUE
            UNION ALL
            SELECT c.id 
            FROM categories c
            INNER JOIN category_tree ct ON c.parent_id = ct.id
            WHERE c.is_active = TRUE
        ),
        filtered AS (
            SELECT d.id, d.product_id, d.color_id, d.size_id, d.price, d.quantity,
                   d.slug AS product_slug,
                   p.title AS product_title,
                   c.name AS color_name,
                   p.created_at
            FROM product_details d
            JOIN products p ON p.id = d.product_id AND p.is_active = TRUE
            JOIN colors c ON c.id = d.color_id
            JOIN sizes s ON s.id = d.size_id
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE d.is_active = TRUE
              AND pc.category_id IN (SELECT id FROM category_tree)
        ),
        one_per_product AS (
            SELECT DISTINCT ON (product_id)
                   id AS detail_id,
                   product_id,
                   product_title,
                   product_slug,
                   color_name,
                   price,
                   quantity,
                   color_id,
                   created_at
            FROM filtered
            ORDER BY product_id, price ASC
        )
        SELECT
            opp.product_id AS productId,
            opp.detail_id AS detailId,
            opp.product_title AS productTitle,
            opp.product_slug AS productSlug,
            opp.color_name AS colorName,
            opp.price AS price,
            opp.quantity AS quantity,
            (SELECT ARRAY(
                SELECT DISTINCT c2.name
                FROM product_details d2
                JOIN colors c2 ON c2.id = d2.color_id
                WHERE d2.product_id = opp.product_id AND d2.is_active = TRUE
                ORDER BY c2.name
            )) AS colors,
            (SELECT ARRAY(
                SELECT i.url
                FROM product_images pi
                JOIN images i ON i.id = pi.image_id
                WHERE pi.detail_id = opp.detail_id
                ORDER BY pi.created_at
                LIMIT 2
            )) AS imageUrls
        FROM one_per_product opp
        ORDER BY opp.created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<ProductCardView> findNewArrivalsByRootCategory(
            @Param("rootCategoryId") Long rootCategoryId,
            @Param("limit") int limit
    );
}
