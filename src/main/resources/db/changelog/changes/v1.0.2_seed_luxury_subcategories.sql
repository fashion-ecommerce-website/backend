--liquibase formatted sql

--changeset fit-team:008-seed-luxury-subcategories
--comment Seed luxury child subcategories for navbar dropdown menu mega-menu
INSERT INTO categories (id, parent_id, name, slug, is_active) VALUES
-- T-Shirts & Polos (parent_id: 1)
(11, 1, 'Oversized T-Shirts', 'oversized-t-shirts', true),
(12, 1, 'Classic Polo Shirts', 'classic-polo-shirts', true),
(13, 1, 'Graphic & Logo Tees', 'graphic-logo-tees', true),
(14, 1, 'Long Sleeve Polos', 'long-sleeve-polos', true),

-- Silk Shirts & Blouses (parent_id: 2)
(21, 2, 'Silk Satin Shirts', 'silk-satin-shirts', true),
(22, 2, 'Lavallière Blouses', 'lavalliere-blouses', true),
(23, 2, 'Printed Silk Shirts', 'printed-silk-shirts', true),
(24, 2, 'Short Sleeve Silk', 'short-sleeve-silk', true),

-- Haute Couture Jackets (parent_id: 3)
(31, 3, 'Tailored Blazers', 'tailored-blazers', true),
(32, 3, 'Tweed Jackets', 'tweed-jackets', true),
(33, 3, 'Leather & Biker Jackets', 'leather-biker-jackets', true),
(34, 3, 'Trench Coats & Capes', 'trench-coats-capes', true),
(35, 3, 'Bomber & Puffer Coats', 'bomber-puffer-coats', true),

-- Luxury Pants & Skirts (parent_id: 4)
(41, 4, 'Tailored Trousers', 'tailored-trousers', true),
(42, 4, 'Evening Gowns & Dresses', 'evening-gowns-dresses', true),
(43, 4, 'Pleated Silk Skirts', 'pleated-silk-skirts', true),
(44, 4, 'Leather Pants', 'leather-pants', true),

-- Luxury Handbags (parent_id: 5)
(51, 5, 'Top Handle Bags', 'top-handle-bags', true),
(52, 5, 'Crossbody & Shoulder Bags', 'crossbody-shoulder-bags', true),
(53, 5, 'Tote & Shopper Bags', 'tote-shopper-bags', true),
(54, 5, 'Clutches & Evening Bags', 'clutches-evening-bags', true),

-- High-End Accessories (parent_id: 6)
(61, 6, 'Luxury Timepieces', 'luxury-timepieces', true),
(62, 6, 'Fine Jewelry & Rings', 'fine-jewelry-rings', true),
(63, 6, 'Designer Sunglasses', 'designer-sunglasses', true),
(64, 6, 'Leather Belts & Wallets', 'leather-belts-wallets', true),
(65, 6, 'Silk Scarves & Ties', 'silk-scarves-ties', true)
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, slug = EXCLUDED.slug, is_active = EXCLUDED.is_active;

--changeset fit-team:009-associate-products-to-subcategories
--comment Map catalog products to newly created luxury subcategories
INSERT INTO product_categories (product_id, category_id) VALUES
-- Category 1 (T-Shirts & Polos)
(201, 11), (209, 11), (216, 11),
(111, 12), (203, 12), (205, 12), (207, 12), (208, 12), (212, 12), (214, 12), (220, 12),
(202, 13), (204, 13), (206, 13), (211, 13), (215, 13), (217, 13), (218, 13), (219, 13),
(210, 14),

-- Category 2 (Silk Shirts & Blouses)
(103, 21), (221, 21), (225, 21), (229, 21), (233, 21), (237, 21),
(222, 22), (226, 22), (230, 22), (234, 22), (238, 22),
(223, 23), (227, 23), (231, 23), (235, 23), (239, 23),
(224, 24), (228, 24), (232, 24), (236, 24), (240, 24),

-- Category 3 (Haute Couture Jackets)
(101, 31), (241, 31), (246, 31), (251, 31), (256, 31),
(109, 32), (242, 32), (247, 32), (252, 32), (257, 32),
(243, 33), (248, 33), (253, 33), (258, 33),
(105, 34), (244, 34), (249, 34), (254, 34), (259, 34),
(245, 35), (250, 35), (255, 35), (260, 35),

-- Category 4 (Luxury Pants & Skirts)
(261, 41), (265, 41), (269, 41), (273, 41), (277, 41),
(104, 42), (262, 42), (266, 42), (270, 42), (274, 42), (278, 42),
(263, 43), (267, 43), (271, 43), (275, 43), (279, 43),
(264, 44), (268, 44), (272, 44), (276, 44), (280, 44),

-- Category 5 (Luxury Handbags)
(102, 51), (107, 51), (108, 51), (281, 51), (285, 51), (289, 51), (293, 51), (297, 51),
(110, 52), (282, 52), (286, 52), (290, 52), (294, 52), (298, 52),
(106, 53), (283, 53), (287, 53), (291, 53), (295, 53), (299, 53),
(284, 54), (288, 54), (292, 54), (296, 54), (300, 54),

-- Category 6 (High-End Accessories)
(301, 61), (306, 61), (311, 61), (316, 61),
(302, 62), (307, 62), (312, 62), (317, 62),
(303, 63), (308, 63), (313, 63), (318, 63),
(112, 64), (304, 64), (309, 64), (314, 64), (319, 64),
(305, 65), (310, 65), (315, 65), (320, 65)
ON CONFLICT (product_id, category_id) DO NOTHING;
