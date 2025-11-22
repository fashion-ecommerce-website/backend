TRUNCATE TABLE interactions RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_roles RESTART IDENTITY CASCADE;
TRUNCATE TABLE users RESTART IDENTITY CASCADE;
TRUNCATE TABLE roles RESTART IDENTITY CASCADE;

BEGIN;

-- =========================================
-- 0) THAM SỐ DÙNG XUYÊN SUỐT
-- =========================================
DROP TABLE IF EXISTS _params;
CREATE TEMP TABLE _params (
                              target_users      int,
                              target_pairs      int,
                              same_group_prob   float8,
                              zipf_s            float8
);
-- 1,000 users; 12,000 cặp; 92% đúng gu; Zipf^1.1
INSERT INTO _params VALUES (1000, 12000, 0.92, 1.10);

-- =========================================
-- 1) ROLES & ADMIN (nếu thiếu)
-- =========================================
INSERT INTO roles (role_name, is_active, created_at, updated_at)
SELECT 'USER', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'USER')
LIMIT 1;

INSERT INTO roles (role_name, is_active, created_at, updated_at)
SELECT 'ADMIN', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'ADMIN')
LIMIT 1;

-- =========================================
-- 2) BỔ SUNG USER CHO ĐỦ 1,000
-- =========================================
WITH p AS (SELECT target_users FROM _params)
INSERT INTO users (email, password, username, dob, phone, avatar_url,
                   is_active, email_verified, phone_verified, created_at, updated_at)
SELECT
    'user' || gs || '@test.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO',
    'user' || gs,
    ('1990-01-01'::date + (random() * INTERVAL '30 years')::interval)::date,
    '0' || LPAD(FLOOR(random() * 999999999)::TEXT, 9, '0'),
    'https://example.com/avatars/user' || gs || '.jpg',
    TRUE, (random() < 0.8), (random() < 0.7),
    NOW() - (random() * INTERVAL '365 days'),
    NOW() - (random() * INTERVAL '365 days')
FROM p
         CROSS JOIN LATERAL generate_series(
        1,
        GREATEST((SELECT target_users FROM _params) - (SELECT COUNT(*) FROM users), 0)
                            ) gs;

-- Gán USER role cho user chưa có
INSERT INTO user_roles (user_id, role_id, is_active, created_at, updated_at)
SELECT
    u.id,
    (SELECT id FROM roles WHERE role_name = 'USER' LIMIT 1),
    true,
    COALESCE(u.created_at, NOW()),
    COALESCE(u.created_at, NOW())
FROM users u
         LEFT JOIN user_roles ur ON ur.user_id = u.id
WHERE ur.user_id IS NULL
ON CONFLICT DO NOTHING;

-- =========================================
-- 3) NHÓM SẢN PHẨM THẬT THEO "GU"
-- =========================================
DROP TABLE IF EXISTS temp_product_groups;
CREATE TEMP TABLE temp_product_groups AS
SELECT
    p.id AS product_id,
    CASE
        WHEN p.title ILIKE 'Túi%' THEN 'BAG'
        WHEN p.title ILIKE 'Nón bóng chày%' THEN 'CAP'
        WHEN p.title ILIKE 'Nón bucket%' THEN 'BUCKET'
        WHEN p.title ILIKE 'Quần short%' OR p.title ILIKE '%Quần jogger%' THEN 'BOTTOM'
        WHEN p.title ILIKE 'Áo sweatshirt%' OR p.title ILIKE '%Áo sơ mi%'
            OR p.title ILIKE 'Áo polo%' OR p.title ILIKE '%Áo thun%' THEN 'TOP'
        ELSE 'MISC'
        END AS g
FROM products p
WHERE p.is_active = TRUE;

UPDATE temp_product_groups SET g = 'MISC' WHERE g IS NULL;

-- =========================================
-- 4) GÁN "GU" CHO USER (5 nhóm chia đều)
-- =========================================
DROP TABLE IF EXISTS temp_user_groups;
CREATE TEMP TABLE temp_user_groups AS
WITH base AS (
    SELECT id AS user_id, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM users
    WHERE is_active = TRUE
    ORDER BY id
    LIMIT (SELECT target_users FROM _params)
)
SELECT
    user_id,
    CASE
        WHEN (rn % 5) = 1 THEN 'BAG'
        WHEN (rn % 5) = 2 THEN 'CAP'
        WHEN (rn % 5) = 3 THEN 'BUCKET'
        WHEN (rn % 5) = 4 THEN 'BOTTOM'
        ELSE 'TOP'
        END AS g
