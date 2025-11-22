---- insert data to table category
INSERT INTO categories("name", parent_id, slug, "is_active" ) VALUES ('Áo polo', 1, 'ao-polo', true);
INSERT INTO categories("name", parent_id, slug, "is_active" ) VALUES ('Áo sơ mi', 1, 'ao-somi', true);
INSERT INTO categories("name", parent_id, slug, "is_active" ) VALUES ('Áo hoodie', 1, 'ao-hoodie', true);

INSERT INTO categories("name", slug, "is_active" ) VALUES ('Quần', 'quan', true);
INSERT INTO categories("name", parent_id, slug, "is_active" ) VALUES ('Quần jogger', 6, 'quan-jogger', true);
INSERT INTO categories("name", parent_id, slug, "is_active" ) VALUES ('Quần shorts', 6, 'quan-short', true);

INSERT INTO categories("name", slug, "is_active" ) VALUES ('Mũ nón', 'mu-non', true);
INSERT INTO categories("name", parent_id, slug, "is_active" ) VALUES ('Nón bucket', 9, 'non-bucket', true);
INSERT INTO categories("name", parent_id, slug, "is_active" ) VALUES ('Nón bóng chày', 9, 'non-bong-chay', true);

INSERT INTO categories("name", slug, "is_active" ) VALUES ('Túi ví', 'tui-vi', true);
INSERT INTO categories("name", parent_id, slug, "is_active" ) VALUES ('Túi tote', 10, 'tui-tote', true);
INSERT INTO categories("name", parent_id, slug, "is_active" ) VALUES ('Túi đeo vai', 10, 'tui-deo-vai', true);
INSERT INTO categories("name", parent_id, slug, "is_active" ) VALUES ('Túi đeo chéo', 10, 'tui-deo-cheo', true);


---- insert data to table product
INSERT INTO products(title, is_active ) VALUES
                                            ('Áo polo unisex Varsity Sportive Over Fit', TRUE),  --9
                                            ('Áo polo unisex Varsity Stripe Overfit', TRUE),  --2
                                            ('Áo polo unisex tay ngắn Dia Monogram Jacquard Color', TRUE),  --10
                                            ('Áo polo unisex tay ngắn Basic Coopers Logo Overfit', TRUE),    --11
                                            ('Áo polo unisex Basic Washing Over Fit', TRUE),			 --12
                                            ('Áo polo unisex Basic Block Comfortable', TRUE),		 --13
                                            ('Áo polo unisex Basic Comfortable Fit', TRUE);		 --14

--Áo polo unisex Varsity Sportive Over Fit (black)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bks_3apqv0153_1_892ed2265b744de683db7ce579968bbb_846cc4bfb0a34f63a3ab51944c8d6548_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (9, 1, 1, 1890000, 12, '', true);
INSERT INTO product_images (detail_id, image_id) VALUES (96, 63);
INSERT INTO product_categories (product_id , category_id) VALUES (9, 3);

-- 1) Áo polo unisex Varsity Stripe Overfit (dark blue)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/43nys_3apqv0253_1_cba04481b9c34700969a1daeb5d1ce84_58fa4983f70a4ed78e19d71efac0d7f1_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (10, 3, 1, 1890000, 12, 'ao-polo-unisex-varsity-stripe-overfit-dark-blue-97', true);
INSERT INTO product_images (detail_id, image_id) VALUES (97, 64);
INSERT INTO product_categories (product_id , category_id) VALUES (10, 3);

-- 2) Áo polo unisex tay ngắn Dia Monogram Jacquard Color (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50ivs_3apqm0253_1_22c0d2663fa1416cb071a5d8fc1c4081_f13ac172fa0e49078c21e34fb0bb972b_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (11, 2, 1, 1890000, 12, 'ao-polo-unisex-tay-ngan-dia-monogram-jacquard-color-white-98', true);
INSERT INTO product_images (detail_id, image_id) VALUES (98, 65);
INSERT INTO product_categories (product_id , category_id) VALUES (11, 3);

-- 3) Áo polo unisex tay ngắn Basic Coopers Logo Overfit (yellow)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bgl_3apqb0353_1_6eaed2c29e7942b2b3d42037a7a9675b_66664b5dce524b16943426b09db64740_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (12, 9, 1, 1890000, 12, 'ao-polo-unisex-tay-ngan-basic-coopers-logo-overfit-yellow-99', true);
INSERT INTO product_images (detail_id, image_id) VALUES (99, 66);
INSERT INTO product_categories (product_id , category_id) VALUES (12, 3);

-- 4) Áo polo unisex Basic Washing Over Fit (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50crd_3apqb0453_1_7f40f807237e4257aa97db4b635950f6_543a05f5bf7a4562a0ff852ad67f76b6_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (13, 2, 1, 1890000, 12, 'ao-polo-unisex-basic-washing-over-fit-white-100', true);
INSERT INTO product_images (detail_id, image_id) VALUES (100, 67);
INSERT INTO product_categories (product_id , category_id) VALUES (13, 3);

-- 5) Áo polo unisex Basic Block Comfortable (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50ivs_3apqb0253_1_e1c2f83587b74812bb7cd3348477fa64_281bf881d0d84572af3084b77c0795df_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (14, 2, 1, 1890000, 12, 'ao-polo-unisex-basic-block-comfortable-white-101', true);
INSERT INTO product_images (detail_id, image_id) VALUES (101, 68);
INSERT INTO product_categories (product_id , category_id) VALUES (14, 3);

-- 6) Áo polo unisex Basic Comfortable Fit (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50ivs_3apqb0153_1_5eb589b94a54432c9dbe66468d39cc00_0ff6758a12184bb48a85f46c4e9d35c5_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (15, 2, 1, 1890000, 12, 'ao-polo-unisex-basic-comfortable-fit-white-102', true);
INSERT INTO product_images (detail_id, image_id) VALUES (102, 69);
INSERT INTO product_categories (product_id , category_id) VALUES (15, 3);


------------------------- AO SO MI --------------------------
-------------------------------------------------------------
---- insert data to table product
INSERT INTO products(title, is_active ) VALUES
                                            ('Áo sơ mi denim unisex tay dài thêu logo thời trang', TRUE),
                                            ('Áo sơ mi denim unisex tay dài phối họa tiết', TRUE),
                                            ('Áo sơ mi unisex Varsity Vintage Cool', TRUE),
                                            ('Áo sơ mi unisex Classic Denim Monogram', TRUE),
                                            ('Áo sơ mi denim unisex Diamond Monogram Embargo', TRUE),
                                            ('Áo sơ mi denim unisex cổ bẻ tay dài Megagram', TRUE),
                                            ('Áo sơ mi denim unisex cổ bẻ tay dài Logo Coopers Mega', TRUE),
                                            ('Áo sơ mi denim unisex cổ bẻ tay dài thời trang', TRUE);

-- SAMPLE (đã thêm slug)
-- Áo sơ mi denim unisex tay dài thêu logo thời trang (blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50ins_3adrb0254_1_5454cd32313a4e1aae5711354376aa83_2861f795f2554aa396730853998ef204_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (16, 10, 1, 4890000, 12, 'ao-so-mi-denim-unisex-tay-dai-theu-logo-thoi-trang-blue-104', true);
INSERT INTO product_images (detail_id, image_id) VALUES (104, 69);
INSERT INTO product_categories (product_id , category_id) VALUES (16, 4);

-- 1) Áo sơ mi denim unisex tay dài phối họa tiết (blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50ins_3adrm0654_1_2d7babc93f12416a9127986aefbeddaf_0195dea213664d35befdf03b23a0cb99_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (17, 10, 1, 4890000, 12, 'ao-so-mi-denim-unisex-tay-dai-phoi-hoa-tiet-blue-105', true);
INSERT INTO product_images (detail_id, image_id) VALUES (105, 70);
INSERT INTO product_categories (product_id , category_id) VALUES (17, 4);

-- 2) Áo sơ mi unisex Varsity Vintage Cool (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/43cgd_3awsv0153_1_9ac67b3c21f54defb3bffe90a6b6bd32_d075a237c9eb4f679c021c9bfcb59458_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (18, 1, 1, 4890000, 12, 'ao-so-mi-unisex-varsity-vintage-cool-black-106', true);
INSERT INTO product_images (detail_id, image_id) VALUES (106, 71);
INSERT INTO product_categories (product_id , category_id) VALUES (18, 4);

-- 3) Áo sơ mi unisex Classic Denim Monogram (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50ivs_3awsm0651_1_11ccde0402634d0fb8ad718e4a97e767_f7d9ea8d42704adfae8a6ec20c49b266_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (19, 2, 1, 4890000, 12, 'ao-so-mi-unisex-classic-denim-monogram-white-107', true);
INSERT INTO product_images (detail_id, image_id) VALUES (107, 72);
INSERT INTO product_categories (product_id , category_id) VALUES (19, 4);

-- 4) Áo sơ mi denim unisex Diamond Monogram Embargo (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50crd_3adrm0653_1_106413b455c04c53904b475dad9ff827_0c5d119697db4043b6236477b22d8ee2_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (20, 2, 1, 4890000, 12, 'ao-so-mi-denim-unisex-diamond-monogram-embargo-white-108', true);
INSERT INTO product_images (detail_id, image_id) VALUES (108, 73);
INSERT INTO product_categories (product_id , category_id) VALUES (20, 4);

-- 5) Áo sơ mi denim unisex cổ bẻ tay dài Megagram (blue)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bll_3adrg0351_1_ad1a65d3c1334dfdbacca2a434d23f8f_45c185916f444ba2b2be20e28d71ca55_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (21, 10, 1, 4890000, 12, 'ao-so-mi-denim-unisex-co-be-tay-dai-megagram-blue-109', true);
INSERT INTO product_images (detail_id, image_id) VALUES (109, 74);
INSERT INTO product_categories (product_id , category_id) VALUES (21, 4);

-- 6) Áo sơ mi denim unisex cổ bẻ tay dài Logo Coopers Mega (blue)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50ins_3adrb0144_1_05b342f4a88f4139b41818e4457c1a93_07e9d06536164cb4ae70e72655e7743f_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (22, 10, 1, 4890000, 12, 'ao-so-mi-denim-unisex-co-be-tay-dai-logo-coopers-mega-blue-110', true);
INSERT INTO product_images (detail_id, image_id) VALUES (110, 75);
INSERT INTO product_categories (product_id , category_id) VALUES (22, 4);

