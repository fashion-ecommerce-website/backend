--liquibase formatted sql

--changeset fit-team:010-seed-expanded-luxury-products
--comment Seed 120 luxury products (20 items per category) from Dior, Chanel, LV, YSL, Gucci, Prada, Hermès, Rolex, Cartier, etc.

-- ============================================================
-- 1. T-SHIRTS & POLOS (Category ID 1 - 20 Records)
-- ============================================================
INSERT INTO products (id, title, description, is_active, created_at, updated_at) VALUES
(201, 'Dior Oblique Oversized Cotton Jersey T-Shirt', 'Crafted in navy blue cotton jersey, featuring the iconic tonal Dior Oblique jacquard motif across the chest.', true, NOW(), NOW()),
(202, 'Chanel Coco Neige Embroidered Velvet T-Shirt', 'Luxury velvet T-shirt in crisp white with hand-embroidered silver CC logo on the chest pocket.', true, NOW(), NOW()),
(203, 'Louis Vuitton Monogram Embroidered Cotton Polo', 'Classic black polo shirt featuring fine pique cotton and subtle tonal Monogram embroidered collar.', true, NOW(), NOW()),
(204, 'Saint Laurent Cassandre Embroidered Tee in Off-White', 'Relaxed fit off-white organic cotton T-shirt featuring micro YSL metal badge on chest.', true, NOW(), NOW()),
(205, 'Gucci Web Stripe Collar Cotton Pique Polo', 'Navy blue cotton pique polo accented with signature green and red Web stripe trim on collar.', true, NOW(), NOW()),
(206, 'Prada Triangle Logo Re-Nylon Pocket T-Shirt', 'Black heavy cotton jersey T-shirt featuring a contrast Re-Nylon chest pocket with enamel triangle logo.', true, NOW(), NOW()),
(207, 'Hermès Sellier Embroidered Cotton Polo Shirt', 'Ultra-soft sea island cotton polo in Éoupe grey with hand-stitched leather Hermes saddle badge.', true, NOW(), NOW()),
(208, 'Burberry Vintage Check Collar Cotton Polo', 'White pique cotton polo shirt highlighted by iconic Vintage Check placket and collar.', true, NOW(), NOW()),
(209, 'Balenciaga Oversized Heavy Jersey Logo Tee', 'Washed black vintage-effect heavy cotton T-shirt featuring distressed Balenciaga logo print.', true, NOW(), NOW()),
(210, 'Jacquemus Le T-shirt Jacquemus Linen Blend', 'Beige linen-blend lightweight T-shirt with chest pocket and embroidered logo signature.', true, NOW(), NOW()),
(211, 'Celine Loose T-Shirt in Cotton Jersey', 'White loose-fit cotton T-shirt featuring classic CELINE PARIS black flocking print on chest.', true, NOW(), NOW()),
(212, 'Fendi FF Motif Sleeve Cotton Polo Shirt', 'Black short-sleeve polo shirt with jacquard FF monogram bands down the shoulders.', true, NOW(), NOW()),
(213, 'Bottega Veneta Intrecciato Pocket Cotton Tee', 'Optic white heavy cotton T-shirt accented with tonal woven leather texture chest pocket.', true, NOW(), NOW()),
(214, 'Tom Ford Silk Cotton Blend Slim Fit Polo', 'Bordeaux red knitted polo crafted from silk-cotton blend with genuine mother-of-pearl buttons.', true, NOW(), NOW()),
(215, 'Versace Medusa Head Gold Embroidered Tee', 'Royal navy crew-neck T-shirt adorned with metallic gold thread Medusa head logo print.', true, NOW(), NOW()),
(216, 'Givenchy Barbed Wire Logo Oversized T-Shirt', 'Black vintage washed cotton jersey T-shirt featuring Givenchy 4G barbed wire print.', true, NOW(), NOW()),
(217, 'Valentino VLogo Signature Cotton Jersey Tee', 'Pure white crew neck T-shirt showcasing minimalist VLogo print across the chest.', true, NOW(), NOW()),
(218, 'Loewe Anagram Embroidered Pocket T-Shirt', 'Sand beige heavy cotton T-shirt with tonal leather Anagram patch stitched on chest pocket.', true, NOW(), NOW()),
(219, 'Alexander McQueen Skull Motif Cotton Jersey Tee', 'Black cotton jersey crewneck T-shirt embellished with tonal crystal skull embroidery.', true, NOW(), NOW()),
(220, 'Dior CD Diamond Jacquard Knitted Polo', 'Sky blue lightweight wool-silk knitted polo displaying CD Diamond signature motif.', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description;

-- Map Category 1
INSERT INTO product_categories (product_id, category_id) VALUES
(201,1),(202,1),(203,1),(204,1),(205,1),(206,1),(207,1),(208,1),(209,1),(210,1),
(211,1),(212,1),(213,1),(214,1),(215,1),(216,1),(217,1),(218,1),(219,1),(220,1)
ON CONFLICT DO NOTHING;

-- Details & Images Category 1
INSERT INTO product_details (id, product_id, slug, color_id, size_id, price, quantity, is_active, created_at, updated_at) VALUES
(2001, 201, 'dior-oblique-tee-navy-m', 5, 3, 24500000.00, 20, true, NOW(), NOW()),
(2002, 202, 'chanel-velvet-tee-white-s', 2, 2, 32000000.00, 15, true, NOW(), NOW()),
(2003, 203, 'lv-monogram-polo-black-l', 1, 4, 28500000.00, 18, true, NOW(), NOW()),
(2004, 204, 'ysl-cassandre-tee-white-m', 2, 3, 16500000.00, 25, true, NOW(), NOW()),
(2005, 205, 'gucci-web-polo-navy-m', 5, 3, 22000000.00, 22, true, NOW(), NOW()),
(2006, 206, 'prada-renylon-tee-black-l', 1, 4, 26000000.00, 19, true, NOW(), NOW()),
(2007, 207, 'hermes-sellier-polo-grey-m', 3, 3, 34000000.00, 10, true, NOW(), NOW()),
(2008, 208, 'burberry-check-polo-white-l', 2, 4, 18500000.00, 30, true, NOW(), NOW()),
(2009, 209, 'balenciaga-logo-tee-black-xl', 1, 5, 19500000.00, 24, true, NOW(), NOW()),
(2010, 210, 'jacquemus-linen-tee-beige-s', 3, 2, 12500000.00, 28, true, NOW(), NOW()),
(2011, 211, 'celine-loose-tee-white-m', 2, 3, 17500000.00, 20, true, NOW(), NOW()),
(2012, 212, 'fendi-ff-polo-black-m', 1, 3, 23500000.00, 16, true, NOW(), NOW()),
(2013, 213, 'bottega-intrecciato-tee-white-l', 2, 4, 21000000.00, 14, true, NOW(), NOW()),
(2014, 214, 'tom-ford-silk-polo-red-m', 4, 3, 29000000.00, 12, true, NOW(), NOW()),
(2015, 215, 'versace-medusa-tee-navy-l', 5, 4, 15500000.00, 32, true, NOW(), NOW()),
(2016, 216, 'givenchy-barbed-tee-black-m', 1, 3, 18000000.00, 22, true, NOW(), NOW()),
(2017, 217, 'valentino-vlogo-tee-white-s', 2, 2, 16000000.00, 26, true, NOW(), NOW()),
(2018, 218, 'loewe-anagram-tee-beige-m', 3, 3, 17000000.00, 20, true, NOW(), NOW()),
(2019, 219, 'mcqueen-skull-tee-black-l', 1, 4, 14500000.00, 25, true, NOW(), NOW()),
(2020, 220, 'dior-cd-polo-blue-m', 5, 3, 31000000.00, 15, true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET price = EXCLUDED.price, quantity = EXCLUDED.quantity;

INSERT INTO images (id, url, alt, created_at) VALUES
(2201, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR5jUDTEnvFhULEc06cWI0QYApF2fGbVewfSTMPKC47Mg&s=10', 'Dior Oblique Tee', NOW()),
(2202, 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=1200&auto=format&fit=crop&q=80', 'Chanel Velvet Tee', NOW()),
(2203, 'https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=1200&auto=format&fit=crop&q=80', 'LV Polo', NOW()),
(2204, 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=1200&auto=format&fit=crop&q=80', 'YSL Cassandre Tee', NOW()),
(2205, 'https://images.unsplash.com/photo-1562157873-818bc0726f68?w=1200&auto=format&fit=crop&q=80', 'Gucci Web Polo', NOW()),
(2206, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRDmt7c5CmABAmxNZ4h7glaaVGQ7sJcwpfeligKTWqQCQ&s=10', 'Prada Re-Nylon Tee', NOW()),
(2207, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQeq-j3XKZuVuU0-AAnIsZ5W-vUMe3kV81lxEsmJmqlow&s=10', 'Hermes Polo', NOW()),
(2208, 'https://images.unsplash.com/photo-1529374255404-311a2a4f1fd9?w=1200&auto=format&fit=crop&q=80', 'Burberry Polo', NOW()),
(2209, 'https://images.unsplash.com/photo-1503341455253-b2e723bb3dbb?w=1200&auto=format&fit=crop&q=80', 'Balenciaga Tee', NOW()),
(2210, 'https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=1200&auto=format&fit=crop&q=80', 'Jacquemus Linen Tee', NOW())
ON CONFLICT (id) DO UPDATE SET url = EXCLUDED.url;

INSERT INTO product_images (id, detail_id, image_id, created_at) VALUES
(4001,2001,2201,NOW()),(4002,2002,2202,NOW()),(4003,2003,2203,NOW()),(4004,2004,2204,NOW()),
(4005,2005,2205,NOW()),(4006,2006,2206,NOW()),(4007,2007,2207,NOW()),(4008,2008,2208,NOW()),
(4009,2009,2209,NOW()),(4010,2010,2210,NOW()),(4011,2011,2201,NOW()),(4012,2012,2203,NOW()),
(4013,2013,2202,NOW()),(4014,2014,2205,NOW()),(4015,2015,2207,NOW()),(4016,2016,2209,NOW()),
(4017,2017,2204,NOW()),(4018,2018,2210,NOW()),(4019,2019,2206,NOW()),(4020,2020,2201,NOW())
ON CONFLICT (id) DO NOTHING;


-- ============================================================
-- 2. SILK SHIRTS & BLOUSES (Category ID 2 - 20 Records)
-- ============================================================
INSERT INTO products (id, title, description, is_active, created_at, updated_at) VALUES
(221, 'Saint Laurent Silk Satin Lavallière Blouse', 'Crafted in fluid black silk satin featuring a dramatic hand-tied neck scarf and mother-of-pearl buttons.', true, NOW(), NOW()),
(222, 'Dior Cannage Silk Jacquard Shirt in Blanc', 'Crisp white pure silk Jacquard shirt adorned with tonal Cannage pattern and concealed button placket.', true, NOW(), NOW()),
(223, 'Chanel Camellia Silk Twill Button-Down Shirt', 'Elegantly tailored silk twill shirt featuring delicate hand-printed camellia floral motifs in gold thread.', true, NOW(), NOW()),
(224, 'Louis Vuitton Damier Patterned Silk Shirt', 'Luxe navy silk shirt woven with subtle glossy Damier monogram checks and tailored french cuffs.', true, NOW(), NOW()),
(225, 'Gucci Flora Silk Twill Bowling Shirt', 'Short-sleeve silk bowling shirt vibrant with archival Gucci Flora botanical illustration print.', true, NOW(), NOW()),
(226, 'Prada Geometric Printed Silk Twill Shirt', 'Retro-modern regular fit shirt cut from smooth silk twill with geometric modernist prints.', true, NOW(), NOW()),
(227, 'Hermès Equestrian Horsebit Printed Silk Shirt', 'Pure mulberry silk shirt hand-printed with iconic Hermès equestrian harness illustrations.', true, NOW(), NOW()),
(228, 'Burberry Silk Crepe de Chine Oversized Shirt', 'Fluid beige silk crepe shirt accented with classic check pattern along sleeve cuffs and collar.', true, NOW(), NOW()),
(229, 'Celine Silk Jacquard Lavallière Shirt', 'Ecru silk jacquard blouse with lavallière tie, covered buttons and delicate French seams.', true, NOW(), NOW()),
(230, 'Fendi FF Karligraphy Silk Satin Shirt', 'Bordeaux red silk satin shirt embroidered with archival FF Karligraphy monogram pattern.', true, NOW(), NOW()),
(231, 'Bottega Veneta Fluid Silk Habotai Shirt', 'Minimalist black silk habotai long sleeve shirt with sharp pointed collar and hidden button closures.', true, NOW(), NOW()),
(232, 'Tom Ford Silk Velvet Evening Tuxedo Shirt', 'Opulent black silk-velvet tuxedo shirt featuring pleated bib front and onyx stud buttons.', true, NOW(), NOW()),
(233, 'Versace Barocco Print Silk Button-Up Shirt', 'Iconic Versace Barocco baroque print shirt in gold and black, crafted in 100% Italian silk.', true, NOW(), NOW()),
(234, 'Valentino Toile Iconographe Silk Shirt', 'Beige and red silk twill shirt printed with repeated VLogo pattern and buttoned cuffs.', true, NOW(), NOW()),
(235, 'Loewe Printed Silk Twill Longline Shirt', 'Artisanal longline silk shirt featuring abstract surrealist print inspired by Spanish ceramics.', true, NOW(), NOW()),
(236, 'Jacquemus Le Chemise Macho Silk Blend Shirt', 'Soft sage green silk blend shirt with exaggerated spread collar and chest flap pockets.', true, NOW(), NOW()),
(237, 'Balenciaga Printed Silk Short Sleeve Shirt', 'Black silk twill boxy shirt with hand-painted Balenciaga graffiti monogram lettering.', true, NOW(), NOW()),
(238, 'Givenchy 4G Silk Organza Layered Shirt', 'Sheer black silk organza shirt with opaque silk cuffs and embroidered 4G motif back panel.', true, NOW(), NOW()),
(239, 'Celine Silk Georgette Pleated Shirt', 'Delicate ivory silk georgette shirt with accordion pleated front bib and ruffled cuffs.', true, NOW(), NOW()),
(240, 'Dior Bandana Print Silk Twill Short Shirt', 'Navy blue short-sleeve shirt crafted in silk twill featuring Dior Bandana paisley print.', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description;

-- Map Category 2
INSERT INTO product_categories (product_id, category_id) VALUES
(221,2),(222,2),(223,2),(224,2),(225,2),(226,2),(227,2),(228,2),(229,2),(230,2),
(231,2),(232,2),(233,2),(234,2),(235,2),(236,2),(237,2),(238,2),(239,2),(240,2)
ON CONFLICT DO NOTHING;

-- Details & Images Category 2
INSERT INTO product_details (id, product_id, slug, color_id, size_id, price, quantity, is_active, created_at, updated_at) VALUES
(2021, 221, 'ysl-silk-lavalliere-black-m', 1, 3, 38500000.00, 14, true, NOW(), NOW()),
(2022, 222, 'dior-cannage-silk-white-s', 2, 2, 42000000.00, 18, true, NOW(), NOW()),
(2023, 223, 'chanel-camellia-silk-gold-m', 3, 3, 56000000.00, 10, true, NOW(), NOW()),
(2024, 224, 'lv-damier-silk-navy-l', 5, 4, 45000000.00, 12, true, NOW(), NOW()),
(2025, 225, 'gucci-flora-silk-beige-m', 3, 3, 36000000.00, 20, true, NOW(), NOW()),
(2026, 226, 'prada-geometric-silk-black-l', 1, 4, 39000000.00, 15, true, NOW(), NOW()),
(2027, 227, 'hermes-equestrian-silk-grey-m', 3, 3, 52000000.00, 8, true, NOW(), NOW()),
(2028, 228, 'burberry-check-silk-beige-l', 3, 4, 31000000.00, 22, true, NOW(), NOW()),
(2029, 229, 'celine-jacquard-silk-white-s', 2, 2, 37500000.00, 16, true, NOW(), NOW()),
(2030, 230, 'fendi-karligraphy-silk-red-m', 4, 3, 41000000.00, 14, true, NOW(), NOW()),
(2031, 231, 'bottega-habotai-silk-black-l', 1, 4, 34000000.00, 19, true, NOW(), NOW()),
(2032, 232, 'tom-ford-velvet-tuxedo-black-m', 1, 3, 58000000.00, 9, true, NOW(), NOW()),
(2033, 233, 'versace-barocco-silk-gold-l', 3, 4, 39500000.00, 25, true, NOW(), NOW()),
(2034, 234, 'valentino-toile-silk-red-m', 4, 3, 36500000.00, 17, true, NOW(), NOW()),
(2035, 235, 'loewe-ceramic-silk-beige-l', 3, 4, 33000000.00, 21, true, NOW(), NOW()),
(2036, 236, 'jacquemus-macho-silk-green-m', 6, 3, 27000000.00, 23, true, NOW(), NOW()),
(2037, 237, 'balenciaga-graffiti-silk-black-xl', 1, 5, 38000000.00, 13, true, NOW(), NOW()),
(2038, 238, 'givenchy-organza-silk-black-m', 1, 3, 43000000.00, 11, true, NOW(), NOW()),
(2039, 239, 'celine-pleated-silk-white-s', 2, 2, 46000000.00, 15, true, NOW(), NOW()),
(2040, 240, 'dior-bandana-silk-navy-m', 5, 3, 35000000.00, 18, true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET price = EXCLUDED.price, quantity = EXCLUDED.quantity;

INSERT INTO images (id, url, alt, created_at) VALUES
(2221, 'https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=1200&auto=format&fit=crop&q=80', 'Saint Laurent Silk Blouse', NOW()),
(2222, 'https://images.unsplash.com/photo-1509631179647-0177331693ae?w=1200&auto=format&fit=crop&q=80', 'Dior Silk Shirt', NOW()),
(2223, 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=1200&auto=format&fit=crop&q=80', 'Chanel Silk Shirt', NOW()),
(2224, 'https://images.unsplash.com/photo-1603252109303-2751441dd157?w=1200&auto=format&fit=crop&q=80', 'LV Damier Shirt', NOW()),
(2225, 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=1200&auto=format&fit=crop&q=80', 'Gucci Flora Shirt', NOW()),
(2226, 'https://images.unsplash.com/photo-1589310243389-96a5483213a8?w=1200&auto=format&fit=crop&q=80', 'Prada Geometric Shirt', NOW()),
(2227, 'https://images.unsplash.com/photo-1620012253295-c15cc3e65df4?w=1200&auto=format&fit=crop&q=80', 'Hermes Equestrian Shirt', NOW()),
(2228, 'https://images.unsplash.com/photo-1584273143981-41c073dfe8f8?w=1200&auto=format&fit=crop&q=80', 'Burberry Crepe Shirt', NOW())
ON CONFLICT (id) DO UPDATE SET url = EXCLUDED.url;

INSERT INTO product_images (id, detail_id, image_id, created_at) VALUES
(4021,2021,2221,NOW()),(4022,2022,2222,NOW()),(4023,2023,2223,NOW()),(4024,2024,2224,NOW()),
(4025,2025,2225,NOW()),(4026,2026,2226,NOW()),(4027,2027,2227,NOW()),(4028,2028,2228,NOW()),
(4029,2029,2221,NOW()),(4030,2030,2223,NOW()),(4031,2031,2222,NOW()),(4032,2032,2225,NOW()),
(4033,2033,2227,NOW()),(4034,2034,2228,NOW()),(4035,2035,2224,NOW()),(4036,2036,2226,NOW()),
(4037,2037,2221,NOW()),(4038,2038,2222,NOW()),(4039,2039,2223,NOW()),(4040,2040,2227,NOW())
ON CONFLICT (id) DO NOTHING;


-- ============================================================
-- 3. HAUTE COUTURE JACKETS (Category ID 3 - 20 Records)
-- ============================================================
INSERT INTO products (id, title, description, is_active, created_at, updated_at) VALUES
(241, 'Dior Tailored Double-Breasted Wool Blazer in Noir', 'Structured hourglass blazer tailored from pure black virgin wool with hand-stitched silk lapels.', true, NOW(), NOW()),
(242, 'Chanel Fantasy Tweed Belted Coat in Gold & Navy', 'Statement full-length coat in French metallic Lesage tweed, completed with leather belt and CC jewel buttons.', true, NOW(), NOW()),
(243, 'Saint Laurent Grain de Poudre Tuxedo Jacket', 'Iconic Le Smoking tuxedo jacket tailored in structured grain de poudre wool with satin peak lapels.', true, NOW(), NOW()),
(244, 'Louis Vuitton Monogram Reversible Down Jacket', 'Luxe padded puffer jacket featuring water-repellent silk monogram canvas on one side and matte black on reverse.', true, NOW(), NOW()),
(245, 'Gucci Double GG Wool Alpaca Blend Trench Coat', 'Camel-toned maxi trench coat crafted in wool-alpaca shearling blend with leather cuff straps.', true, NOW(), NOW()),
(246, 'Prada Re-Nylon Gabardine Padded Bomber Jacket', 'Oversized bomber jacket in iconic Prada Re-Nylon fabric featuring triangle logo pocket on arm.', true, NOW(), NOW()),
(247, 'Hermès Cashmere Double-Face Wrap Coat in Écoupe', 'Hand-sewn double-face cashmere wrap coat with kimono sleeves and detachable leather tie belt.', true, NOW(), NOW()),
(248, 'Burberry Kensington Heritage Trench Coat in Honey', 'Archival classic trench coat crafted in weatherproof cotton gabardine with Vintage Check lining.', true, NOW(), NOW()),
(249, 'Balenciaga Hourglass Houndstooth Wool Blazer', 'Sculptural hourglass silhouette blazer with exaggerated padded shoulders in houndstooth wool.', true, NOW(), NOW()),
(250, 'Celine Chasseur Jacket in Bouclé Tweed', 'Short boxy jacket woven in black and gold bouclé tweed featuring triomphe engraved brass buttons.', true, NOW(), NOW()),
(251, 'Fendi Shearling FF Lining Leather Jacket', 'Black supple nappa leather jacket lined with plush natural shearling embossed with FF logo.', true, NOW(), NOW()),
(252, 'Bottega Veneta Intrecciato Leather Biker Jacket', 'Soft lambskin motorcycle jacket featuring woven leather accents along shoulders and back seam.', true, NOW(), NOW()),
(253, 'Tom Ford Velvet Evening Dinner Jacket in Bordeaux', 'Luxe velvet evening jacket with silk satin shawl lapels and hand-finished boutonnière loop.', true, NOW(), NOW()),
(254, 'Versace Medusa Embellished Leather Jacket', 'Black calfskin leather jacket adorned with gold Medusa studs and heavy gold zip closures.', true, NOW(), NOW()),
(255, 'Valentino Roman Stud Wool Cashmere Cape', 'Sweeping wool-cashmere black cape embellished with hand-applied antique brass Roman Studs.', true, NOW(), NOW()),
(256, 'Loewe Anagram Belted Suede Trench Coat', 'Supple Spanish calfskin suede trench coat featuring laser-cut Anagram leather belt buckle.', true, NOW(), NOW()),
(257, 'Alexander McQueen Sculptural Tailored Leather Jacket', 'Black leather jacket with dramatic basque waist silhouette and asymmetric zip closure.', true, NOW(), NOW()),
(258, 'Jacquemus Le Blouson Bambino Leather Jacket', 'Cropped boxy leather jacket with oversized notch lapels and shearling collar trim.', true, NOW(), NOW()),
(259, 'Givenchy Wool Silk Tailored Tuxedo Blazer', 'Single-breasted wool blazer with satin collar band and satin-covered button closure.', true, NOW(), NOW()),
(260, 'Dior Cannage Quilted Leather Bomber Jacket', 'Black lambskin leather jacket quilted in classic Cannage pattern with ribbed knit waist.', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description;

-- Map Category 3
INSERT INTO product_categories (product_id, category_id) VALUES
(241,3),(242,3),(243,3),(244,3),(245,3),(246,3),(247,3),(248,3),(249,3),(250,3),
(251,3),(252,3),(253,3),(254,3),(255,3),(256,3),(257,3),(258,3),(259,3),(260,3)
ON CONFLICT DO NOTHING;

-- Details & Images Category 3
INSERT INTO product_details (id, product_id, slug, color_id, size_id, price, quantity, is_active, created_at, updated_at) VALUES
(2041, 241, 'dior-wool-blazer-black-m', 1, 3, 115000000.00, 12, true, NOW(), NOW()),
(2042, 242, 'chanel-tweed-coat-gold-l', 3, 4, 185000000.00, 8, true, NOW(), NOW()),
(2043, 243, 'ysl-grain-tuxedo-black-m', 1, 3, 95000000.00, 15, true, NOW(), NOW()),
(2044, 244, 'lv-puffer-jacket-black-l', 1, 4, 125000000.00, 10, true, NOW(), NOW()),
(2045, 245, 'gucci-cashmere-trench-beige-l', 3, 4, 138000000.00, 9, true, NOW(), NOW()),
(2046, 246, 'prada-renylon-bomber-black-m', 1, 3, 78000000.00, 18, true, NOW(), NOW()),
(2047, 247, 'hermes-cashmere-wrap-grey-m', 3, 3, 210000000.00, 5, true, NOW(), NOW()),
(2048, 248, 'burberry-kensington-trench-beige-m', 3, 3, 65000000.00, 22, true, NOW(), NOW()),
(2049, 249, 'balenciaga-hourglass-blazer-black-s', 1, 2, 88000000.00, 14, true, NOW(), NOW()),
(2050, 250, 'celine-chasseur-jacket-gold-s', 3, 2, 92000000.00, 11, true, NOW(), NOW()),
(2051, 251, 'fendi-shearling-jacket-black-l', 1, 4, 145000000.00, 7, true, NOW(), NOW()),
(2052, 252, 'bottega-intrecciato-biker-black-m', 1, 3, 135000000.00, 13, true, NOW(), NOW()),
(2053, 253, 'tom-ford-velvet-dinner-red-l', 4, 4, 108000000.00, 10, true, NOW(), NOW()),
(2054, 254, 'versace-medusa-leather-black-m', 1, 3, 98000000.00, 16, true, NOW(), NOW()),
(2055, 255, 'valentino-roman-cape-black-l', 1, 4, 110000000.00, 8, true, NOW(), NOW()),
(2056, 256, 'loewe-suede-trench-beige-m', 3, 3, 128000000.00, 12, true, NOW(), NOW()),
(2057, 257, 'mcqueen-sculptural-leather-black-s', 1, 2, 102000000.00, 14, true, NOW(), NOW()),
(2058, 258, 'jacquemus-bambino-leather-green-m', 6, 3, 62000000.00, 20, true, NOW(), NOW()),
(2059, 259, 'givenchy-tuxedo-blazer-black-l', 1, 4, 86000000.00, 15, true, NOW(), NOW()),
(2060, 260, 'dior-cannage-bomber-black-m', 1, 3, 120000000.00, 11, true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET price = EXCLUDED.price, quantity = EXCLUDED.quantity;

INSERT INTO images (id, url, alt, created_at) VALUES
(2241, 'https://images.unsplash.com/photo-1539109136881-3be0616acf4b?w=1200&auto=format&fit=crop&q=80', 'Dior Wool Blazer', NOW()),
(2242, 'data:image/jpeg;base64,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', 'Chanel Tweed Coat', NOW()),
(2243, 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=1200&auto=format&fit=crop&q=80', 'Saint Laurent Tuxedo Jacket', NOW()),
(2244, 'https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=1200&auto=format&fit=crop&q=80', 'LV Puffer Jacket', NOW()),
(2245, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ0Y3Aq4AYvJBAPeyi3BGS0ZYGmb2hjZi5ZE4vWq3Rw_g&s=10', 'Gucci Cashmere Trench', NOW()),
(2246, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQi8bhitMITa5qDarkKxAbr-ccDYdc26bOgmr9NGbtIMA&s=10', 'Prada Re-Nylon Bomber', NOW()),
(2247, 'https://tse4.mm.bing.net/th/id/OIP.OZdEj8DIE8Brc1A5r3zv2wHaJx?r=0&pid=Api&h=220&P=0', 'Hermes Cashmere Wrap', NOW()),
(2248, 'https://images.unsplash.com/photo-1512436991641-6745cdb1723f?w=1200&auto=format&fit=crop&q=80', 'Burberry Trench', NOW())
ON CONFLICT (id) DO UPDATE SET url = EXCLUDED.url;

INSERT INTO product_images (id, detail_id, image_id, created_at) VALUES
(4041,2041,2241,NOW()),(4042,2042,2242,NOW()),(4043,2043,2243,NOW()),(4044,2044,2244,NOW()),
(4045,2045,2245,NOW()),(4046,2046,2246,NOW()),(4047,2047,2247,NOW()),(4048,2048,2248,NOW()),
(4049,2049,2241,NOW()),(4050,2050,2243,NOW()),(4051,2051,2242,NOW()),(4052,2052,2245,NOW()),
(4053,2053,2247,NOW()),(4054,2054,2248,NOW()),(4055,2055,2244,NOW()),(4056,2056,2246,NOW()),
(4057,2057,2241,NOW()),(4058,2058,2242,NOW()),(4059,2059,2243,NOW()),(4060,2060,2247,NOW())
ON CONFLICT (id) DO NOTHING;


-- ============================================================
-- 4. LUXURY PANTS & SKIRTS (Category ID 4 - 20 Records)
-- ============================================================
INSERT INTO products (id, title, description, is_active, created_at, updated_at) VALUES
(261, 'Louis Vuitton Silk Twill Asymmetric Evening Gown', 'Floor-length evening dress in gold silk twill featuring fluid draped shoulder and asymmetric slit.', true, NOW(), NOW()),
(262, 'Dior Pleated Silk Chiffon Midi Skirt in Powder Pink', 'Ethereal midi skirt crafted from hand-pleated rose pink silk chiffon with grosgrain waist band.', true, NOW(), NOW()),
(263, 'Chanel Wide-Leg Wool Flannel Tailored Trousers', 'High-waisted tailored trousers in charcoal wool flannel featuring subtle front pleats and CC buttons.', true, NOW(), NOW()),
(264, 'Saint Laurent Grain de Poudre Tuxedo Pants', 'Sleek black tuxedo trousers featuring satin side stripes and pressed crease legs.', true, NOW(), NOW()),
(265, 'Gucci GG Velvet A-Line Mini Skirt', 'Rich bordeaux red velvet mini skirt woven with GG monogram motif and gold horsebit hardware.', true, NOW(), NOW()),
(266, 'Prada Re-Nylon Pleated Wrap Skirt', 'Modern wrap skirt in black Re-Nylon featuring sharp accordion pleats and enamel logo buckle.', true, NOW(), NOW()),
(267, 'Hermès Straight-Leg Leather Trousers in Noir', 'Butter-soft lambskin leather pants tailored with a straight leg and tonal saddle stitching.', true, NOW(), NOW()),
(268, 'Valentino Heavy Silk Satin Column Gown in Red', 'Breathtaking red column gown tailored in heavy silk satin with deep V-back cutout.', true, NOW(), NOW()),
(269, 'Balenciaga Sculptural Wide-Leg Tailored Pants', 'Voluminous dark navy tailored trousers featuring exaggerated wide leg silhouette and welt pockets.', true, NOW(), NOW()),
(270, 'Celine Grain de Poudre Flare Trousers', 'Tailored flare trousers in beige wool grain de poudre with sharp center creases.', true, NOW(), NOW()),
(271, 'Bottega Veneta Fluid Viscose Maxi Skirt', 'Minimalist black jersey maxi skirt featuring side slit and concealed waist band.', true, NOW(), NOW()),
(272, 'Fendi FF Jacquard Silk Midi Dress', 'Tailored midi dress in beige silk jacquard woven with allover FF monogram pattern.', true, NOW(), NOW()),
(273, 'Jacquemus La Robe Saudade Draped Dress', 'Sensual draped linen-blend halter dress in sage green with open back detail.', true, NOW(), NOW()),
(274, 'Tom Ford Grain de Poudre Slim Cigarette Pants', 'Sharp black cigarette trousers tailored in stretch wool with satin waistband trim.', true, NOW(), NOW()),
(275, 'Versace Silk Satin Printed Slip Dress', 'Gold Barocco print silk satin midi dress featuring delicate lace trim neckline.', true, NOW(), NOW()),
(276, 'Loewe Asymmetric Pleated Silk Skirt', 'Multi-layer silk crepe skirt in terracotta brown with handkerchief hemline.', true, NOW(), NOW()),
(277, 'Givenchy Tailored Wool Cutout Dress', 'Body-con black wool midi dress with strategic waist cutouts and silver zip back.', true, NOW(), NOW()),
(278, 'Alexander McQueen Corseted Taffeta Ballgown', 'Dramatic black silk taffeta ballgown featuring internal boned corset and voluminous skirt.', true, NOW(), NOW()),
(279, 'Celine Leather Mini Skirt in Supple Lambskin', 'Black nappa leather mini skirt featuring exposed brass zip closure and silk lining.', true, NOW(), NOW()),
(280, 'Dior Wool Silk Flare Tailored Pants', 'Off-white wool and silk blend trousers with elegant flare leg and side pockets.', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description;

-- Map Category 4
INSERT INTO product_categories (product_id, category_id) VALUES
(261,4),(262,4),(263,4),(264,4),(265,4),(266,4),(267,4),(268,4),(269,4),(270,4),
(271,4),(272,4),(273,4),(274,4),(275,4),(276,4),(277,4),(278,4),(279,4),(280,4)
ON CONFLICT DO NOTHING;

-- Details & Images Category 4
INSERT INTO product_details (id, product_id, slug, color_id, size_id, price, quantity, is_active, created_at, updated_at) VALUES
(2061, 261, 'lv-silk-gown-gold-s', 3, 2, 145000000.00, 7, true, NOW(), NOW()),
(2062, 262, 'dior-silk-skirt-pink-m', 3, 3, 58000000.00, 16, true, NOW(), NOW()),
(2063, 263, 'chanel-flannel-trousers-black-l', 1, 4, 62000000.00, 12, true, NOW(), NOW()),
(2064, 264, 'ysl-tuxedo-pants-black-m', 1, 3, 45000000.00, 18, true, NOW(), NOW()),
(2065, 265, 'gucci-velvet-skirt-red-s', 4, 2, 38000000.00, 20, true, NOW(), NOW()),
(2066, 266, 'prada-renylon-skirt-black-m', 1, 3, 42000000.00, 15, true, NOW(), NOW()),
(2067, 267, 'hermes-leather-pants-black-l', 1, 4, 125000000.00, 6, true, NOW(), NOW()),
(2068, 268, 'valentino-satin-gown-red-s', 4, 2, 165000000.00, 8, true, NOW(), NOW()),
(2069, 269, 'balenciaga-wide-pants-navy-l', 5, 4, 39000000.00, 14, true, NOW(), NOW()),
(2070, 270, 'celine-flare-trousers-beige-m', 3, 3, 48000000.00, 17, true, NOW(), NOW()),
(2071, 271, 'bottega-viscose-skirt-black-m', 1, 3, 34000000.00, 22, true, NOW(), NOW()),
(2072, 272, 'fendi-ff-silk-dress-beige-s', 3, 2, 68000000.00, 11, true, NOW(), NOW()),
(2073, 273, 'jacquemus-saudade-dress-green-s', 6, 2, 29000000.00, 25, true, NOW(), NOW()),
(2074, 274, 'tom-ford-cigarette-pants-black-m', 1, 3, 41000000.00, 19, true, NOW(), NOW()),
(2075, 275, 'versace-slip-dress-gold-s', 3, 2, 52000000.00, 13, true, NOW(), NOW()),
(2076, 276, 'loewe-asymmetric-skirt-beige-m', 3, 3, 46000000.00, 15, true, NOW(), NOW()),
(2077, 277, 'givenchy-cutout-dress-black-s', 1, 2, 72000000.00, 10, true, NOW(), NOW()),
(2078, 278, 'mcqueen-corset-gown-black-m', 1, 3, 210000000.00, 4, true, NOW(), NOW()),
(2079, 279, 'celine-leather-skirt-black-s', 1, 2, 55000000.00, 14, true, NOW(), NOW()),
(2080, 280, 'dior-wool-pants-white-m', 2, 3, 54000000.00, 18, true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET price = EXCLUDED.price, quantity = EXCLUDED.quantity;

INSERT INTO images (id, url, alt, created_at) VALUES
(2261, 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=1200&auto=format&fit=crop&q=80', 'LV Silk Gown', NOW()),
(2262, 'https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1200&auto=format&fit=crop&q=80', 'Dior Silk Skirt', NOW()),
(2263, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQa8dRVuzQFjughxWy0uObntIKYBDdpgpJmpEWyGSo3Sg&s=10', 'Chanel Trousers', NOW()),
(2264, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRx_jrtvD2RxzJKycbGyM8-Frfb9nEQiYqoHqMk0c_cMA&s=10', 'Saint Laurent Pants', NOW()),
(2265, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTXDq9Oma8HR9EyX2TCdxfXm1gC1C9RxnLpYm75AkJcCA&s', 'Gucci Velvet Skirt', NOW()),
(2266, 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=1200&auto=format&fit=crop&q=80', 'Prada Wrap Skirt', NOW()),
(2267, 'https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=1200&auto=format&fit=crop&q=80', 'Hermes Leather Pants', NOW()),
(2268, 'https://images.unsplash.com/photo-1563178406-4cdc2923acbc?w=1200&auto=format&fit=crop&q=80', 'Valentino Satin Gown', NOW())
ON CONFLICT (id) DO UPDATE SET url = EXCLUDED.url;

INSERT INTO product_images (id, detail_id, image_id, created_at) VALUES
(4061,2061,2261,NOW()),(4062,2062,2262,NOW()),(4063,2063,2263,NOW()),(4064,2064,2264,NOW()),
(4065,2065,2265,NOW()),(4066,2066,2266,NOW()),(4067,2067,2267,NOW()),(4068,2068,2268,NOW()),
(4069,2069,2261,NOW()),(4070,2070,2263,NOW()),(4071,2071,2262,NOW()),(4072,2072,2265,NOW()),
(4073,2073,2267,NOW()),(4074,2074,2268,NOW()),(4075,2075,2264,NOW()),(4076,2076,2266,NOW()),
(4077,2077,2261,NOW()),(4078,2078,2262,NOW()),(4079,2079,2263,NOW()),(4080,2080,2267,NOW())
ON CONFLICT (id) DO NOTHING;


-- ============================================================
-- 5. LUXURY HANDBAGS (Category ID 5 - 20 Records)
-- ============================================================
INSERT INTO products (id, title, description, is_active, created_at, updated_at) VALUES
(281, 'Chanel 22 Small Handbag in Shiny Calfskin & Gold Metal', 'Modern drawstring tote crafted in glossy black calfskin with gold-finish metal chain and CC medallion.', true, NOW(), NOW()),
(282, 'Dior Saddle Bag with Strap in Black Grained Calfskin', 'Architectural magnetic flap handbag featuring gold D-stirrup and removable oblique jacquard shoulder strap.', true, NOW(), NOW()),
(283, 'Hermès Kelly 25 Sellier Epsom Leather in Black', 'Structured handbag hand-stitched in rigid Epsom calfskin with palladium hardware and padlock key fob.', true, NOW(), NOW()),
(284, 'Louis Vuitton Capucines MM in Taurillon Leather', 'Refined top handle handbag in supple Taurillon leather with wrapped LV logo hardware.', true, NOW(), NOW()),
(285, 'Bottega Veneta Padded Cassette Bag in Intrecciato Leather', 'Iconic padded orthogonal weave crossbody bag in lambskin leather with gold triangular buckle.', true, NOW(), NOW()),
(286, 'Saint Laurent Icare Maxi Tote Bag in Quilted Lambskin', 'Generous tote bag featuring soft diamond quilting and giant bronze metal YSL emblem.', true, NOW(), NOW()),
(287, 'Gucci Jackie 1961 Small Shoulder Bag in GG Supreme', 'Reimagined hobo handbag in beige GG Supreme canvas with green and red Web stripe and piston closure.', true, NOW(), NOW()),
(288, 'Prada Cleo Brushed Leather Shoulder Bag in White', 'Sleek rounded curved shoulder bag in polished white brushed leather featuring enamel triangle logo.', true, NOW(), NOW()),
(289, 'Celine Triomphe Medium Bag in Shiny Calfskin', 'Classic flap shoulder bag in smooth black calfskin with metallic Triomphe gold clasp.', true, NOW(), NOW()),
(290, 'Fendi Peekaboo ISeeU Medium Bag in Selleria Leather', 'Iconic dual-compartment top handle bag hand-stitched in beige Roman calfskin.', true, NOW(), NOW()),
(291, 'Loewe Puzzle Small Bag in Soft Grained Calfskin', 'Innovative origami-fold geometric bag crafted in tan grained calfskin with shoulder strap.', true, NOW(), NOW()),
(292, 'Jacquemus Le Chiquito Moyen Handbag in White', 'Iconic mini structured leather handbag with exaggerated curved top handle and removable strap.', true, NOW(), NOW()),
(293, 'Balenciaga Le Cagole Small Shoulder Bag in Arena Leather', 'Y2K studded shoulder bag in crinkled Arena lambskin featuring heart mirror and card pouch.', true, NOW(), NOW()),
(294, 'Goyard Saint Louis GM Tote Bag in Goyardine Canvas', 'Ultra-lightweight reversible tote in waterproof Goyardine canvas with matching pouch.', true, NOW(), NOW()),
(295, 'Valentino Garavani Locò Small Shoulder Bag in Pink', 'Compact leather shoulder bag adorned with Swarovski crystal VLogo Signature buckle.', true, NOW(), NOW()),
(296, 'Givenchy Antigona Medium Leather Bag in Black', 'Structured trapezoid tote in smooth box leather featuring pentagonal logo patch.', true, NOW(), NOW()),
(297, 'Delvaux Brillant MM Box Calfskin Handbag', 'Belgium haute maroquinerie jewel bag handcrafted in smooth box calf leather with horseshoe buckle.', true, NOW(), NOW()),
(298, 'Moynat Rejane BB Leather Bag in Noir', 'Timeless curved handbag in supple Taurillon leather with patented jewel lock closure.', true, NOW(), NOW()),
(299, 'Bottega Veneta Jodie Small Intrecciato Bag in Sage', 'Soft hobo bag in knot-detailed Intrecciato woven nappa leather.', true, NOW(), NOW()),
(300, 'Hermès Constance 18 Epsom Leather Bag in Gold', 'Iconic shoulder bag featuring gold Epsom calfskin and oversized H metal clasp.', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description;

-- Map Category 5
INSERT INTO product_categories (product_id, category_id) VALUES
(281,5),(282,5),(283,5),(284,5),(285,5),(286,5),(287,5),(288,5),(289,5),(290,5),
(291,5),(292,5),(293,5),(294,5),(295,5),(296,5),(297,5),(298,5),(299,5),(300,5)
ON CONFLICT DO NOTHING;

-- Details & Images Category 5
INSERT INTO product_details (id, product_id, slug, color_id, size_id, price, quantity, is_active, created_at, updated_at) VALUES
(2081, 281, 'chanel-22-small-black-m', 1, 3, 168000000.00, 10, true, NOW(), NOW()),
(2082, 282, 'dior-saddle-bag-black-m', 1, 3, 110000000.00, 15, true, NOW(), NOW()),
(2083, 283, 'hermes-kelly-25-black-m', 1, 3, 520000000.00, 2, true, NOW(), NOW()),
(2084, 284, 'lv-capucines-mm-black-m', 1, 3, 195000000.00, 6, true, NOW(), NOW()),
(2085, 285, 'bottega-cassette-black-m', 1, 3, 98000000.00, 14, true, NOW(), NOW()),
(2086, 286, 'ysl-icare-maxi-tote-black-l', 1, 4, 135000000.00, 8, true, NOW(), NOW()),
(2087, 287, 'gucci-jackie-1961-beige-m', 3, 3, 78000000.00, 18, true, NOW(), NOW()),
(2088, 288, 'prada-cleo-bag-white-m', 2, 3, 82000000.00, 16, true, NOW(), NOW()),
(2089, 289, 'celine-triomphe-bag-black-m', 1, 3, 92000000.00, 12, true, NOW(), NOW()),
(2090, 290, 'fendi-peekaboo-isee-beige-m', 3, 3, 142000000.00, 9, true, NOW(), NOW()),
(2091, 291, 'loewe-puzzle-small-tan-m', 3, 3, 86000000.00, 20, true, NOW(), NOW()),
(2092, 292, 'jacquemus-chiquito-white-s', 2, 2, 24000000.00, 30, true, NOW(), NOW()),
(2093, 293, 'balenciaga-cagole-black-m', 1, 3, 72000000.00, 17, true, NOW(), NOW()),
(2094, 294, 'goyard-saint-louis-gm-black-l', 1, 4, 58000000.00, 15, true, NOW(), NOW()),
(2095, 295, 'valentino-loco-bag-pink-s', 3, 2, 69000000.00, 11, true, NOW(), NOW()),
(2096, 296, 'givenchy-antigona-medium-black-m', 1, 3, 76000000.00, 13, true, NOW(), NOW()),
(2097, 297, 'delvaux-brillant-mm-black-m', 1, 3, 240000000.00, 4, true, NOW(), NOW()),
(2098, 298, 'moynat-rejane-bb-black-m', 1, 3, 185000000.00, 5, true, NOW(), NOW()),
(2099, 299, 'bottega-jodie-small-green-m', 6, 3, 89000000.00, 14, true, NOW(), NOW()),
(2100, 300, 'hermes-constance-18-gold-m', 3, 3, 380000000.00, 3, true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET price = EXCLUDED.price, quantity = EXCLUDED.quantity;

INSERT INTO images (id, url, alt, created_at) VALUES
(2281, 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=1200&auto=format&fit=crop&q=80', 'Chanel 22 Handbag', NOW()),
(2282, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTCbtXIgh0lsDT3OviK9X92DMZC7t5DeojmT6nqujX0pA&s=10', 'Dior Saddle Bag', NOW()),
(2283, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQu83QPWeGURGHM7Hmsgfgi2V8Gs2vVkOExT_6vYRC63Q&s', 'Hermes Kelly Bag', NOW()),
(2284, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS5UWxcgRzZxr35vKW-23sA4by8sGUNSA56xVL1poVZuw&s=10', 'LV Capucines Bag', NOW()),
(2285, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQTlkiJo-oSvcbgTi1W8UJBGilHknLPR_dg9hvkm-Oc6Q&s=10', 'Bottega Cassette Bag', NOW()),
(2286, 'https://images.unsplash.com/photo-1590874103328-eac38a683ce7?w=1200&auto=format&fit=crop&q=80', 'YSL Icare Tote', NOW()),
(2287, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQpdMDmeYDKKVOMlg9zplJ3ckUzK56gSELiaulgIKkaWw&s=10', 'Gucci Jackie Bag', NOW()),
(2288, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQn1LC0zVYGXN1n8FDCWA6jneGtvnTST8A1o3OOnNriQg&s=10', 'Prada Cleo Bag', NOW())
ON CONFLICT (id) DO UPDATE SET url = EXCLUDED.url;

INSERT INTO product_images (id, detail_id, image_id, created_at) VALUES
(4081,2081,2281,NOW()),(4082,2082,2282,NOW()),(4083,2083,2283,NOW()),(4084,2084,2284,NOW()),
(4085,2085,2285,NOW()),(4086,2086,2286,NOW()),(4087,2087,2287,NOW()),(4088,2088,2288,NOW()),
(4089,2089,2281,NOW()),(4090,2090,2283,NOW()),(4091,2091,2282,NOW()),(4092,2092,2285,NOW()),
(4093,2093,2287,NOW()),(4094,2094,2288,NOW()),(4095,2095,2284,NOW()),(4096,2096,2286,NOW()),
(4097,2097,2281,NOW()),(4098,2098,2282,NOW()),(4099,2099,2283,NOW()),(4100,2100,2287,NOW())
ON CONFLICT (id) DO NOTHING;


-- ============================================================
-- 6. HIGH-END ACCESSORIES (Category ID 6 - 20 Records)
-- ============================================================
INSERT INTO products (id, title, description, is_active, created_at, updated_at) VALUES
(301, 'Rolex Submariner Date 41mm Oystersteel & Cerachrom', 'Iconic divers watch featuring black dial, Cerachrom bezel and Oystersteel bracelet.', true, NOW(), NOW()),
(302, 'Cartier Tank Must Watch 18K Yellow Gold', 'Timeless rectangular timepiece featuring quartz movement and black alligator strap.', true, NOW(), NOW()),
(303, 'Dior CD Diamond Square Sunglasses in Black', 'Bevelled black acetate sunglasses displaying signature CD Diamond metal hinges.', true, NOW(), NOW()),
(304, 'Chanel Coco Crush Ring 18K Yellow Gold with Diamonds', 'Quilted motif ring in 18k yellow gold set with brilliant-cut diamonds.', true, NOW(), NOW()),
(305, 'Louis Vuitton Initiales 40mm Reversible Leather Belt', 'Reversible belt featuring Monogram canvas on one side and black calf leather with gold LV buckle.', true, NOW(), NOW()),
(306, 'Hermès Constance Reversible Leather Belt Buckle', '38mm reversible calfskin leather belt with gold-plated metal H signature buckle.', true, NOW(), NOW()),
(307, 'Gucci Double G Leather Belt in Black', 'Smooth black leather belt completed with archival antique gold-toned Double G buckle.', true, NOW(), NOW()),
(308, 'Prada Metal Triangle Logo Saffiano Leather Wallet', 'Continental wallet in Saffiano leather featuring snap closure and enamel metal triangle logo.', true, NOW(), NOW()),
(309, 'Saint Laurent Monogram Leather Card Case', 'Quilted leather card holder with five slots and metal CASSANDRE YSL badge.', true, NOW(), NOW()),
(310, 'Tom Ford FT0237 Snowdon Square Sunglasses', 'Classic vintage-inspired dark tortoise sunglasses featuring signature T metal temples.', true, NOW(), NOW()),
(311, 'Rolex Datejust 36 Palm Dial Oystersteel & Everose Gold', 'Elegantly fluted bezel timepiece displaying olive green palm motif dial.', true, NOW(), NOW()),
(312, 'Cartier LOVE Bracelet 18K Yellow Gold', 'Iconic oval wristband decorated with functional locking screw motif.', true, NOW(), NOW()),
(313, 'Van Cleef & Arpels Vintage Alhambra Pendant 18K Gold', 'Yellow gold necklace adorned with clover mother-of-pearl pendant.', true, NOW(), NOW()),
(314, 'Bvlgari Serpenti Viper 18K Rose Gold Ring', 'Coiled snake silhouette ring set with pavé diamonds in 18k rose gold.', true, NOW(), NOW()),
(315, 'Celine Triomphe Oval Acetate Sunglasses', 'Chic oval black sunglasses with gold-finish metal Triomphe temple motifs.', true, NOW(), NOW()),
(316, 'Fendi FF Pattern Jacquard Silk Scarf', 'Square silk twill scarf woven with allover FF monogram border and hand-rolled edges.', true, NOW(), NOW()),
(317, 'Bottega Veneta Intrecciato Zip-Around Wallet', 'Long wallet crafted in signature woven lambskin leather with twelve card slots.', true, NOW(), NOW()),
(318, 'Tiffany & Co. HardWear Graduated Link Necklace', 'Bold industrial sterling silver link necklace with lobster clasp.', true, NOW(), NOW()),
(319, 'Balenciaga BB Icon Thin Leather Belt', 'Slim black calfskin belt accented with shiny gold BB interlocked logo buckle.', true, NOW(), NOW()),
(320, 'Dior Mitzah Silk Scarf in Dior Oblique Print', 'Long double-sided silk twill scarf printed with Dior Oblique signature band.', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description;

-- Map Category 6
INSERT INTO product_categories (product_id, category_id) VALUES
(301,6),(302,6),(303,6),(304,6),(305,6),(306,6),(307,6),(308,6),(309,6),(310,6),
(311,6),(312,6),(313,6),(314,6),(315,6),(316,6),(317,6),(318,6),(319,6),(320,6)
ON CONFLICT DO NOTHING;

-- Details & Images Category 6
INSERT INTO product_details (id, product_id, slug, color_id, size_id, price, quantity, is_active, created_at, updated_at) VALUES
(2101, 301, 'rolex-submariner-black-m', 1, 3, 360000000.00, 5, true, NOW(), NOW()),
(2102, 302, 'cartier-tank-gold-m', 3, 3, 290000000.00, 6, true, NOW(), NOW()),
(2103, 303, 'dior-sunglasses-black-m', 1, 3, 18500000.00, 25, true, NOW(), NOW()),
(2104, 304, 'chanel-coco-ring-gold-s', 3, 2, 89000000.00, 12, true, NOW(), NOW()),
(2105, 305, 'lv-initiales-belt-black-m', 1, 3, 19500000.00, 28, true, NOW(), NOW()),
(2106, 306, 'hermes-constance-belt-gold-m', 3, 3, 24500000.00, 20, true, NOW(), NOW()),
(2107, 307, 'gucci-double-g-belt-black-m', 1, 3, 16000000.00, 35, true, NOW(), NOW()),
(2108, 308, 'prada-saffiano-wallet-black-m', 1, 3, 21500000.00, 22, true, NOW(), NOW()),
(2109, 309, 'ysl-card-case-black-s', 1, 2, 11500000.00, 40, true, NOW(), NOW()),
(2110, 310, 'tom-ford-sunglasses-brown-m', 3, 3, 14500000.00, 30, true, NOW(), NOW()),
(2111, 311, 'rolex-datejust-green-m', 6, 3, 420000000.00, 4, true, NOW(), NOW()),
(2112, 312, 'cartier-love-bracelet-gold-m', 3, 3, 215000000.00, 8, true, NOW(), NOW()),
(2113, 313, 'vancleef-alhambra-pendant-gold-m', 3, 3, 118000000.00, 10, true, NOW(), NOW()),
(2114, 314, 'bvlgari-serpenti-ring-gold-s', 3, 2, 135000000.00, 7, true, NOW(), NOW()),
(2115, 315, 'celine-triomphe-sunglasses-black-m', 1, 3, 16500000.00, 26, true, NOW(), NOW()),
(2116, 316, 'fendi-ff-scarf-beige-m', 3, 3, 12800000.00, 20, true, NOW(), NOW()),
(2117, 317, 'bottega-intrecciato-wallet-black-m', 1, 3, 26000000.00, 18, true, NOW(), NOW()),
(2118, 318, 'tiffany-hardwear-necklace-silver-m', 2, 3, 84000000.00, 9, true, NOW(), NOW()),
(2119, 319, 'balenciaga-bb-belt-black-m', 1, 3, 13500000.00, 25, true, NOW(), NOW()),
(2120, 320, 'dior-mitzah-scarf-blue-m', 5, 3, 8800000.00, 32, true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET price = EXCLUDED.price, quantity = EXCLUDED.quantity;

INSERT INTO images (id, url, alt, created_at) VALUES
(2301, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=1200&auto=format&fit=crop&q=80', 'Rolex Submariner Watch', NOW()),
(2302, 'https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=1200&auto=format&fit=crop&q=80', 'Cartier Tank Watch', NOW()),
(2303, 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=1200&auto=format&fit=crop&q=80', 'Dior Sunglasses', NOW()),
(2304, 'https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=1200&auto=format&fit=crop&q=80', 'Chanel Gold Ring', NOW()),
(2305, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTRy4qxCFWX8Q5t5f-cebP7YAx7b13GqLIOlN5q29p3sA&s=10', 'LV Leather Belt', NOW()),
(2306, 'https://images.unsplash.com/photo-1605100804763-247f67b3557e?w=1200&auto=format&fit=crop&q=80', 'Cartier Love Bracelet', NOW()),
(2307, 'https://images.unsplash.com/photo-1622434641406-a158123450f9?w=1200&auto=format&fit=crop&q=80', 'Luxury Watch', NOW()),
(2308, 'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=1200&auto=format&fit=crop&q=80', 'Tom Ford Sunglasses', NOW())
ON CONFLICT (id) DO UPDATE SET url = EXCLUDED.url;

INSERT INTO product_images (id, detail_id, image_id, created_at) VALUES
(4101,2101,2301,NOW()),(4102,2102,2302,NOW()),(4103,2103,2303,NOW()),(4104,2104,2304,NOW()),
(4105,2105,2305,NOW()),(4106,2106,2305,NOW()),(4107,2107,2305,NOW()),(4108,2108,2306,NOW()),
(4109,2109,2306,NOW()),(4110,2110,2308,NOW()),(4111,2111,2307,NOW()),(4112,2112,2306,NOW()),
(4113,2113,2304,NOW()),(4114,2114,2304,NOW()),(4115,2115,2303,NOW()),(4116,2116,2308,NOW()),
(4117,2117,2306,NOW()),(4118,2118,2304,NOW()),(4119,2119,2305,NOW()),(4120,2120,2303,NOW())
ON CONFLICT (id) DO NOTHING;