FROM base;

-- =========================================
-- 5) ZIPF WEIGHTS CHO MỖI NHÓM (tạo item hot)
-- =========================================
DROP TABLE IF EXISTS _pw;
CREATE TEMP TABLE _pw AS
WITH ranked AS (
    -- xếp hạng trong nhóm bằng id (ổn định). Nếu muốn đổi, thay ORDER BY md5(product_id::text)
    SELECT product_id, g,
           ROW_NUMBER() OVER (PARTITION BY g ORDER BY product_id) AS rnk
    FROM temp_product_groups
)
SELECT
    product_id, g,
    (1.0 / (rnk ^ (SELECT zipf_s FROM _params)))::float8 AS w
FROM ranked;

DROP TABLE IF EXISTS _pw_cum;
CREATE TEMP TABLE _pw_cum AS
SELECT
    g,
    product_id,
    w,
    SUM(w) OVER (PARTITION BY g) AS w_sum,
    SUM(w) OVER (PARTITION BY g ORDER BY w DESC, product_id) AS cum_w
FROM (
         SELECT g, product_id, w
         FROM _pw
         ORDER BY g, w DESC, product_id
     ) s;

-- =========================================
-- 6) QUOTA ~12 CẶP/USER (đảm bảo phủ đều tất cả user)
-- =========================================
DROP TABLE IF EXISTS _user_quota;
CREATE TEMP TABLE _user_quota AS
WITH totals AS (
    SELECT
        (SELECT target_users FROM _params)  AS U,
        (SELECT target_pairs FROM _params)  AS P
),
     base AS (
         SELECT ug.user_id, ROW_NUMBER() OVER (ORDER BY ug.user_id) AS rn
         FROM temp_user_groups ug
     ),
     q AS (
         SELECT
             b.user_id,
             (P / U)::int AS base_k,
             (P % U)::int AS remainder,
             b.rn
         FROM base b CROSS JOIN totals
     )
SELECT
    user_id,
    (base_k + CASE WHEN rn <= remainder THEN 1 ELSE 0 END)::int AS k
FROM q;

-- =========================================
-- 7) SINH CẶP THEO QUOTA + ZIPF + NHIỄU  (PHIÊN BẢN FIXED)
-- =========================================
TRUNCATE TABLE interactions RESTART IDENTITY;
CREATE UNIQUE INDEX IF NOT EXISTS idx_interaction_unique
    ON interactions(user_id, product_id, action_type);

DROP TABLE IF EXISTS _picked_attempts;
CREATE TEMP TABLE _picked_attempts AS
SELECT
    uq.user_id,
    sel.pick_g,
    pick.product_id,
    random() AS r_behav,
    (NOW() - ((floor(random()*90)::int) || ' days')::interval
        - ((floor(random()*24)::int) || ' hours')::interval
        - ((floor(random()*60)::int) || ' minutes')::interval) AS created_at
FROM _user_quota uq
         JOIN temp_user_groups ug ON ug.user_id = uq.user_id
         JOIN LATERAL (
    SELECT CASE
               WHEN random() < (SELECT same_group_prob FROM _params) THEN ug.g
               ELSE (
                   SELECT g FROM (SELECT DISTINCT g FROM temp_product_groups WHERE g <> ug.g) AS gs
                   ORDER BY random() LIMIT 1
               )
               END AS pick_g
    ) sel ON TRUE
         JOIN LATERAL (
    SELECT pw.product_id
    FROM _pw_cum pw
    WHERE pw.g = sel.pick_g
      AND pw.cum_w >= (random() * pw.w_sum)
    ORDER BY pw.cum_w
    LIMIT 1
    ) pick ON TRUE
-- tạo dư nhiều attempt
         JOIN LATERAL generate_series(1, uq.k * 10) gs(n) ON TRUE;

-- --- Khử trùng per-user ---
DROP TABLE IF EXISTS _pairs12k;
CREATE TEMP TABLE _pairs12k AS
WITH dedup AS (
    SELECT DISTINCT ON (user_id, product_id)
        user_id, product_id, r_behav, created_at
    FROM _picked_attempts
    ORDER BY user_id, product_id, random()
),
     ranked AS (
         SELECT d.*, ROW_NUMBER() OVER (PARTITION BY d.user_id ORDER BY random()) AS rn
         FROM dedup d
     )
