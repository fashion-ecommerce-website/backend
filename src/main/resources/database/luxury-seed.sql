-- ============================================================
-- FIT LUXURY E-COMMERCE - CLEAN & SEED DATABASE SCRIPT
-- ============================================================

-- Clean existing catalog data and broken image links
TRUNCATE TABLE product_images, product_details, product_categories, products, images, categories, colors, sizes CASCADE;

-- 1. Seed Categories
INSERT INTO categories (id, name, slug, parent_id, is_active, created_at) VALUES
(1, 'T-Shirts & Polos', 't-shirts-polos', NULL, true, NOW()),
(2, 'Silk Shirts & Blouses', 'silk-shirts-blouses', NULL, true, NOW()),
(3, 'Haute Couture Jackets', 'haute-couture-jackets', NULL, true, NOW()),
(4, 'Luxury Pants & Skirts', 'luxury-pants-skirts', NULL, true, NOW()),
(5, 'Luxury Handbags', 'luxury-handbags', NULL, true, NOW()),
(6, 'High-End Accessories', 'high-end-accessories', NULL, true, NOW()),
-- Subcategories (parent_id 1 to 6)
(11, 'Oversized T-Shirts', 'oversized-t-shirts', 1, true, NOW()),
(12, 'Classic Polo Shirts', 'classic-polo-shirts', 1, true, NOW()),
(13, 'Graphic & Logo Tees', 'graphic-logo-tees', 1, true, NOW()),
(14, 'Long Sleeve Polos', 'long-sleeve-polos', 1, true, NOW()),
(21, 'Silk Satin Shirts', 'silk-satin-shirts', 2, true, NOW()),
(22, 'Lavallière Blouses', 'lavalliere-blouses', 2, true, NOW()),
(23, 'Printed Silk Shirts', 'printed-silk-shirts', 2, true, NOW()),
(24, 'Short Sleeve Silk', 'short-sleeve-silk', 2, true, NOW()),
(31, 'Tailored Blazers', 'tailored-blazers', 3, true, NOW()),
(32, 'Tweed Jackets', 'tweed-jackets', 3, true, NOW()),
(33, 'Leather & Biker Jackets', 'leather-biker-jackets', 3, true, NOW()),
(34, 'Trench Coats & Capes', 'trench-coats-capes', 3, true, NOW()),
(35, 'Bomber & Puffer Coats', 'bomber-puffer-coats', 3, true, NOW()),
(41, 'Tailored Trousers', 'tailored-trousers', 4, true, NOW()),
(42, 'Evening Gowns & Dresses', 'evening-gowns-dresses', 4, true, NOW()),
(43, 'Pleated Silk Skirts', 'pleated-silk-skirts', 4, true, NOW()),
(44, 'Leather Pants', 'leather-pants', 4, true, NOW()),
(51, 'Top Handle Bags', 'top-handle-bags', 5, true, NOW()),
(52, 'Crossbody & Shoulder Bags', 'crossbody-shoulder-bags', 5, true, NOW()),
(53, 'Tote & Shopper Bags', 'tote-shopper-bags', 5, true, NOW()),
(54, 'Clutches & Evening Bags', 'clutches-evening-bags', 5, true, NOW()),
(61, 'Luxury Timepieces', 'luxury-timepieces', 6, true, NOW()),
(62, 'Fine Jewelry & Rings', 'fine-jewelry-rings', 6, true, NOW()),
(63, 'Designer Sunglasses', 'designer-sunglasses', 6, true, NOW()),
(64, 'Leather Belts & Wallets', 'leather-belts-wallets', 6, true, NOW()),
(65, 'Silk Scarves & Ties', 'silk-scarves-ties', 6, true, NOW())
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, slug = EXCLUDED.slug, is_active = EXCLUDED.is_active;

-- 2. Seed Colors
INSERT INTO colors (id, name, hex, is_active) VALUES
(1, 'Noir Black', '#0B0B0B', true),
(2, 'Blanc White', '#FFFFFF', true),
(3, 'Beige Gold', '#F5F5DC', true),
(4, 'Bordeaux Red', '#800020', true),
(5, 'Royal Navy', '#0B1D3A', true),
(6, 'Emerald Green', '#004B23', true)
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, hex = EXCLUDED.hex, is_active = EXCLUDED.is_active;