-- 7) Áo sơ mi denim unisex cổ bẻ tay dài thời trang (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50cgs_3adrv0144_1_8751ba6cef2b416e9883f1108bc3d53b_4d063a5988834b61909a32213095b979_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (23, 1, 1, 4890000, 12, 'ao-so-mi-denim-unisex-co-be-tay-dai-thoi-trang-black-111', true);
INSERT INTO product_images (detail_id, image_id) VALUES (111, 76);
INSERT INTO product_categories (product_id , category_id) VALUES (23, 4);



------------------------- AO HOODIE --------------------------
-------------------------------------------------------------
---- insert data to table product
INSERT INTO products(title, is_active ) VALUES
                                            ('Áo sweatshirt unisex tay dài Varsity Street Cursive', TRUE),
                                            ('Áo sweatshirt unisex cổ tròn tay dài Varsity', TRUE),
                                            ('Áo sweatshirt unisex cổ tròn tay dài Varsity', TRUE),
                                            ('Áo sweatshirt unisex cổ tròn Basic Vintage Small Lettering', TRUE),
                                            ('Áo sweatshirt unisex cổ tròn Basic Street Back Graphic', TRUE),
                                            ('Áo sweatshirt nữ tay dài Vintage Letter Graphic Varsity', TRUE),
                                            ('Áo sweatshirt unisex tay dài Monogram Vintage Big Lux', TRUE),
                                            ('Áo sweatshirt unisex cổ tròn tay dài New York Yankees', TRUE),
                                            ('Áo sweatshirt unisex cổ tròn tay dài New York', TRUE),
                                            ('Áo sweatshirt unisex cổ tròn Varsity Sportive', TRUE);

-- SAMPLE
-- Áo sweatshirt unisex tay dài Varsity Street Cursive (black)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/43cgs_3amtv0754_1_0834e744a8654e26ae1b5b28e2965831_21db25b5041a4a44924f7e08fd037410_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (24, 1, 1, 3790000, 12, 'ao-sweatshirt-unisex-tay-dai-varsity-street-cursive-black-112', true);
INSERT INTO product_images (detail_id, image_id) VALUES (112, 78);
INSERT INTO product_categories (product_id , category_id) VALUES (24, 5);

-- 1) Áo sweatshirt unisex cổ tròn tay dài Varsity (gray)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/09mgs_3amtv1154_1_62ba17cd4274471aaab404688c115993_8875ded7ab57476bb3f4c35b882310b6_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (25, 11, 1, 3790000, 12, 'ao-sweatshirt-unisex-co-tron-tay-dai-varsity-gray-113', true);
INSERT INTO product_images (detail_id, image_id) VALUES (113, 79);
INSERT INTO product_categories (product_id , category_id) VALUES (25, 5);

-- 2) Áo sweatshirt unisex cổ tròn tay dài Varsity (black)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bks_3amtv0354_1_d87ba8cae03346bdbcf78788a445afa4_4c6f2599487b407dafddab0c9ba26d61_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (26, 1, 1, 3790000, 12, 'ao-sweatshirt-unisex-co-tron-tay-dai-varsity-black-114', true);
INSERT INTO product_images (detail_id, image_id) VALUES (114, 80);
INSERT INTO product_categories (product_id , category_id) VALUES (26, 5);

-- 3) Áo sweatshirt unisex cổ tròn Basic Vintage Small Lettering (yellow)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50ivs_3awsm0651_1_11ccde0402634d0fb8ad718e4a97e767_f7d9ea8d42704adfae8a6ec20c49b266_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (27, 9, 1, 3790000, 12, 'ao-sweatshirt-unisex-co-tron-basic-vintage-small-lettering-yellow-115', true);
INSERT INTO product_images (detail_id, image_id) VALUES (115, 81);
INSERT INTO product_categories (product_id , category_id) VALUES (27, 5);

-- 4) Áo sweatshirt unisex cổ tròn Basic Street Back Graphic (white)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/43mgl_3amtb3154_1_be41fd7782424117a94b6c7e3f12477c_603ed0ea253744bbb6c8dc8e34710daf_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (28, 2, 1, 3790000, 12, 'ao-sweatshirt-unisex-co-tron-basic-street-back-graphic-white-116', true);
INSERT INTO product_images (detail_id, image_id) VALUES (116, 82);
INSERT INTO product_categories (product_id , category_id) VALUES (28, 5);