SELECT r.user_id, r.product_id, r.r_behav, r.created_at
FROM ranked r
         JOIN _user_quota uq ON uq.user_id = r.user_id
WHERE r.rn <= uq.k;

-- --- BÙ THIẾU: user nào chưa đủ quota thì thêm random ---
WITH cur AS (
    SELECT user_id, COUNT(*) AS have_k
    FROM _pairs12k
    GROUP BY user_id
),
     need AS (
         SELECT uq.user_id, GREATEST(uq.k - COALESCE(c.have_k,0), 0) AS missing
         FROM _user_quota uq
                  LEFT JOIN cur c ON c.user_id = uq.user_id
         WHERE GREATEST(uq.k - COALESCE(c.have_k,0), 0) > 0
     )
INSERT INTO _pairs12k (user_id, product_id, r_behav, created_at)
SELECT n.user_id,
       p.product_id,
       random() AS r_behav,
       NOW() - random()*interval '60 days'
FROM need n
         JOIN LATERAL (
    SELECT product_id
    FROM temp_product_groups
    ORDER BY random()
    LIMIT (n.missing + 2)  -- bù thêm 1–2 để tránh trùng
    ) p ON TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM _pairs12k x
    WHERE x.user_id = n.user_id AND x.product_id = p.product_id
);

-- --- Map funnel & insert ---
WITH sampled AS (
    SELECT
        user_id,
        product_id,
        CASE
            WHEN r_behav < 0.55 THEN 'VIEW'
            WHEN r_behav < 0.85 THEN 'LIKE'
            WHEN r_behav < 0.95 THEN 'ADD_TO_CART'
            ELSE 'PURCHASE'
            END AS action_type,
        CASE
            WHEN r_behav < 0.55 THEN 1 + FLOOR(random()*2)::int
            WHEN r_behav < 0.85 THEN 2
            WHEN r_behav < 0.95 THEN 3
            ELSE 5
            END AS cnt,
        created_at
    FROM _pairs12k
)
INSERT INTO interactions (user_id, product_id, action_type, count, created_at)
SELECT user_id, product_id, action_type, cnt, created_at
FROM sampled
ON CONFLICT (user_id, product_id, action_type) DO UPDATE
    SET count = interactions.count + EXCLUDED.count,
        created_at = GREATEST(interactions.created_at, EXCLUDED.created_at);
COMMIT;

-- =========================================
-- 8) THỐNG KÊ NHANH
-- =========================================
SELECT '=== SUMMARY ===' AS info;

-- Quy mô
SELECT 'Users (active)' AS metric, COUNT(*)::text FROM users WHERE is_active = TRUE
UNION ALL SELECT 'Products (active)', COUNT(*)::text FROM products WHERE is_active = TRUE
UNION ALL SELECT 'Interactions (rows)', COUNT(*)::text FROM interactions;

-- Tỉ lệ hành vi
SELECT action_type, COUNT(*) AS rows
FROM interactions
GROUP BY action_type
ORDER BY rows DESC;

-- Số user & item phủ
SELECT COUNT(DISTINCT user_id) AS users_in_inters,
       COUNT(DISTINCT product_id) AS items_in_inters
FROM interactions;

-- Trung bình số item mỗi user
SELECT AVG(cnt)::numeric(10,2) AS avg_items_per_user FROM (
                                                              SELECT user_id, COUNT(DISTINCT product_id) AS cnt
                                                              FROM interactions GROUP BY user_id
                                                          ) t;

-- Phủ item: min/avg/max users mỗi item
WITH t AS (
    SELECT product_id, COUNT(DISTINCT user_id) AS users_per_item
    FROM interactions GROUP BY product_id
)
SELECT MIN(users_per_item) AS min_users_per_item,
       AVG(users_per_item)::numeric(10,2) AS avg_users_per_item,
       MAX(users_per_item) AS max_users_per_item
FROM t;

-- Top-10 item có nhiều user nhất (để thấy “hot”)
WITH t AS (
    SELECT product_id, COUNT(DISTINCT user_id) AS users_per_item
    FROM interactions GROUP BY product_id
)
SELECT p.id, p.title, t.users_per_item
FROM t JOIN products p ON p.id = t.product_id
ORDER BY t.users_per_item DESC
LIMIT 10;