-- 3. Seed Sizes
INSERT INTO sizes (id, code, label, is_active) VALUES
(1, 'XS', 'XS', true),
(2, 'S', 'S', true),
(3, 'M', 'M', true),
(4, 'L', 'L', true),
(5, 'XL', 'XL', true)
ON CONFLICT (id) DO UPDATE SET code = EXCLUDED.code, label = EXCLUDED.label, is_active = EXCLUDED.is_active;

-- 4. Seed Products
INSERT INTO products (id, title, description, is_active, created_at, updated_at) VALUES
(101, 'Dior Bar Jacket in Black Houndstooth Wool', 'An iconic piece from the House of Dior, crafted in black and white houndstooth virgin wool with cinched waist and hand-stitched silk lining.', true, NOW(), NOW()),
(102, 'Chanel Classic Double Flap Bag in Quilted Lambskin', 'Timeless elegance by Chanel, featuring diamond-quilted black lambskin leather, gold-tone metal hardware, and signature double C clasp.', true, NOW(), NOW()),
(103, 'Saint Laurent Monogram Silk Satin Shirt in Noir', 'Tailored from 100% mulberry silk satin, featuring a sharp lavallière collar, mother-of-pearl buttons, and subtle YSL tonal embroidered monogram.', true, NOW(), NOW()),
(104, 'Louis Vuitton Damier Silk Twill Evening Dress', 'Crafted from fluid silk twill adorned with the iconic Damier motif, cinched with a leather belt and gold LV engraved buckle.', true, NOW(), NOW()),
(105, 'Gucci GG Cashmere Double-Breasted Coat', 'A luxurious double-breasted coat in camel GG wool-cashmere blend, highlighted by horn buttons and GG jacquard lining.', true, NOW(), NOW()),
(106, 'Prada Re-Nylon & Saffiano Leather Tote Bag', 'Modern minimalist tote crafted from industrial Re-Nylon and Saffiano leather trim, featuring the enamel triangle logo.', true, NOW(), NOW()),
(107, 'Hermès Birkin 25 Gold Togo Leather with Gold Hardware', 'The pinnacle of luxury craftsmanship, hand-stitched Togo calfskin in warm Gold hue with 18k gold-plated hardware.', true, NOW(), NOW()),
(108, 'Dior Lady Dior Medium Bag in Latte Cannage Lambskin', 'Epitome of Dior elegance in latte cannage lambskin with pale gold-finish metal D.I.O.R. charms.', true, NOW(), NOW()),
(109, 'Chanel Tweed Short Jacket in Ecru & Gold', 'Signature Lesage woven tweed in ecru and metallic gold thread, adorned with lion head jewel buttons.', true, NOW(), NOW()),
(110, 'Saint Laurent Le 5 à 7 Shoulder Bag in Smooth Leather', 'Sleek hobo silhouette with tab closure featuring the metal CASSANDRE YSL hook in bronze-toned metal.', true, NOW(), NOW()),
(111, 'Gucci Silk Jacquard Oversized Polo Shirt', 'Soft fluid silk jacquard shirt featuring subtle GG tone-on-tone pattern and mother-of-pearl buttons.', true, NOW(), NOW()),
(112, 'Dior Oblique High-Top Luxury Sneakers', 'Standout sneaker design featuring transparent paneling overlaid on black and white Dior Oblique canvas.', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description, is_active = EXCLUDED.is_active;

-- 5. Seed Product Categories Mapping
INSERT INTO product_categories (product_id, category_id) VALUES
(101, 3), (102, 5), (103, 2), (104, 4),
(105, 3), (106, 5), (107, 5), (108, 5),
(109, 3), (110, 5), (111, 1), (112, 6)
ON CONFLICT DO NOTHING;

-- 6. Seed Product Details
INSERT INTO product_details (id, product_id, slug, color_id, size_id, price, quantity, is_active, created_at, updated_at) VALUES
(1001, 101, 'dior-bar-jacket-black-m', 1, 3, 115000000.00, 15, true, NOW(), NOW()),
(1002, 101, 'dior-bar-jacket-white-s', 2, 2, 115000000.00, 10, true, NOW(), NOW()),
(1003, 102, 'chanel-classic-flap-black-m', 1, 3, 265000000.00, 8, true, NOW(), NOW()),
(1004, 103, 'ysl-silk-shirt-black-l', 1, 4, 38500000.00, 25, true, NOW(), NOW()),
(1005, 104, 'lv-damier-dress-beige-s', 3, 2, 85000000.00, 12, true, NOW(), NOW()),
(1006, 105, 'gucci-cashmere-coat-beige-l', 3, 4, 128000000.00, 9, true, NOW(), NOW()),
(1007, 106, 'prada-renylon-tote-black-m', 1, 3, 68000000.00, 20, true, NOW(), NOW()),
(1008, 107, 'hermes-birkin-25-gold-m', 3, 3, 480000000.00, 3, true, NOW(), NOW()),
(1009, 108, 'lady-dior-medium-latte-m', 3, 3, 165000000.00, 7, true, NOW(), NOW()),
(1010, 109, 'chanel-tweed-jacket-gold-s', 3, 2, 145000000.00, 11, true, NOW(), NOW()),
(1011, 110, 'ysl-le-5-7-black-m', 1, 3, 58000000.00, 18, true, NOW(), NOW()),
(1012, 111, 'gucci-silk-polo-navy-m', 5, 3, 32000000.00, 30, true, NOW(), NOW()),
(1013, 112, 'dior-oblique-sneakers-white-l', 2, 4, 29500000.00, 22, true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET price = EXCLUDED.price, quantity = EXCLUDED.quantity, is_active = EXCLUDED.is_active;

-- 7. Seed Images
INSERT INTO images (id, url, alt, created_at) VALUES
(2001, 'https://images.unsplash.com/photo-1539109136881-3be0616acf4b?w=1200&auto=format&fit=crop&q=80', 'Dior Bar Jacket Haute Couture', NOW()),
(2002, 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=1200&auto=format&fit=crop&q=80', 'Chanel Classic Flap Luxury Bag', NOW()),
(2003, 'https://images.unsplash.com/photo-1509631179647-0177331693ae?w=1200&auto=format&fit=crop&q=80', 'Saint Laurent Silk Shirt', NOW()),
(2004, 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=1200&auto=format&fit=crop&q=80', 'Louis Vuitton Silk Twill Dress', NOW()),
(2005, 'data:image/jpeg;base64,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', 'Gucci Cashmere Coat', NOW()),
(2006, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQuKPD9J7K3wliHQv3-FxDtm4mdL6YEyImhBKF-bUd6Gw&s=10', 'Prada Re-Nylon Tote', NOW()),
(2007, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTn7oZWeTjSfw7yFGMW3hmYeHU5-O6ICsP2nS4Ct3NJGQ&s=10', 'Hermès Birkin Bag Gold', NOW()),
(2008, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS5UWxcgRzZxr35vKW-23sA4by8sGUNSA56xVL1poVZuw&s=10', 'Lady Dior Cannage Bag', NOW()),
(2009, 'https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=1200&auto=format&fit=crop&q=80', 'Chanel Woven Tweed Jacket', NOW()),
(2010, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ6AiCui0q7EM3p9NIlcHvP70Jon8haQ6p9dlWaXLjt6Q&s=10', 'Saint Laurent Le 5 à 7 Bag', NOW()),
(2011, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTefJ-CaOMgdbt1y7hLeABhD4yBQD7Su_GIKbchyxHazw&s', 'Gucci Silk Jacquard Polo', NOW()),
(2012, 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=1200&auto=format&fit=crop&q=80', 'Dior Oblique Sneakers', NOW())
ON CONFLICT (id) DO UPDATE SET url = EXCLUDED.url;

-- 8. Seed Product Detail Images Mapping
INSERT INTO product_images (id, detail_id, image_id, created_at) VALUES
(3001, 1001, 2001, NOW()), (3002, 1002, 2001, NOW()), (3003, 1003, 2002, NOW()),
(3004, 1004, 2003, NOW()), (3005, 1005, 2004, NOW()), (3006, 1006, 2005, NOW()),
(3007, 1007, 2006, NOW()), (3008, 1008, 2007, NOW()), (3009, 1009, 2008, NOW()),
(3010, 1010, 2009, NOW()), (3011, 1011, 2010, NOW()), (3012, 1012, 2011, NOW()),
(3013, 1013, 2012, NOW())
ON CONFLICT (id) DO NOTHING;