-- 5) Áo sweatshirt nữ tay dài Vintage Letter Graphic Varsity (black)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/43cgs_3amtv0554_1_14bcd2f1165b405bab78cf5222d9d2f1_f8f63099c28e4794acb794a85295b2a4_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (29, 1, 1, 3790000, 12, 'ao-sweatshirt-nu-tay-dai-vintage-letter-graphic-varsity-black-117', true);
INSERT INTO product_images (detail_id, image_id) VALUES (117, 83);
INSERT INTO product_categories (product_id , category_id) VALUES (29, 5);

-- 7) Áo sweatshirt unisex tay dài Monogram Vintage Big Lux (yellow)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50crd_3amtm0654_1_0b0c22ff3c6841988591f81ed40a57d6_36d9043062ad4c59b9f2d255e33a240a_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (30, 9, 1, 3790000, 12, 'ao-sweatshirt-unisex-tay-dai-monogram-vintage-big-lux-yellow-118', true);
INSERT INTO product_images (detail_id, image_id) VALUES (118, 84);
INSERT INTO product_categories (product_id , category_id) VALUES (30, 5);

-- 8) Áo sweatshirt unisex cổ tròn tay dài New York Yankees (yellow)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50crs_3amtm0154_1_de8f4b3d3f934bba978e975c3830ad3e_9669be79bbf34c19835bb5646e7c90c8_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (31, 9, 1, 3790000, 12, 'ao-sweatshirt-unisex-co-tron-tay-dai-new-york-yankees-yellow-119', true);
INSERT INTO product_images (detail_id, image_id) VALUES (119, 85);
INSERT INTO product_categories (product_id , category_id) VALUES (31, 5);

-- 9) Áo sweatshirt unisex cổ tròn tay dài New York (yellow)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50crs_3amtm0954_1_df8206a4c99f4a31847ad8091aeeead1_c61d1c5a34e446a997d2d10d3fcbf751_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (32, 9, 1, 3790000, 12, 'ao-sweatshirt-unisex-co-tron-tay-dai-new-york-yellow-120', true);
INSERT INTO product_images (detail_id, image_id) VALUES (120, 86);
INSERT INTO product_categories (product_id , category_id) VALUES (32, 5);

-- 10) Áo sweatshirt unisex cổ tròn Varsity Sportive (red)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/43wis_3amtv0254_1_218b071fda4e45e0b8fb494b8748c191_49bfc4413cde49f3ac2c232d8e6f77d8_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (33, 4, 1, 3790000, 12, 'ao-sweatshirt-unisex-co-tron-varsity-sportive-red-121', true);
INSERT INTO product_images (detail_id, image_id) VALUES (121, 87);
INSERT INTO product_categories (product_id , category_id) VALUES (33, 5);


------------------------- QUAN JOGGER --------------------------
-------------------------------------------------------------
---- insert data to table product
INSERT INTO products(title, is_active ) VALUES
                                            ('Quần jogger unisex lưng thun Basic Small Logo Light', TRUE),
                                            ('Quần jogger unisex lưng thun Basic Small Logo', TRUE),
                                            ('Quần jogger unisex lưng thun Varsity', TRUE),
                                            ('Quần jogger unisex lưng thun Jacquard Monogram', TRUE),
                                            ('Quần jogger unisex lưng thun Varsity', TRUE),
                                            ('Quần jogger unisex lưng thun Basic Small Logo', TRUE),
                                            ('Quần jogger unisex lưng thun Classic Monogram', TRUE),
                                            ('Quần jogger unisex lưng thun Monogram', TRUE),
                                            ('Quần jogger unisex lưng thun Monogram', TRUE),
                                            ('Quần jogger unisex phối logo New York Yankees', TRUE);

-- SAMPLE (đã tạo slug)
-- Quần jogger unisex lưng thun Basic Small Logo Light (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bks_3aptb0351_1_50c66111f6fe40328b4a9d665a3aceca_cf9d0e122aac409a9a9c53dcf233571f_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (34, 1, 1, 2890000, 12, 'quan-jogger-unisex-lung-thun-basic-small-logo-light-black-122', true);
INSERT INTO product_images (detail_id, image_id) VALUES (122, 88);
INSERT INTO product_categories (product_id , category_id) VALUES (34, 7);

-- 1) Quần jogger unisex lưng thun Basic Small Logo (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bks_3aptb0151_1_22ac43a0c2ac4234a0d2c400adc01443_156f72eebf5e41329bbd1a18e867f02f_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (35, 1, 1, 2890000, 12, 'quan-jogger-unisex-lung-thun-basic-small-logo-black-123', true);
INSERT INTO product_images (detail_id, image_id) VALUES (123, 89);
INSERT INTO product_categories (product_id , category_id) VALUES (35, 7);

-- 2) Quần jogger unisex lưng thun Varsity (gray)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/13mgs_3aptv0346_1_6495697c35ef461ab9dcf4471f2d6c8f_5273542b767a467b91e1a36c2c7d0f96_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (36, 11, 1, 2890000, 12, 'quan-jogger-unisex-lung-thun-varsity-gray-124', true);
INSERT INTO product_images (detail_id, image_id) VALUES (124, 90);
INSERT INTO product_categories (product_id , category_id) VALUES (36, 7);

-- 3) Quần jogger unisex lưng thun Jacquard Monogram (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/43crs_3aptm0544_1_97f040fb2f2f42f1ba30307a0daf1ab3_a30d14c4e78e4f0cb1d350c13d8652ac_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (37, 2, 1, 2890000, 12, 'quan-jogger-unisex-lung-thun-jacquard-monogram-white-125', true);
INSERT INTO product_images (detail_id, image_id) VALUES (125, 91);
INSERT INTO product_categories (product_id , category_id) VALUES (37, 7);

-- 4) Quần jogger unisex lưng thun Varsity (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50crs_3aptv0144_1_d28b614a9bbb45dda88f548017105a16_9070fc16367c44788e7e571d6e11bd7a_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (38, 2, 1, 2890000, 12, 'quan-jogger-unisex-lung-thun-varsity-white-126', true);
INSERT INTO product_images (detail_id, image_id) VALUES (126, 92);
INSERT INTO product_categories (product_id , category_id) VALUES (38, 7);

-- 5) Quần jogger unisex lưng thun Basic Small Logo (yellow)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/43crd_3aptb0846_1_9e58e7f1b39946c5afab53bb462e46c2_d79e4209122341368dd0aeae33e06aa2_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (39, 9, 1, 2890000, 12, 'quan-jogger-unisex-lung-thun-basic-small-logo-yellow-127', true);
INSERT INTO product_images (detail_id, image_id) VALUES (127, 93);
INSERT INTO product_categories (product_id , category_id) VALUES (39, 7);

-- 6) Quần jogger unisex lưng thun Classic Monogram (pink)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50crd_3aptm0746_1_a67b77142ced42f0a6c53e158a1930dc_49bfaceb478f42e5967b0bff43b992d0_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (40, 5, 1, 2890000, 12, 'quan-jogger-unisex-lung-thun-classic-monogram-pink-128', true);
INSERT INTO product_images (detail_id, image_id) VALUES (128, 94);
INSERT INTO product_categories (product_id , category_id) VALUES (40, 7);

