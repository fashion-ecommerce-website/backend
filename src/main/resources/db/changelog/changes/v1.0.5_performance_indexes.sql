--liquibase formatted sql
--changeset fit-team:perf-001-db-indexes
--comment Performance: Add database indexes for frequently filtered columns

-- Index cho product_details.is_active (dùng trong hầu hết WHERE clauses)
CREATE INDEX IF NOT EXISTS idx_product_details_is_active ON product_details(is_active);

-- Index cho product_details.product_id (JOIN với products)
CREATE INDEX IF NOT EXISTS idx_product_details_product_id ON product_details(product_id);

-- Index cho product_details.color_id (JOIN với colors)
CREATE INDEX IF NOT EXISTS idx_product_details_color_id ON product_details(color_id);

-- Index cho product_details.size_id (JOIN với sizes)
CREATE INDEX IF NOT EXISTS idx_product_details_size_id ON product_details(size_id);

-- Composite index cho (product_id, color_id) - dùng trong DISTINCT ON
CREATE INDEX IF NOT EXISTS idx_product_details_product_color ON product_details(product_id, color_id);

-- Index cho products.is_active
CREATE INDEX IF NOT EXISTS idx_products_is_active ON products(is_active);

-- Index cho products.created_at (ORDER BY trong new-arrivals)
CREATE INDEX IF NOT EXISTS idx_products_created_at ON products(created_at DESC);

-- Index cho categories.slug (filter theo category slug)
CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug);

-- Index cho categories.parent_id (subquery trong category filter)
CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON categories(parent_id);

-- Index cho categories.is_active
CREATE INDEX IF NOT EXISTS idx_categories_is_active ON categories(is_active);

-- Index cho colors.name (WHERE c.name IN (:colors))
CREATE INDEX IF NOT EXISTS idx_colors_name ON colors(name);

-- Index cho sizes.code (WHERE s.code IN (:sizes))
CREATE INDEX IF NOT EXISTS idx_sizes_code ON sizes(code);

-- Index cho product_categories.product_id
CREATE INDEX IF NOT EXISTS idx_product_categories_product_id ON product_categories(product_id);

-- Index cho product_categories.category_id
CREATE INDEX IF NOT EXISTS idx_product_categories_category_id ON product_categories(category_id);

-- Index cho product_images.detail_id (subquery lấy image URLs)
CREATE INDEX IF NOT EXISTS idx_product_images_detail_id ON product_images(detail_id);

-- Index cho product_images.created_at (ORDER BY trong subquery)
CREATE INDEX IF NOT EXISTS idx_product_images_detail_created ON product_images(detail_id, created_at);

-- Composite index cho promotions: (is_active, start_at, end_at) - JOIN condition trong promotion query
CREATE INDEX IF NOT EXISTS idx_promotions_active_dates ON promotions(is_active, start_at, end_at);

-- Composite index cho promotion_targets: (promotion_id, target_type, target_id) - JOIN condition
CREATE INDEX IF NOT EXISTS idx_promotion_targets_composite ON promotion_targets(promotion_id, target_type, target_id);

-- Index cho promotion_targets.target_id (để tìm target nhanh)
CREATE INDEX IF NOT EXISTS idx_promotion_targets_target ON promotion_targets(target_type, target_id);

-- Index cho order_details.detail_id (dùng trong findTopSellingProducts)
CREATE INDEX IF NOT EXISTS idx_order_details_detail_id ON order_details(detail_id);
