-- Xóa dữ liệu cũ (nếu muốn reset)
-- TRUNCATE TABLE public.categories RESTART IDENTITY CASCADE;

-- ======================================
-- GIÀY DÉP
-- ======================================
WITH giaydep AS (
    INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
    VALUES (NULL, 'Giày Dép', 'giay-dep', now(), true)
    RETURNING id
), sneakers AS (
    INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
    SELECT id, 'Giày Sneakers', 'giay-sneakers', now(), true FROM giaydep
    RETURNING id
), sandals AS (
    INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
    SELECT id, 'Giày Sandals', 'giay-sandals', now(), true FROM giaydep
    RETURNING id
)
INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
-- Sneakers children
SELECT s.id, v.name, v.slug, now(), true
FROM sneakers s
CROSS JOIN (VALUES
    ('Giày Mules', 'giay-mules'),
    ('Giày Bigball Chunky', 'giay-bigball-chunky'),
    ('Giày Chunky Liner', 'giay-chunky-liner'),
    ('Giày Chunky Jogger', 'giay-chunky-jogger'),
    ('Giày Chunky Classic', 'giay-chunky-classic'),
    ('Giày Chunky Low & High', 'giay-chunky-low-high')
) AS v(name, slug)

UNION ALL
-- Sandals children
SELECT sa.id, v.name, v.slug, now(), true
FROM sandals sa
CROSS JOIN (VALUES
    ('Giày Clog', 'giay-clog'),
    ('Giày Sandals', 'giay-sandals-sub'),
    ('Dép quai ngang', 'dep-quai-ngang')
) AS v(name, slug);




-- ======================================
-- QUẦN ÁO
-- ======================================
WITH quanao AS (
    INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
    VALUES (NULL, 'Quần Áo', 'quan-ao', now(), true)
    RETURNING id
), ao AS (
    INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
    SELECT id, 'Áo', 'ao', now(), true FROM quanao
    RETURNING id
), quan AS (
    INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
    SELECT id, 'Quần', 'quan', now(), true FROM quanao
    RETURNING id
), aokhoac AS (
    INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
    SELECT id, 'Áo Khoác', 'ao-khoac', now(), true FROM quanao
    RETURNING id
), nu AS (
    INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
    SELECT id, 'Dành Cho Nữ', 'danh-cho-nu', now(), true FROM quanao
    RETURNING id
)
INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)

-- Áo
SELECT a.id, v.name, v.slug, now(), true
FROM ao a
CROSS JOIN (VALUES
    ('Áo thun', 'ao-thun'),
    ('Áo polo', 'ao-polo'),
    ('Áo sơ mi', 'ao-so-mi'),
    ('Áo nỉ/ sweatshirts', 'ao-ni-sweatshirts'),
    ('Áo hoodie', 'ao-hoodie')
) AS v(name, slug)

UNION ALL
-- Quần
SELECT q.id, v.name, v.slug, now(), true
FROM quan q
CROSS JOIN (VALUES
    ('Quần jogger', 'quan-jogger'),
    ('Quần shorts', 'quan-shorts')
) AS v(name, slug)

UNION ALL
-- Áo khoác
SELECT ak.id, v.name, v.slug, now(), true
FROM aokhoac ak
CROSS JOIN (VALUES
    ('Áo khoác phao', 'ao-khoac-phao'),
    ('Áo khoác lông', 'ao-khoac-long'),
    ('Áo khoác cardigan', 'ao-khoac-cardigan'),
    ('Áo khoác phối mũ', 'ao-khoac-phoi-mu'),
    ('Áo khoác bóng chày', 'ao-khoac-bong-chay')
) AS v(name, slug)

UNION ALL
-- Nữ
SELECT n.id, v.name, v.slug, now(), true
FROM nu n
CROSS JOIN (VALUES
    ('Áo nữ', 'ao-nu'),
    ('Váy', 'vay'),
    ('Đầm', 'dam'),
    ('Quần dài', 'quan-dai'),
    ('Quần shorts nữ', 'quan-shorts-nu'),
    ('Đồ bơi', 'do-boi')
) AS v(name, slug);

-- Bộ sưu tập trong Quần Áo (KHÔNG dùng quanao nữa)
INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
SELECT id, 'MLB HEART', 'mlb-heart', now(), true 
FROM public.categories WHERE slug = 'quan-ao';

INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
SELECT id, 'MLB DENIM', 'mlb-denim', now(), true 
FROM public.categories WHERE slug = 'quan-ao';

-- ======================================
-- MŨ NÓN
-- ======================================
WITH munon AS (
    INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
    VALUES (NULL, 'Mũ Nón', 'mu-non', now(), true)
    RETURNING id
)
INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
SELECT m.id, v.name, v.slug, now(), true
FROM munon m
CROSS JOIN (VALUES
    ('Nón Bucket', 'non-bucket'),
    ('Nón Bóng Chày', 'non-bong-chay')
) AS v(name, slug);

-- ======================================
-- TÚI VÍ
-- ======================================
WITH tuivi AS (
    INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
    VALUES (NULL, 'Túi Ví', 'tui-vi', now(), true)
    RETURNING id
)
INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
SELECT t.id, v.name, v.slug, now(), true
FROM tuivi t
CROSS JOIN (VALUES
    ('Túi Tote', 'tui-tote'),
    ('Túi đeo vai', 'tui-deo-vai'),
    ('Túi đeo chéo', 'tui-deo-cheo'),
    ('Balo', 'balo')
) AS v(name, slug);

-- ======================================
-- PHỤ KIỆN
-- ======================================
WITH phukien AS (
    INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
    VALUES (NULL, 'Phụ Kiện', 'phu-kien', now(), true)
    RETURNING id
)
INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
SELECT pk.id, 'Vớ', 'vo', now(), true
FROM phukien pk;

-- ======================================
-- ƯU ĐÃI
-- ======================================
WITH uudai AS (
    INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
    VALUES (NULL, 'Ưu Đãi', 'uu-dai', now(), true)
    RETURNING id
)
INSERT INTO public.categories(parent_id, name, slug, created_at, is_active)
SELECT u.id, v.name, v.slug, now(), true
FROM uudai u
CROSS JOIN (VALUES
    ('Steal Karina Style', 'steal-karina-style'),
    ('Patch Ball Cap', 'patch-ball-cap')
) AS v(name, slug);