-- 7) Quần jogger unisex lưng thun Monogram (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bks_3aptm0646_1_fc53d2b09592497992446700b8674f86_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (41, 1, 1, 2890000, 12, 'quan-jogger-unisex-lung-thun-monogram-black-129', true);
INSERT INTO product_images (detail_id, image_id) VALUES (129, 95);
INSERT INTO product_categories (product_id , category_id) VALUES (41, 7);

-- 8) Quần jogger unisex lưng thun Monogram (yellow)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50crd_3aptm0144_1_5fd391849aea4c62bc7546bbb0e37dc0_972dced1b4cf4d1fbd18493f3da4aca8_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (42, 9, 1, 2890000, 12, 'quan-jogger-unisex-lung-thun-monogram-yellow-130', true);
INSERT INTO product_images (detail_id, image_id) VALUES (130, 96);
INSERT INTO product_categories (product_id , category_id) VALUES (42, 7);

-- 9) Quần jogger unisex phối logo New York Yankees (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/45ots_3aptb0444_1_0312dcdcad7a477eaa937fabb53ac85c_bf0a7b91abad44eca08aace9caf22e52_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (43, 2, 1, 2890000, 12, 'quan-jogger-unisex-phoi-logo-new-york-yankees-white-131', true);
INSERT INTO product_images (detail_id, image_id) VALUES (131, 97);
INSERT INTO product_categories (product_id , category_id) VALUES (43, 7);




------------------------- QUAN JOGGER --------------------------
-------------------------------------------------------------
---- insert data to table product
INSERT INTO products(title, is_active ) VALUES
                                            ('Quần short unisex ống rộng lưng thun', TRUE),
                                            ('Quần short unisex ống rộng viền sọc', TRUE),
                                            ('Quần short unisex ống rộng phối túi hộp', TRUE),
                                            ('Quần short unisex ống rộng phối dây rút', TRUE),
                                            ('Quần short unisex ngang gối phối logo', TRUE),
                                            ('Quần short unisex ống rộng Varsity Lettering', TRUE),
                                            ('Quần short unisex ống rộng Casual', TRUE),
                                            ('Quần short unisex ống rộng Vintage', TRUE),
                                            ('Quần short unisex Basic Small Logo', TRUE),
                                            ('Quần short unisex Basic Small Logo Summer', TRUE),
                                            ('Quần short unisex ống rộng Sports', TRUE);

-- SAMPLE (đã tạo slug)
-- Quần short unisex ống rộng lưng thun (white)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50crd_3asmb0154_1_628c69679eb74c0381c88853cee04dba_476129f521c948969ce2bf0870dd73dc_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (44, 2, 1, 2890000, 12, 'quan-short-unisex-ong-rong-lung-thun-white-132', true);
INSERT INTO product_images (detail_id, image_id) VALUES (132, 98);
INSERT INTO product_categories (product_id , category_id) VALUES (44, 8);

-- 1) Quần short unisex ống rộng viền sọc (white)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50crs_3aspb0754_1_c073d01e53b64a8fb8c3d5a8f6fd63d7_04fac9a173364edfb3bdcda94ac7ad74_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (45, 2, 1, 2890000, 12, 'quan-short-unisex-ong-rong-vien-soc-white-133', true);
INSERT INTO product_images (detail_id, image_id) VALUES (133, 99);
INSERT INTO product_categories (product_id , category_id) VALUES (45, 8);

-- 2) Quần short unisex ống rộng phối túi hộp (black)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bks_3aspb0354_1_e428a6ce8b8747fcaca5bb19badc2d6e_e291476406654a39b01f2ed84a13a8db_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (46, 1, 1, 2890000, 12, 'quan-short-unisex-ong-rong-phoi-tui-hop-black-134', true);
INSERT INTO product_images (detail_id, image_id) VALUES (134, 100);
INSERT INTO product_categories (product_id , category_id) VALUES (46, 8);

-- 3) Quần short unisex ống rộng phối dây rút (white)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50crd_3aspb0154_1_9e6a1fd653f646758cc4207aeaba2f58_874b6cd408d44dc0a686f3e6d3a143f7_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (47, 2, 1, 2890000, 12, 'quan-short-unisex-ong-rong-phoi-day-rut-white-135', true);
INSERT INTO product_images (detail_id, image_id) VALUES (135, 101);
INSERT INTO product_categories (product_id , category_id) VALUES (47, 8);

-- 4) Quần short unisex ngang gối phối logo (brown)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bgd_3asmv0254_1_4bfc5ffb9eec4c92a879b549496fbdeb_bd20fef0be6e42c28b57185ebff665bf_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (48, 8, 1, 2890000, 12, 'quan-short-unisex-ngang-goi-phoi-logo-brown-136', true);
INSERT INTO product_images (detail_id, image_id) VALUES (136, 102);
INSERT INTO product_categories (product_id , category_id) VALUES (48, 8);

-- 5) Quần short unisex ống rộng Varsity Lettering (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/43mgl_3aspv0153_1_be9d24e2cfcb4da7aeda6963fbe41b2f_40a44e4ee4a142ebab52c304b7cb944b_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (49, 2, 1, 2890000, 12, 'quan-short-unisex-ong-rong-varsity-lettering-white-137', true);
INSERT INTO product_images (detail_id, image_id) VALUES (137, 103);
INSERT INTO product_categories (product_id , category_id) VALUES (49, 8);

-- 6) Quần short unisex ống rộng Casual (dark blue)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/43nys_3fspv0353_1_ed065088d8594cbab447a190d953271c_876aab7d734f42aeb7a3076470b830af_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (50, 3, 1, 2890000, 12, 'quan-short-unisex-ong-rong-casual-dark-blue-138', true);
INSERT INTO product_images (detail_id, image_id) VALUES (138, 104);
INSERT INTO product_categories (product_id , category_id) VALUES (50, 8);

-- 7) Quần short unisex ống rộng Vintage (brown)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/43bgs_3asmv0753_1_a44cbea5d423424c8133cee2235fb314_80f34f5649d84383adf661db7e6e150c_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (51, 8, 1, 2890000, 12, 'quan-short-unisex-ong-rong-vintage-brown-139', true);
INSERT INTO product_images (detail_id, image_id) VALUES (139, 105);
INSERT INTO product_categories (product_id , category_id) VALUES (51, 8);

-- 8) Quần short unisex Basic Small Logo (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bks_3aspb1353_1_a18d08027d154213a155a4459b1b21d2_1dd8dfa295ed497883b848131d522c5e_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (52, 1, 1, 2890000, 12, 'quan-short-unisex-basic-small-logo-black-140', true);
INSERT INTO product_images (detail_id, image_id) VALUES (140, 106);
INSERT INTO product_categories (product_id , category_id) VALUES (52, 8);

-- 9) Quần short unisex Basic Small Logo Summer (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50ivs_3aspb1253_1_1a616911086d4863bc15c220ef5c74f8_fe93428e5f9f4443b1fc5cd3cbcf542b_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (53, 2, 1, 2890000, 12, 'quan-short-unisex-basic-small-logo-summer-white-141', true);
INSERT INTO product_images (detail_id, image_id) VALUES (141, 107);
INSERT INTO product_categories (product_id , category_id) VALUES (53, 8);

-- 10) Quần short unisex ống rộng Sports (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bks_3aspb0153_2_0527632c58f94e8bb4e6d582a03c5c0c_27891f1275664da1ac1b3e885cff5bc1_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (54, 1, 1, 2890000, 12, 'quan-short-unisex-ong-rong-sports-black-142', true);
INSERT INTO product_images (detail_id, image_id) VALUES (142, 108);
INSERT INTO product_categories (product_id , category_id) VALUES (54, 8);


------------------------- NON BUCKET --------------------------
-------------------------------------------------------------
---- insert data to table product
INSERT INTO products(title, is_active) VALUES
                                           ('Nón bucket unisex Denim Embossed', TRUE),
                                           ('Nón bucket unisex Color Denim Safari', TRUE),
                                           ('Nón bucket unisex dệt kim Stripe Summer', TRUE),
                                           ('Nón bucket unisex Diamond Monogram Sporty Safari', TRUE),
                                           ('Nón bucket unisex Denim Random Embo Monogram', TRUE),
                                           ('Nón bucket unisex Jacquard Dia Monogram', TRUE),
                                           ('Nón bucket unisex Basic', TRUE);

-- SAMPLE (đã tạo slug)
-- Nón bucket unisex Denim Embossed (blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bll_3ahtv035n_1_36d3f18f80ac46f0afa767566862cfe6_19ea679a08ec47c1944504f54cdcd307_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (55, 10, 1, 1590000, 12, 'non-bucket-unisex-denim-embossed-blue-143', true);
INSERT INTO product_images (detail_id, image_id) VALUES (143, 109);
INSERT INTO product_categories (product_id , category_id) VALUES (55, 10);

-- 1) Nón bucket unisex Color Denim Safari (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50grs_3ahtd025n_1_25804a497e714bae8f399fc1f306a0e2_259f8c9aa8a94182b695284e63cc5243_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (56, 1, 1, 1590000, 12, 'non-bucket-unisex-color-denim-safari-black-144', true);
INSERT INTO product_images (detail_id, image_id) VALUES (144, 110);
INSERT INTO product_categories (product_id , category_id) VALUES (56, 10);

-- 2) Nón bucket unisex dệt kim Stripe Summer (dark blue)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/07nys_3ahtv0153_1_f0e76d1c02044c5fb0aa811bc46a6d4f_478162e8dc3b43b88e4b4e5ef77911b9_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (57, 3, 1, 1590000, 12, 'non-bucket-unisex-det-kim-stripe-summer-dark-blue-145', true);
INSERT INTO product_images (detail_id, image_id) VALUES (145, 111);
INSERT INTO product_categories (product_id , category_id) VALUES (57, 10);

-- 3) Nón bucket unisex Diamond Monogram Sporty Safari (yellow)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bgd_3ahtm015n_1_607c3124a474449ba02e7ec1042e986c_83164d314edb4d6b9ce3eb3b2187693d_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (58, 9, 1, 1590000, 12, 'non-bucket-unisex-diamond-monogram-sporty-safari-yellow-146', true);
INSERT INTO product_images (detail_id, image_id) VALUES (146, 112);
INSERT INTO product_categories (product_id , category_id) VALUES (58, 10);

-- 4) Nón bucket unisex Denim Random Embo Monogram (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50ins_3ahtdp14n_1_960e95a9f27244098d6dcb89f9db7157_df489876b66c4aa79fd97c1864cfc1bc_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (59, 1, 1, 1590000, 12, 'non-bucket-unisex-denim-random-embo-monogram-black-147', true);
INSERT INTO product_images (detail_id, image_id) VALUES (147, 113);
INSERT INTO product_categories (product_id , category_id) VALUES (59, 10);

-- 5) Nón bucket unisex Jacquard Dia Monogram (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bks_3ahtm024n_1_a72fa976adfb4eedb6a551698db7e43c_5bdf831a652c4d71bb51afa2d33459a3_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (60, 1, 1, 1590000, 12, 'non-bucket-unisex-jacquard-dia-monogram-black-148', true);
INSERT INTO product_images (detail_id, image_id) VALUES (148, 114);
INSERT INTO product_categories (product_id , category_id) VALUES (60, 10);

-- 6) Nón bucket unisex Basic (yellow)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bgl_3aht7804n_2_30b3329f79a540bfb4c045520507901b_55a2a461098a45f8b454ad8796eb877c_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (61, 9, 1, 1590000, 12, 'non-bucket-unisex-basic-yellow-149', true);
INSERT INTO product_images (detail_id, image_id) VALUES (149, 115);
INSERT INTO product_categories (product_id , category_id) VALUES (61, 10);



------------------------- NON BUCKET --------------------------
-------------------------------------------------------------
---- insert data to table product
INSERT INTO products(title, is_active) VALUES
                                           ('Nón bóng chày unisex Suede Touch Structure', TRUE),
                                           ('Nón bóng chày unisex Vintage Damage Denim', TRUE),
                                           ('Nón bóng chày unisex Monogram New York Yankees', TRUE),
                                           ('Nón bóng chày unisex Jelly Unstructured', TRUE),
                                           ('Nón bóng chày unisex Jelly Color Unstructured', TRUE),
                                           ('Nón bóng chày unisex Hip Street Lettering Structured', TRUE),
                                           ('Nón bóng chày unisex Denim Suede Touch', TRUE),
                                           ('Nón bóng chày unisex Vintage Pigment Wappen', TRUE),
                                           ('Nón bóng chày unisex Vintage Color Washing', TRUE),
                                           ('Nón bóng chày unisex Varsity Vintage Street', TRUE),
                                           ('Nón bóng chày unisex Varsity', TRUE),
                                           ('Nón bóng chày unisex Hip Street Artwork', TRUE),
                                           ('Nón bóng chày unisex Denim Embossed', TRUE),
                                           ('Nón bóng chày unisex Vintage Embo Damage Unstructured', TRUE),
                                           ('Nón bóng chày unisex Denim Varsity Cursive', TRUE);

-- SAMPLE (đã tạo slug)
-- Nón bóng chày unisex Suede Touch Structure (dark blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/07nys_3acpb095n_1_156169d1b57b4fb3a38f9681026c2858_10d4a3c1beff413faf12f2e9f87eae50_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (62, 3, 1, 1290000, 12, 'non-bong-chay-unisex-suede-touch-structure-dark-blue-150', true);
INSERT INTO product_images (detail_id, image_id) VALUES (150, 116);
INSERT INTO product_categories (product_id , category_id) VALUES (62, 11);

-- 1) Nón bóng chày unisex Vintage Damage Denim (black)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bks_3acpb195n_1_1a2249c9058c485fbc22a3a25b802f91_6e9cae209d224dd1aec498eb594fdfb2_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (63, 1, 1, 1290000, 12, 'non-bong-chay-unisex-vintage-damage-denim-black-151', true);
INSERT INTO product_images (detail_id, image_id) VALUES (151, 117);
INSERT INTO product_categories (product_id , category_id) VALUES (63, 11);

-- 2) Nón bóng chày unisex Monogram New York Yankees (black)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bks_3acpm045n_1_cf15bc493ccb4f9aa8303f1daabdad24_ed98b7eddf3646b1881b850505324e60_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (64, 1, 1, 1290000, 12, 'non-bong-chay-unisex-monogram-new-york-yankees-black-152', true);
INSERT INTO product_images (detail_id, image_id) VALUES (152, 118);
INSERT INTO product_categories (product_id , category_id) VALUES (64, 11);

-- 3) Nón bóng chày unisex Jelly Unstructured (yellow)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bgs_3acpv315n_2_532ee585ffdf473d972eb06b5f671976_4c432bf287df42ce8f75a082a214c006_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (65, 9, 1, 1290000, 12, 'non-bong-chay-unisex-jelly-unstructured-yellow-153', true);
INSERT INTO product_images (detail_id, image_id) VALUES (153, 119);
INSERT INTO product_categories (product_id , category_id) VALUES (65, 11);

-- 4) Nón bóng chày unisex Jelly Color Unstructured (pink)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/07mal_3acpv325n_1_6231f80032544133a366ac34b707585d_cb4b6b5c465846f9ae53696cdc20875f_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (66, 5, 1, 1290000, 12, 'non-bong-chay-unisex-jelly-color-unstructured-pink-154', true);
INSERT INTO product_images (detail_id, image_id) VALUES (154, 120);
INSERT INTO product_categories (product_id , category_id) VALUES (66, 11);

-- 5) Nón bóng chày unisex Hip Street Lettering Structured (blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50nys_3acpv285n_1_5b41709fe79342efb9b0a6782fd374da_e6699d7fd303494aa9f55fc7da74312d_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (67, 10, 1, 1290000, 12, 'non-bong-chay-unisex-hip-street-lettering-structured-blue-155', true);
INSERT INTO product_images (detail_id, image_id) VALUES (155, 121);
INSERT INTO product_categories (product_id , category_id) VALUES (67, 11);

-- 6) Nón bóng chày unisex Denim Suede Touch (blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bgs_3acpb125n_1_a313cf92c9ba4742a0729e13402530c4_8874b5bcb2f74c45b31b38e18eebecee_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (68, 10, 1, 1290000, 12, 'non-bong-chay-unisex-denim-suede-touch-blue-156', true);
INSERT INTO product_images (detail_id, image_id) VALUES (156, 122);
INSERT INTO product_categories (product_id , category_id) VALUES (68, 11);

-- 7) Nón bóng chày unisex Vintage Pigment Wappen (brown)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/43brs_3acpv265n_1_e3cd7b45b4fe4c3581ec574c98924677_35b13fbea5334009ad60f72251bbc91f_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (69, 8, 1, 1290000, 12, 'non-bong-chay-unisex-vintage-pigment-wappen-brown-157', true);
INSERT INTO product_images (detail_id, image_id) VALUES (157, 123);
INSERT INTO product_categories (product_id , category_id) VALUES (69, 11);

-- 8) Nón bóng chày unisex Vintage Color Washing (gray)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50cgs_3acpv185n_2_38a01b0125b04a0e8b5a7b6af6ed4794_8aa64b54ac6d49178510c84ea7123960_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (70, 11, 1, 1290000, 12, 'non-bong-chay-unisex-vintage-color-washing-gray-158', true);
INSERT INTO product_images (detail_id, image_id) VALUES (158, 124);
INSERT INTO product_categories (product_id , category_id) VALUES (70, 11);

-- 9) Nón bóng chày unisex Varsity Vintage Street (yellow)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bgd_3acpb145n_1_47bd2d14e33847b29f5948fd4eb63578_4a56db1ef1a642508f1f9d354b211e7e_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (71, 9, 1, 1290000, 12, 'non-bong-chay-unisex-varsity-vintage-street-yellow-159', true);
INSERT INTO product_images (detail_id, image_id) VALUES (159, 125);
INSERT INTO product_categories (product_id , category_id) VALUES (71, 11);

-- 10) Nón bóng chày unisex Varsity (blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50nyl_3acpm065n_1_ad39554e7183429ab0cd2c18e250d297_592b044c3a8a49599eab854136fe88e6_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (72, 10, 1, 1290000, 12, 'non-bong-chay-unisex-varsity-blue-160', true);
INSERT INTO product_images (detail_id, image_id) VALUES (160, 126);
INSERT INTO product_categories (product_id , category_id) VALUES (72, 11);

-- 11) Nón bóng chày unisex Hip Street Artwork (green)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50kap_3acpv235n_1_9fba9b1d73c64126b132fe0c5a026be2_536205f77dc24f0f95a36bfd52f8c434_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (73, 12, 1, 1290000, 12, 'non-bong-chay-unisex-hip-street-artwork-green-161', true);
INSERT INTO product_images (detail_id, image_id) VALUES (161, 127);
INSERT INTO product_categories (product_id , category_id) VALUES (73, 11);

-- 12) Nón bóng chày unisex Denim Embossed (blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bll_3acpv215n_2_92cf407faf1e4bc5a691806edbd8efd9_c68d7a00124c4aaa813bfc91567cf03f_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (74, 10, 1, 1290000, 12, 'non-bong-chay-unisex-denim-embossed-blue-162', true);
INSERT INTO product_images (detail_id, image_id) VALUES (162, 128);
INSERT INTO product_categories (product_id , category_id) VALUES (74, 11);

-- 13) Nón bóng chày unisex Vintage Embo Damage Unstructured (red)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50wid_3acpv295n_1_2fa21998a4fd4ed7bd2cb1bdf584e194_f417322a26eb4291ad085337edcc317e_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (75, 4, 1, 1290000, 12, 'non-bong-chay-unisex-vintage-embo-damage-unstructured-red-163', true);
INSERT INTO product_images (detail_id, image_id) VALUES (163, 129);
INSERT INTO product_categories (product_id , category_id) VALUES (75, 11);

-- 14) Nón bóng chày unisex Denim Varsity Cursive (blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50ins_3acpds14n_2_908771edbb2a419797ea6f9f957ebbb8_a25b0ee009fb432890f0aa96429c3c9a_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (76, 10, 1, 1290000, 12, 'non-bong-chay-unisex-denim-varsity-cursive-blue-164', true);
INSERT INTO product_images (detail_id, image_id) VALUES (164, 130);
INSERT INTO product_categories (product_id , category_id) VALUES (76, 11);


------------------------- TUI TOTE --------------------------
-------------------------------------------------------------
---- insert data to table product
INSERT INTO products(title, is_active) VALUES
                                           ('Túi tote unisex dệt kim phom chữ nhật Summer', TRUE),
                                           ('Túi tote unisex Vintage Lettering Denim', TRUE),
                                           ('Túi tote unisex Vintage Lettering Shopper', TRUE),
                                           ('Túi tote nữ phom hình thang Basic PU', TRUE),
                                           ('Túi tote unisex Vintage Lettering Denim', TRUE);

-- SAMPLE (đã tạo slug)
-- Túi tote unisex dệt kim phom chữ nhật Summer (yellow)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50ivs_3aorr0153_1_8099d8db1c234e458861e0025d3223a9_4dfa310f5dd04ddc8ccb299bd72f6c7a_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (77, 9, 1, 1990000, 12, 'tui-tote-unisex-det-kim-phom-chu-nhat-summer-yellow-165', true);
INSERT INTO product_images (detail_id, image_id) VALUES (165, 131);
INSERT INTO product_categories (product_id , category_id) VALUES (77, 13);

-- 1) Túi tote unisex Vintage Lettering Denim (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/tote_bag-3aorv025n-50ins_847d9c00e5bc4500a1c4955af03aef8f_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (78, 1, 1, 1990000, 12, 'tui-tote-unisex-vintage-lettering-denim-black-166', true);
INSERT INTO product_images (detail_id, image_id) VALUES (166, 132);
INSERT INTO product_categories (product_id , category_id) VALUES (78, 13);

-- 2) Túi tote unisex Vintage Lettering Shopper (yellow)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50crs_3aorv015n_1_bd2b4b227a9845fba27a98a9d656e856_68380e6e0fce40a1a8135ea1b725c941_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (79, 9, 1, 1990000, 12, 'tui-tote-unisex-vintage-lettering-shopper-yellow-167', true);
INSERT INTO product_images (detail_id, image_id) VALUES (167, 133);
INSERT INTO product_categories (product_id , category_id) VALUES (79, 13);

-- 3) Túi tote nữ phom hình thang Basic PU (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/43crd_3aors884n_1_b77be90cb2d44ab9b93b053ed1c8ad02_14b1b13a3436445da6a1f3b02ec7f5ef_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (80, 2, 1, 1990000, 12, 'tui-tote-nu-phom-hinh-thang-basic-pu-white-168', true);
INSERT INTO product_images (detail_id, image_id) VALUES (168, 134);
INSERT INTO product_categories (product_id , category_id) VALUES (80, 13);

-- 4) Túi tote unisex Vintage Lettering Denim (blue)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/07sbl_3aorv025n_1_75a5a6cec8304771b4b8f49e6aa4cb41_19630f61b3424be198d55cac48356fe3_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (81, 10, 1, 1990000, 12, 'tui-tote-unisex-vintage-lettering-denim-blue-169', true);
INSERT INTO product_images (detail_id, image_id) VALUES (169, 135);
INSERT INTO product_categories (product_id , category_id) VALUES (81, 13);



------------------------- TUI DEO VAI --------------------------
-------------------------------------------------------------
---- insert data to table product
INSERT INTO products(title, is_active) VALUES
                                           ('Túi đeo vai nữ hình thang Denim Coloring', TRUE),
                                           ('Túi đeo vai nữ hình bán nguyệt Denim Color Block', TRUE),
                                           ('Túi đeo vai nữ Classic Monogram Vintage Denim', TRUE),
                                           ('Túi đeo vai nữ hình bán nguyệt Basic Sportive', TRUE),
                                           ('Túi đeo vai nữ Classic Jacquard Denim Monogram', TRUE),
                                           ('Túi đeo vai nữ Varsity Lettering Canvas', TRUE),
                                           ('Túi đeo vai nữ Classic Mono Jacquard', TRUE),
                                           ('Túi đeo vai nữ Curve Padded Hobo', TRUE),
                                           ('Túi đeo vai nữ hình bán nguyệt Denim Coopers Mega Logo Dia Monogram', TRUE),
                                           ('Túi đeo vai nữ hình bán nguyệt Denim Logo Coopers Mega', TRUE),
                                           ('Túi đeo vai nữ Denim Coopers Mega', TRUE),
                                           ('Túi đeo vai nữ Embo Monogram Boston Red Sox', TRUE);

-- SAMPLE (đã tạo slug)
-- Túi đeo vai nữ hình thang Denim Coloring (dark blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50ins_3abmmv75n_1_fd83c9af06394d58a74dd7dc8b1c1111_b9a3ecc520eb4177b6e579fe8360bbaa_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (82, 3, 1, 3490000, 12, 'tui-deo-vai-nu-hinh-thang-denim-coloring-dark-blue-170', true);
INSERT INTO product_images (detail_id, image_id) VALUES (170, 136);
INSERT INTO product_categories (product_id , category_id) VALUES (82, 14);

-- 1) Túi đeo vai nữ hình bán nguyệt Denim Color Block (dark blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50ins_3abqmv35n_2_d734386e234d454f9925e6c9bea614c0_a88e811f59f04473aa9aeeb15513cdf3_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (83, 3, 1, 3490000, 12, 'tui-deo-vai-nu-hinh-ban-nguyet-denim-color-block-dark-blue-171', true);
INSERT INTO product_images (detail_id, image_id) VALUES (171, 137);
INSERT INTO product_categories (product_id , category_id) VALUES (83, 14);

-- 2) Túi đeo vai nữ Classic Monogram Vintage Denim (blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bll_3abqmv55n_1_318bfe1e32d946e6893de667a0c1f30e_fa6f095cf2bc4a388c64121a300886b7_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (84, 10, 1, 3490000, 12, 'tui-deo-vai-nu-classic-monogram-vintage-denim-blue-172', true);
INSERT INTO product_images (detail_id, image_id) VALUES (172, 138);
INSERT INTO product_categories (product_id , category_id) VALUES (84, 14);

-- 3) Túi đeo vai nữ hình bán nguyệt Basic Sportive (black)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/50bks_3abqb045n_1_b9ef35713f604ebbb55f093b658a83b4_0b613ae5de7e40e2b374a240ffd5a610_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (85, 1, 1, 3490000, 12, 'tui-deo-vai-nu-hinh-ban-nguyet-basic-sportive-black-173', true);
INSERT INTO product_images (detail_id, image_id) VALUES (173, 139);
INSERT INTO product_categories (product_id , category_id) VALUES (85, 14);

-- 4) Túi đeo vai nữ Classic Jacquard Denim Monogram (blue)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bll_3abqmd15n_1_76966c25a7d9412f96988f242f8aef69_a1672f3fe80049d0a02fba674df9f8b3_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (86, 10, 1, 3490000, 12, 'tui-deo-vai-nu-classic-jacquard-denim-monogram-blue-174', true);
INSERT INTO product_images (detail_id, image_id) VALUES (174, 140);
INSERT INTO product_categories (product_id , category_id) VALUES (86, 14);

-- 5) Túi đeo vai nữ Varsity Lettering Canvas (yellow)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50crs_3abqb015n_1_f0b964c044fa40cc8919b3ef72d111bb_ee6b3ae52c3845158e3e19fe1d755448_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (87, 9, 1, 3490000, 12, 'tui-deo-vai-nu-varsity-lettering-canvas-yellow-175', true);
INSERT INTO product_images (detail_id, image_id) VALUES (175, 141);
INSERT INTO product_categories (product_id , category_id) VALUES (87, 14);

-- 6) Túi đeo vai nữ Classic Mono Jacquard (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/43bgl_3abqm015n_1_47efa3c5e4d84ad292b0564fba4576b7_5dd8171b62d64a7ba3185b4a796ff80b_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (88, 2, 1, 3490000, 12, 'tui-deo-vai-nu-classic-mono-jacquard-white-176', true);
INSERT INTO product_images (detail_id, image_id) VALUES (176, 142);
INSERT INTO product_categories (product_id , category_id) VALUES (88, 14);

-- 7) Túi đeo vai nữ Curve Padded Hobo (pink)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50pkm_3abql064n_1_7c5eadd0f4d849ef9731f19eb7ecbc64_a7677137dcb54ac199078cb11a28d7bc_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (89, 5, 1, 3490000, 12, 'tui-deo-vai-nu-curve-padded-hobo-pink-177', true);
INSERT INTO product_images (detail_id, image_id) VALUES (177, 143);
INSERT INTO product_categories (product_id , category_id) VALUES (89, 14);

-- 8) Túi đeo vai nữ hình bán nguyệt Denim Coopers Mega Logo Dia Monogram (dark blue)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/shoulder_bag_3abqm134n_122feef5aea145dd948685f16a28b9ec_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (90, 3, 1, 3490000, 12, 'tui-deo-vai-nu-hinh-ban-nguyet-denim-coopers-mega-logo-dia-monogram-dark-blue-178', true);
INSERT INTO product_images (detail_id, image_id) VALUES (178, 144);
INSERT INTO product_categories (product_id , category_id) VALUES (90, 14);

-- 9) Túi đeo vai nữ hình bán nguyệt Denim Logo Coopers Mega (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/shoulder_bag_3abqm034n__f5451b56448f43f4b2639b95c54c63a2_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (91, 1, 1, 3490000, 12, 'tui-deo-vai-nu-hinh-ban-nguyet-denim-logo-coopers-mega-black-179', true);
INSERT INTO product_images (detail_id, image_id) VALUES (179, 145);
INSERT INTO product_categories (product_id , category_id) VALUES (91, 14);

-- 10) Túi đeo vai nữ Denim Coopers Mega (dark blue)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50ins_3abml054n_1_b89b6a860c8647c88f9c8c472815a37e_69ecdf5db4c04b98837c71f0645688c8_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (92, 3, 1, 3490000, 12, 'tui-deo-vai-nu-denim-coopers-mega-dark-blue-180', true);
INSERT INTO product_images (detail_id, image_id) VALUES (180, 146);
INSERT INTO product_categories (product_id , category_id) VALUES (92, 14);

-- 11) Túi đeo vai nữ Embo Monogram Boston Red Sox (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/43crd_3acrs024n_1_25b4d80a2f35448faf8be4548d680a92_37a750c1fb874ebaa70114bbf8b2df33_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (93, 2, 1, 3490000, 12, 'tui-deo-vai-nu-embo-monogram-boston-red-sox-white-181', true);
INSERT INTO product_images (detail_id, image_id) VALUES (181, 147);
INSERT INTO product_categories (product_id , category_id) VALUES (93, 14);


------------------------- TUI DEO CHEO --------------------------
-------------------------------------------------------------
---- insert data to table product
INSERT INTO products(title, is_active) VALUES
                                           ('Túi đeo chéo unisex hình bán nguyệt Basic Sporty', TRUE),
                                           ('Túi đeo chéo unisex Monogram Golf Core', TRUE),
                                           ('Túi đeo chéo unisex Classic Monogram Golfcore', TRUE),
                                           ('Túi đeo vai nữ hình thang NY', TRUE),
                                           ('Túi đeo chéo nữ phom chữ nhật Cursive Vintage Denim', TRUE),
                                           ('Túi đeo chéo nữ phom chữ nhật Classic Mono Jacquard', TRUE),
                                           ('Túi đeo chéo unisex phom chữ nhật Basic Gopcore', TRUE),
                                           ('Túi đeo chéo unisex phom chữ nhật Dia Mono Sportive', TRUE),
                                           ('Túi đeo chéo nữ phom chữ nhật Basic Athleisure Mini', TRUE),
                                           ('Túi đeo chéo nữ phom chữ nhật Basic Athleisure', TRUE);

-- SAMPLE (đã tạo slug)
-- Túi đeo chéo unisex hình bán nguyệt Basic Sporty (yellow)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/43sas_3acrb045n_1_840934e9291a44758eac74319bf0f4aa_4c7f4af462e94f659cd05dce5e954edc_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (94, 9, 1, 1590000, 12, 'tui-deo-cheo-unisex-hinh-ban-nguyet-basic-sporty-yellow-182', true);
INSERT INTO product_images (detail_id, image_id) VALUES (182, 148);
INSERT INTO product_categories (product_id , category_id) VALUES (94, 15);

-- 1) Túi đeo chéo unisex Monogram Golf Core (blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/43grl_3acrm145n_1_7dd5187b54454921920a5ce3b1abf14b_119a412ef45b41748e5a81e13c20fe48_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (95, 10, 1, 1590000, 12, 'tui-deo-cheo-unisex-monogram-golf-core-blue-183', true);
INSERT INTO product_images (detail_id, image_id) VALUES (183, 149);
INSERT INTO product_categories (product_id , category_id) VALUES (95, 15);

-- 3) Túi đeo chéo unisex Classic Monogram Golfcore (blue)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/43grl_3acrm045n_1_7ed67b3de6914215a4971d3eae2f342a_c38ec637cba247be91b70fe075ec8d52_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (96, 10, 1, 1590000, 12, 'tui-deo-cheo-unisex-classic-monogram-golfcore-blue-184', true);
INSERT INTO product_images (detail_id, image_id) VALUES (184, 150);
INSERT INTO product_categories (product_id , category_id) VALUES (96, 15);

-- 4) Túi đeo vai nữ hình thang NY (brown)
INSERT INTO images (url) VALUES
    ('https://cdn.hstatic.net/products/200000642007/43brd_3abqbc15n_1_658c415839bf4390ac2fc0cc6781ad73_5e0dd928bb1f46cbaac408e0d9a8770a_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (97, 8, 1, 1590000, 12, 'tui-deo-vai-nu-hinh-thang-ny-brown-185', true);
INSERT INTO product_images (detail_id, image_id) VALUES (185, 151);
INSERT INTO product_categories (product_id , category_id) VALUES (97, 15);

-- 5) Túi đeo chéo nữ phom chữ nhật Cursive Vintage Denim (blue)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/crossbody-3acrvd15n-50bll_34f7f282bac446c3a1c84f26891d2e27_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (98, 10, 1, 1590000, 12, 'tui-deo-cheo-nu-phom-chu-nhat-cursive-vintage-denim-blue-186', true);
INSERT INTO product_images (detail_id, image_id) VALUES (186, 152);
INSERT INTO product_categories (product_id , category_id) VALUES (98, 15);

-- 6) Túi đeo chéo nữ phom chữ nhật Classic Mono Jacquard (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bks_3acrm015n_1_4b852e863b1c4cba9d373f2b0294eaf6_29d2c8702f794f6982ab30657ce0ff26_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (99, 1, 1, 1590000, 12, 'tui-deo-cheo-nu-phom-chu-nhat-classic-mono-jacquard-black-187', true);
INSERT INTO product_images (detail_id, image_id) VALUES (187, 153);
INSERT INTO product_categories (product_id , category_id) VALUES (99, 15);

-- 7) Túi đeo chéo unisex phom chữ nhật Basic Gopcore (black)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50bks_3acrb025n_1_0d1d8a0b1e914f588ab66d34ec285abe_7f365b93f677483792575e9f3242fdf7_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (100, 1, 1, 1590000, 12, 'tui-deo-cheo-unisex-phom-chu-nhat-basic-gopcore-black-188', true);
INSERT INTO product_images (detail_id, image_id) VALUES (188, 154);
INSERT INTO product_categories (product_id , category_id) VALUES (100, 15);

-- 8) Túi đeo chéo unisex phom chữ nhật Dia Mono Sportive (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/50ivs_3acrm035n_1_ab98876441ac4e279a9e93abe3ff900e_71000db03d8c4e38954621ec85f15533_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (101, 2, 1, 1590000, 12, 'tui-deo-cheo-unisex-phom-chu-nhat-dia-mono-sportive-white-189', true);
INSERT INTO product_images (detail_id, image_id) VALUES (189, 155);
INSERT INTO product_categories (product_id , category_id) VALUES (101, 15);

-- 9) Túi đeo chéo nữ phom chữ nhật Basic Athleisure Mini (pink)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/45pkm_3acra015n_1_f84a6f8aa46641a0a68acd6732b1017c_fb449ac7edd84e66af87bd33a2172961_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (102, 5, 1, 1590000, 12, 'tui-deo-cheo-nu-phom-chu-nhat-basic-athleisure-mini-pink-190', true);
INSERT INTO product_images (detail_id, image_id) VALUES (190, 156);
INSERT INTO product_categories (product_id , category_id) VALUES (102, 15);

-- 10) Túi đeo chéo nữ phom chữ nhật Basic Athleisure (white)
INSERT INTO images (url) VALUES
    ('https://product.hstatic.net/200000642007/product/shoulder_bag-3abqa015n-50crs_3f611544fc8741e4baa7b83f3d4a10c7_master.jpg');
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
    (103, 2, 1, 1590000, 12, 'tui-deo-cheo-nu-phom-chu-nhat-basic-athleisure-white-191', true);
INSERT INTO product_images (detail_id, image_id) VALUES (191, 157);
INSERT INTO product_categories (product_id , category_id) VALUES (103, 15);
