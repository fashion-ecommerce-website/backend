
-- Xóa toàn bộ dữ liệu của các bảng (tạm thời vô hiệu hóa ràng buộc khóa ngoại)
DO
$$
    DECLARE
        r RECORD;
    BEGIN
        -- Disable foreign key constraints
        EXECUTE 'SET session_replication_role = replica';

        -- Xóa dữ liệu từ tất cả các bảng
        FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
                EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' RESTART IDENTITY CASCADE';
            END LOOP;

        -- Re-enable foreign key constraints
        EXECUTE 'SET session_replication_role = DEFAULT';
    END;
$$;

---- Insert data to table roles
INSERT INTO roles (role_name, is_active, created_at, updated_at) VALUES
                                                                     ('USER', true, '2024-01-01 08:00:00', '2024-01-01 08:00:00'),
                                                                     ('ADMIN', true, '2024-01-01 08:00:00', '2024-01-01 08:00:00');

INSERT INTO user_ranks (code, name)
VALUES
    ('BRONZE',   'Bronze'),
    ('SILVER',   'Silver'),
    ('GOLD',     'Gold'),
    ('PLATINUM', 'Platinum'),
    ('DIAMOND',  'Diamond');



---- insert data to table category
INSERT INTO categories("name", slug, "is_active" ) VALUES ('Áo', 'ao', true);
INSERT INTO categories("name", parent_id, slug, "is_active" ) VALUES ('Áo thun', 1, 'ao-thun', true);

---- insert data to table color
INSERT INTO colors ("name", hex ) VALUES ('black', '#2c2d31');
INSERT INTO colors ("name", hex ) VALUES ('white', '#d6d8d3');
INSERT INTO colors ("name", hex ) VALUES ('dark blue', '#14202e');
INSERT INTO colors ("name", hex ) VALUES ('red', '#2c2d31');
INSERT INTO colors ("name", hex ) VALUES ('pink', '#d4a2bb');
INSERT INTO colors ("name", hex ) VALUES ('orange', '#c69338');
INSERT INTO colors ("name", hex ) VALUES ('mint', '#60a1a7');
INSERT INTO colors ("name", hex ) VALUES ('brown', '#624e4f');
INSERT INTO colors ("name", hex ) VALUES ('yellow', '#dac7a7');
INSERT INTO colors ("name", hex ) VALUES ('blue', '#8ba6c1');
INSERT INTO colors ("name", hex ) VALUES ('gray', '#c6c6c4');
INSERT INTO colors ("name", hex ) VALUES ('green', '#76715d');

---- insert data to table size
INSERT INTO sizes (code, "label") VALUES ('S', 'S');
INSERT INTO sizes (code, "label") VALUES ('M', 'M');
INSERT INTO sizes (code, "label") VALUES ('L', 'L');
INSERT INTO sizes (code, "label") VALUES ('XL', 'XL');
INSERT INTO sizes (code, "label") VALUES ('F', 'F');

---- insert data to table product
INSERT INTO products(title, is_active ) VALUES
                                            ('Áo thun unisex tay ngắn Vintage Monogram Big Lux', TRUE),  --1
                                            ('Áo thun unisex tay ngắn Classic Monogram Big Lux', TRUE),  --2
                                            ('Áo thun unisex tay ngắn Basic Colorful Mega Logo', TRUE),  --3
                                            ('Áo thun unisex cổ tròn tay ngắn Varsity Series', TRUE),    --4
                                            ('Áo thun unisex cổ tròn tay ngắn Varsity', TRUE),			 --5
                                            ('Áo thun unisex cổ tròn tay ngắn phối viền', TRUE),		 --6
                                            ('Áo thun unisex cổ tròn tay ngắn phối logo', TRUE),		 --7
                                            ('Áo thun unisex cổ tròn tay ngắn Legends Heart', TRUE);	 --8


---- insert data to table image
--Áo thun unisex tay ngắn Vintage Monogram Big Lux, dark blue
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50nys_3atsm0754_1_23b21611ba6d40b4bb3388972db5503e_a091f84c244a4731af6a276ba74d20ad_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50nys_3atsm0754_2_abbaee00c8b741ca9a9b24620110def0_573fc1bef9974b3a941a4e8ec2c58bde_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50nys_3atsm0754_4_d33a56119e8d478c80ff195c95ea4802_1be63fe8b7954983b2a6cd80e90c2dac_master.jpg');

--Áo thun unisex tay ngắn Vintage Monogram Big Lux, white
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsm0754_1_46419b34e82e492699b0d7a39e45a8b3_f3f2fb9b8b14490b86d3962b1bf73c53_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsm0754_2_4bd782fa65d54e61ba97c012124cf367_e73a81420b8a46da963aa3be9e71c641_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsm0754_4_5450411745f341549e9d8bafadca8cfb_3cf8c24165244718b7f03b8e1265b947_master.jpg');
--Áo thun unisex tay ngắn Vintage Monogram Big Lux, black
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50cgs_3atsm0754_1_599283215479477bac8aeb30767a8662_07b5f138257b402ab9fd78a882b466db_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50cgs_3atsm0754_2_f9b5358f99b840b6b844721bb3c2fde0_bd39fae75c96435c84e5407b0f9076b2_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50cgs_3atsm0754_4_6064beb308914f1c869c3ebf55b2202f_dc6cd081bf794ffc84b7b761903ab7dc_master.jpg');

--Áo thun unisex tay ngắn Classic Monogram Big Lux, yellow
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/43bgl_3atsm0454_1_73006739e9ca41688156641ce1ce6176_6e1fb8216b1143109cd3d8fc0e719cf9_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/43bgl_3atsm0454_2_bac45704a6254699ad58116c634286a2_a070b79a90bc48f6af799091c5bac807_master.jpg');
--Áo thun unisex tay ngắn Classic Monogram Big Lux, black
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50bks_3atsm0454_1_ae45644814de4f0fa9a71c4c0da4f2f1_c2d4b10e5a5541b79291f7dc59a9bc2b_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50bks_3atsm0454_2_c4697ad76ba14c19aef2442998eefeea_22370b38e8c54d2d8268b4286baa9469_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50bks_3atsm0454_4_879d418f00174b8a999a56c6ab701072_98fb8b31fc984c23bff285c12e9d3d41_master.jpg');
--Áo thun unisex tay ngắn Classic Monogram Big Lux, white
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsm0454_1_bf092a5050824c86aba6874d6098b625_7f5a2a71109c4421ae322b8ba4d9399f_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsm0454_2_f643bb1ff26743ef9e9e208a210fd5bc_ed337ce1f7b44d999534a2ca2e078c84_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsm0454_4_34f6fdb62171448399368051ad757b3e_0326e25193b64b3f87bed9e32e9b8f6d_master.jpg');

--Áo thun unisex tay ngắn Basic Colorful Mega Logo, black
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50bks_3atsb0754_1_2167da6328ad4e85a55f1937a43dbaa8_ba5221d9071b4de4bfdfd569a316daf3_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50bks_3atsb0754_2_b999498883164f9d8d47c1341b91410c_6a193c01d2704e7f9c6f7f0f36d1ccb1_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50bks_3atsb0754_4_48e79236488f4c1891eb483402291996_bbea38a6546f47e3a7db98188bc60bdd_master.jpg');
--Áo thun unisex tay ngắn Basic Colorful Mega Logo, white
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/43ivs_3atsb0754_1_272ce33412ed45088f20b4c2b70d7b60_16c44ba2ea02430c85482a7575b9f8d1_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/43ivs_3atsb0754_2_d0d07393661e41868a1617b876a44071_c2b88df2fc3b4081b68dd6a319b64279_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/43ivs_3atsb0754_5_55259c827bd2424d80fa82d6e7bd5b3b_e49befaacfb045d386ff2a87d9973edc_master.jpg');
--Áo thun unisex tay ngắn Basic Colorful Mega Logo, brown
INSERT INTO images (url) VALUES
                             ('https://product.hstatic.net/200000642007/product/43bgl_3atsb0644_1_46730237395a4321b348b3a4e04b960a_32287a17fb754783878beec5b3d9f329_master.jpg'),
                             ('https://product.hstatic.net/200000642007/product/43bgl_3atsb0644_2_ef0d7f73732c45c180185f5b965b34ec_89fa7064433240b5a80fd44650d2145c_master.jpg'),
                             ('https://product.hstatic.net/200000642007/product/43bgl_3atsb0644_4_47a1d086403b4d3292d42d5b6c15e95a_c67cbbacf7ed4f00a478d44f8c7cb5ee_master.jpg');

--Áo thun unisex cổ tròn tay ngắn Varsity Series, mint
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/07mts_3atsv0554_1_8bcfa4f4060d41d6a393677a7a5e391f_c3f0bd80b6264be48efdfe927056fb0d_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/07mts_3atsv0554_2_818c061843a442fbb1f5a275f67a07bb_cf561b7b3ccc47c8aaf170ae609da388_master.jpg');
--Áo thun unisex cổ tròn tay ngắn Varsity Series, dark blue
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/43nyp_3atsv0554_1_0a4e32a37bb54ed28b75c547e2e5608c_0617b8ded1f6471cb90c29c7688d8b1e_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/43nyp_3atsv0554_2_2d7c984a0c5e47479f36800233f25ca2_86e94d3f50694412bf0dfa02546bb2d2_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/43nyp_3atsv0554_7_1c36f19ab1064e53894619beeb3fa65f_7280e846089e487b9faaabc2943fb6c4_master.jpg');
--Áo thun unisex cổ tròn tay ngắn Varsity Series, white
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsv0554_1_41a9901958e2417ca520038755f6214b_95449c11956540e59f86036fe642ea4a_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsv0554_2_bb6807a3f69749068697decf3f90c82d_67d53f1ca72a4c9f9e1c16dc4e5bfd59_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsv0554_4_3977916e03d04afe90ff48fba591595c_fb84ee9c83da4587ba81f0da9a90bae7_master.jpg');

--Áo thun unisex cổ tròn tay ngắn Varsity, red
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/43wis_3atsv0154_1_2845165a711f4f4cacdd9a376594c7b9_72cc214f4f1b48cc878612dc884d1406_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/43wis_3atsv0154_2_91a19908b1704ab3960ba9699cbc052e_e0461f0e26ee4630802398dfebdeea53_master.jpg');
--Áo thun unisex cổ tròn tay ngắn Varsity, white
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsv0154_1_9d72ca03b1da4fc7b4befdf9b5d91ed3_e4a56f5f29b44eb5a531bfa6eb4fc868_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsv0154_2_969fd52d3dac436daf46edfffa3a0619_4de7657b9bbe4139a601ded56957d8e2_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsv0154_5_2464aa87c0364bb4a0e8fef81aaef32d_2d8828ebe82a4c96ab8cf4f347854582_master.jpg');
--Áo thun unisex cổ tròn tay ngắn Varsity, black
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50bks_3atsv0154_1_5dd511f31092443bb8b149714880eeca_89a1598d9e084e0f959a29aee8fd44c4_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50bks_3atsv0154_2_f82394adab0749f6b36b5e70b64f0c61_1ded7654eb7f44389eb003535dd6ce43_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50bks_3atsv0154_5_1b7cea7a546b4965bef924b24d3cd147_fa9e8d7b5b994802a21015fe948d6831_master.jpg');

--Áo thun unisex cổ tròn tay ngắn phối viền, dark blue
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50nys_3atsb1254_1_dcbd6acc20e54fe489363ea29d987beb_296b88b7650a43948afe940e16cff893_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50nys_3atsb1254_2_28f50a22d603421da510695483fa8c03_dd2305fdd848439abc779b59808e1a87_master.jpg');
--Áo thun unisex cổ tròn tay ngắn phối viền, white
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsb1254_1_bd58ec06409d4ec58d64e74a804b697f_22d4a8c09bd946fb884d51eb6df4a67e_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsb1254_2_8e80e60513f64039a86f34666a4af33d_f3b7064e715f4357879da5e81c6e8b51_master.jpg');

--Áo thun unisex cổ tròn tay ngắn phối logo, dark blue
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50nys_3atsm0154_1_a7dd444a2019463ab09f75be6612dc9d_7936179e58f748d69d9e9d84044f48d0_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50nys_3atsm0154_2_a5062455af0d4a7584254860b069f438_1bc2d6a66ab54e61bb08dda46f49d1ea_master.jpg');
--Áo thun unisex cổ tròn tay ngắn phối logo, yellow
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50crs_3atsm0154_1_a58b3e4583564b8f928f24e25be90a44_83d97289a707473aa1873dd697fe82ee_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50crs_3atsm0154_2_68b17efeef2c444891f626bf9a22d1ff_8e426e8d6ff3474abadc8844c07aeb12_master.jpg');
--Áo thun unisex cổ tròn tay ngắn phối logo, black
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50bks_3atsb1654_1_66567bd236ec4d68813058ae201e1f68_b1f2cea5076c454ca4fb9f7777c346ec_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50bks_3atsb1654_2_b4d32bc0943f4532a27aeed6ff93cc14_08092cf9c19d44c39552ffb02b92c2b9_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50bks_3atsb1654_6_8bd5a3812fe64d14a510dab5fca427c9_6f5eb2f91568475fb526e0ca6e06e558_master.jpg');
--Áo thun unisex cổ tròn tay ngắn phối logo, white
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/43ivs_3atsb1654_1_729494560aba4d20bcadbcdee7a230b9_cd6e12decf0b4eab8d4fd000cbc6f249_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/43ivs_3atsb1654_2_1179059fd31a4750b9695cbe2158d98d_fcaa94c65a67495b9fd5259ffe2498f1_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/43ivs_3atsb1654_4_6b153cb85a494e88af2f9e9cd77d8f46_74a7223ec1034528893e29d1406a0a5f_master.jpg');

--Áo thun unisex cổ tròn tay ngắn Legends Heart, pink
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50pkd_3atsh0254_1_8e688c3f44b4441eb8e0f005d5515d41_81376623bf0d4d2ebff083d7bf193b9e_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50pkd_3atsh0254_2_d0a4d25862e44c5e870bdb9c0fbc1fee_545b9c4c134a46d1b5983a04205f10b6_master.jpg');
--Áo thun unisex cổ tròn tay ngắn Legends Heart, black
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50cgs_3atsh0254_1_cbd670749990457a8b4b5005cbe81b7c_61e410952af74ed182b984485016ef6e_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50cgs_3atsh0254_2_cfe23ae9168d4d2bb9b4c820d2b2ac4f_fb61812fdd6647a79ead15e53f36a411_master.jpg');
--Áo thun unisex cổ tròn tay ngắn Legends Heart, white
INSERT INTO images (url) VALUES
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsh0254_1_fce764aafad94baf82879e45dda0b793_14f73b17e60045e08bc1678845f472dc_master.jpg'),
                             ('https://cdn.hstatic.net/products/200000642007/50ivs_3atsh0254_2_d76c6d0de5704bd6acec5be492abce1c_ce4ae6f483f34e97b240d1b924257d3c_master.jpg');


---- insert data to table product detail
INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
                                                                                                  (1, 3, 1, 1890000, 13, 'ao-thun-unisex-tay-ngan-vintage-monogram-big-lux-1', TRUE),
                                                                                                  (1, 3, 2, 1890000, 13, 'ao-thun-unisex-tay-ngan-vintage-monogram-big-lux-2', TRUE),
                                                                                                  (1, 3, 3, 1890000, 13, 'ao-thun-unisex-tay-ngan-vintage-monogram-big-lux-3', TRUE),
                                                                                                  (1, 3, 4, 1890000, 13, 'ao-thun-unisex-tay-ngan-vintage-monogram-big-lux-4', TRUE),
                                                                                                  (1, 1, 1, 1890000, 13, 'ao-thun-unisex-tay-ngan-vintage-monogram-big-lux-5', TRUE),
                                                                                                  (1, 1, 2, 1890000, 13, 'ao-thun-unisex-tay-ngan-vintage-monogram-big-lux-6', TRUE),
                                                                                                  (1, 1, 3, 1890000, 13, 'ao-thun-unisex-tay-ngan-vintage-monogram-big-lux-7', TRUE),
                                                                                                  (1, 1, 4, 1890000, 13, 'ao-thun-unisex-tay-ngan-vintage-monogram-big-lux-8', TRUE),
                                                                                                  (1, 2, 1, 1890000, 13, 'ao-thun-unisex-tay-ngan-vintage-monogram-big-lux-9', TRUE),
                                                                                                  (1, 2, 2, 1890000, 13, 'ao-thun-unisex-tay-ngan-vintage-monogram-big-lux-10', TRUE),
                                                                                                  (1, 2, 3, 1890000, 13, 'ao-thun-unisex-tay-ngan-vintage-monogram-big-lux-11', TRUE),
                                                                                                  (1, 2, 4, 1890000, 13, 'ao-thun-unisex-tay-ngan-vintage-monogram-big-lux-12', TRUE);

INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
                                                                                                  (2, 9, 1, 1890000, 13, 'ao-thun-unisex-tay-ngan-classic-monogram-big-lux-13', TRUE),
                                                                                                  (2, 9, 2, 1890000, 13, 'ao-thun-unisex-tay-ngan-classic-monogram-big-lux-14', TRUE),
                                                                                                  (2, 9, 3, 1890000, 13, 'ao-thun-unisex-tay-ngan-classic-monogram-big-lux-15', TRUE),
                                                                                                  (2, 9, 4, 1890000, 13, 'ao-thun-unisex-tay-ngan-classic-monogram-big-lux-16', TRUE),
                                                                                                  (2, 1, 1, 1890000, 13, 'ao-thun-unisex-tay-ngan-classic-monogram-big-lux-17', TRUE),
                                                                                                  (2, 1, 2, 1890000, 13, 'ao-thun-unisex-tay-ngan-classic-monogram-big-lux-18', TRUE),
                                                                                                  (2, 1, 3, 1890000, 13, 'ao-thun-unisex-tay-ngan-classic-monogram-big-lux-19', TRUE),
                                                                                                  (2, 1, 4, 1890000, 13, 'ao-thun-unisex-tay-ngan-classic-monogram-big-lux-20', TRUE),
                                                                                                  (2, 2, 1, 1890000, 13, 'ao-thun-unisex-tay-ngan-classic-monogram-big-lux-21', TRUE),
                                                                                                  (2, 2, 2, 1890000, 13, 'ao-thun-unisex-tay-ngan-classic-monogram-big-lux-22', TRUE),
                                                                                                  (2, 2, 3, 1890000, 13, 'ao-thun-unisex-tay-ngan-classic-monogram-big-lux-23', TRUE),
                                                                                                  (2, 2, 4, 1890000, 13, 'ao-thun-unisex-tay-ngan-classic-monogram-big-lux-24', TRUE);

INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
                                                                                                  (3, 8, 1, 1890000, 13, 'ao-thun-unisex-tay-ngan-basic-colorful-mega-logo-25', TRUE),
                                                                                                  (3, 8, 2, 1890000, 13, 'ao-thun-unisex-tay-ngan-basic-colorful-mega-logo-26', TRUE),
                                                                                                  (3, 8, 3, 1890000, 13, 'ao-thun-unisex-tay-ngan-basic-colorful-mega-logo-27', TRUE),
                                                                                                  (3, 8, 4, 1890000, 13, 'ao-thun-unisex-tay-ngan-basic-colorful-mega-logo-28', TRUE),
                                                                                                  (3, 1, 1, 1890000, 13, 'ao-thun-unisex-tay-ngan-basic-colorful-mega-logo-29', TRUE),
                                                                                                  (3, 1, 2, 1890000, 13, 'ao-thun-unisex-tay-ngan-basic-colorful-mega-logo-30', TRUE),
                                                                                                  (3, 1, 3, 1890000, 13, 'ao-thun-unisex-tay-ngan-basic-colorful-mega-logo-31', TRUE),
                                                                                                  (3, 1, 4, 1890000, 13, 'ao-thun-unisex-tay-ngan-basic-colorful-mega-logo-32', TRUE),
                                                                                                  (3, 2, 1, 1890000, 13, 'ao-thun-unisex-tay-ngan-basic-colorful-mega-logo-33', TRUE),
                                                                                                  (3, 2, 2, 1890000, 13, 'ao-thun-unisex-tay-ngan-basic-colorful-mega-logo-34', TRUE),
                                                                                                  (3, 2, 3, 1890000, 13, 'ao-thun-unisex-tay-ngan-basic-colorful-mega-logo-35', TRUE),
                                                                                                  (3, 2, 4, 1890000, 13, 'ao-thun-unisex-tay-ngan-basic-colorful-mega-logo-36', TRUE);

INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
                                                                                                  (4, 7, 1, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-series-37', TRUE),
                                                                                                  (4, 7, 2, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-series-38', TRUE),
                                                                                                  (4, 7, 3, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-series-39', TRUE),
                                                                                                  (4, 7, 4, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-series-40', TRUE),
                                                                                                  (4, 3, 1, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-series-41', TRUE),
                                                                                                  (4, 3, 2, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-series-42', TRUE),
                                                                                                  (4, 3, 3, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-series-43', TRUE),
                                                                                                  (4, 3, 4, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-series-44', TRUE),
                                                                                                  (4, 2, 1, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-series-45', TRUE),
                                                                                                  (4, 2, 2, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-series-46', TRUE),
                                                                                                  (4, 2, 3, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-series-47', TRUE),
                                                                                                  (4, 2, 4, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-series-48', TRUE);

INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
                                                                                                  (5, 4, 1, 1390000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-49', TRUE),
                                                                                                  (5, 4, 2, 1390000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-50', TRUE),
                                                                                                  (5, 4, 3, 1390000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-51', TRUE),
                                                                                                  (5, 4, 4, 1390000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-52', TRUE),
                                                                                                  (5, 1, 1, 1390000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-53', TRUE),
                                                                                                  (5, 1, 2, 1390000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-54', TRUE),
                                                                                                  (5, 1, 3, 1390000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-55', TRUE),
                                                                                                  (5, 1, 4, 1390000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-56', TRUE),
                                                                                                  (5, 2, 1, 1390000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-57', TRUE),
                                                                                                  (5, 2, 2, 1390000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-58', TRUE),
                                                                                                  (5, 2, 3, 1390000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-59', TRUE),
                                                                                                  (5, 2, 4, 1390000, 13, 'ao-thun-unisex-co-tron-tay-ngan-varsity-60', TRUE);


INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
                                                                                                  (6, 3, 1, 1690000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-vien-61', TRUE),
                                                                                                  (6, 3, 2, 1690000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-vien-62', TRUE),
                                                                                                  (6, 3, 3, 1690000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-vien-63', TRUE),
                                                                                                  (6, 3, 4, 1690000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-vien-64', TRUE),
                                                                                                  (6, 2, 1, 1690000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-vien-65', TRUE),
                                                                                                  (6, 2, 2, 1690000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-vien-66', TRUE),
                                                                                                  (6, 2, 3, 1690000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-vien-67', TRUE),
                                                                                                  (6, 2, 4, 1690000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-vien-68', TRUE);


INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
                                                                                                  (7, 3, 1, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-69', TRUE),
                                                                                                  (7, 3, 2, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-70', TRUE),
                                                                                                  (7, 3, 3, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-71', TRUE),
                                                                                                  (7, 3, 4, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-72', TRUE),
                                                                                                  (7, 9, 1, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-73', TRUE),
                                                                                                  (7, 9, 2, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-74', TRUE),
                                                                                                  (7, 9, 3, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-75', TRUE),
                                                                                                  (7, 9, 4, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-76', TRUE),
                                                                                                  (7, 1, 1, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-77', TRUE),
                                                                                                  (7, 1, 2, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-78', TRUE),
                                                                                                  (7, 1, 3, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-79', TRUE),
                                                                                                  (7, 1, 4, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-80', TRUE),
                                                                                                  (7, 2, 1, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-81', TRUE),
                                                                                                  (7, 2, 2, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-82', TRUE),
                                                                                                  (7, 2, 3, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-83', TRUE),
                                                                                                  (7, 2, 4, 2290000, 13, 'ao-thun-unisex-co-tron-tay-ngan-phoi-logo-84', TRUE);

INSERT INTO product_details (product_id, color_id, size_id, price, quantity, slug, is_active) VALUES
                                                                                                  (8, 5, 1, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-legends-heart-85', TRUE),
                                                                                                  (8, 5, 2, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-legends-heart-86', TRUE),
                                                                                                  (8, 5, 3, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-legends-heart-87', TRUE),
                                                                                                  (8, 5, 4, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-legends-heart-88', TRUE),
                                                                                                  (8, 1, 1, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-legends-heart-89', TRUE),
                                                                                                  (8, 1, 2, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-legends-heart-90', TRUE),
                                                                                                  (8, 1, 3, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-legends-heart-91', TRUE),
                                                                                                  (8, 1, 4, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-legends-heart-92', TRUE),
                                                                                                  (8, 2, 1, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-legends-heart-93', TRUE),
                                                                                                  (8, 2, 2, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-legends-heart-94', TRUE),
                                                                                                  (8, 2, 3, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-legends-heart-95', TRUE),
                                                                                                  (8, 2, 4, 1590000, 13, 'ao-thun-unisex-co-tron-tay-ngan-legends-heart-96', TRUE);


---- insert data to table product image

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (1, 1),
                                                     (1, 2),
                                                     (1, 3),
                                                     (2, 1),
                                                     (2, 2),
                                                     (2, 3),
                                                     (3, 1),
                                                     (3, 2),
                                                     (3, 3),
                                                     (4, 1),
                                                     (4, 2),
                                                     (4, 3);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (5, 7),
                                                     (5, 8),
                                                     (5, 9),
                                                     (6, 7),
                                                     (6, 8),
                                                     (6, 9),
                                                     (7, 7),
                                                     (7, 8),
                                                     (7, 9),
                                                     (8, 7),
                                                     (8, 8),
                                                     (8, 9);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (9, 4),
                                                     (9, 5),
                                                     (9, 6),
                                                     (10, 4),
                                                     (10, 5),
                                                     (10, 6),
                                                     (11, 4),
                                                     (11, 5),
                                                     (11, 6),
                                                     (12, 4),
                                                     (12, 5),
                                                     (12, 6);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (13, 10),
                                                     (13, 11),
                                                     (14, 10),
                                                     (14, 11),
                                                     (15, 10),
                                                     (15, 11),
                                                     (16, 10),
                                                     (16, 11);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (17, 12),
                                                     (17, 13),
                                                     (17, 14),
                                                     (18, 12),
                                                     (18, 13),
                                                     (18, 14),
                                                     (19, 12),
                                                     (19, 13),
                                                     (19, 14),
                                                     (20, 12),
                                                     (20, 13),
                                                     (20, 14);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (21, 15),
                                                     (21, 16),
                                                     (21, 17),
                                                     (22, 15),
                                                     (22, 16),
                                                     (22, 17),
                                                     (23, 15),
                                                     (23, 16),
                                                     (23, 17),
                                                     (24, 15),
                                                     (24, 16),
                                                     (24, 17);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (25, 24),
                                                     (25, 25),
                                                     (25, 26),
                                                     (26, 24),
                                                     (26, 25),
                                                     (26, 26),
                                                     (27, 24),
                                                     (27, 25),
                                                     (27, 26),
                                                     (28, 24),
                                                     (28, 25),
                                                     (28, 26);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (29, 18),
                                                     (29, 19),
                                                     (29, 20),
                                                     (30, 18),
                                                     (30, 19),
                                                     (30, 20),
                                                     (31, 18),
                                                     (31, 19),
                                                     (31, 20),
                                                     (32, 18),
                                                     (32, 19),
                                                     (32, 20);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (33, 21),
                                                     (33, 22),
                                                     (33, 23),
                                                     (34, 21),
                                                     (34, 22),
                                                     (34, 23),
                                                     (35, 21),
                                                     (35, 22),
                                                     (35, 23),
                                                     (36, 21),
                                                     (36, 22),
                                                     (36, 23);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (37, 27),
                                                     (37, 28),
                                                     (38, 27),
                                                     (38, 28),
                                                     (39, 27),
                                                     (39, 28),
                                                     (40, 27),
                                                     (40, 28);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (41, 29),
                                                     (41, 30),
                                                     (41, 31),
                                                     (42, 29),
                                                     (42, 30),
                                                     (42, 31),
                                                     (43, 29),
                                                     (43, 30),
                                                     (43, 31),
                                                     (44, 29),
                                                     (44, 30),
                                                     (44, 31);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (45, 32),
                                                     (45, 33),
                                                     (45, 34),
                                                     (46, 32),
                                                     (46, 33),
                                                     (46, 34),
                                                     (47, 32),
                                                     (47, 33),
                                                     (47, 34),
                                                     (48, 32),
                                                     (48, 33),
                                                     (48, 34);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (49, 35),
                                                     (49, 36),
                                                     (50, 35),
                                                     (50, 36),
                                                     (51, 35),
                                                     (51, 36),
                                                     (52, 35),
                                                     (52, 36);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (53, 40),
                                                     (53, 41),
                                                     (53, 42),
                                                     (54, 40),
                                                     (54, 41),
                                                     (54, 42),
                                                     (55, 40),
                                                     (55, 41),
                                                     (55, 42),
                                                     (56, 40),
                                                     (56, 41),
                                                     (56, 42);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (57, 37),
                                                     (57, 38),
                                                     (57, 39),
                                                     (58, 37),
                                                     (58, 38),
                                                     (58, 39),
                                                     (59, 37),
                                                     (59, 38),
                                                     (59, 39),
                                                     (60, 37),
                                                     (60, 38),
                                                     (60, 39);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (61, 43),
                                                     (61, 44),
                                                     (62, 43),
                                                     (62, 44),
                                                     (63, 43),
                                                     (63, 44),
                                                     (64, 43),
                                                     (64, 44);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (65, 45),
                                                     (65, 46),
                                                     (66, 45),
                                                     (66, 46),
                                                     (67, 45),
                                                     (67, 46),
                                                     (68, 45),
                                                     (68, 46);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (69, 47),
                                                     (69, 48),
                                                     (70, 47),
                                                     (70, 48),
                                                     (71, 47),
                                                     (71, 48),
                                                     (72, 47),
                                                     (72, 48);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (73, 49),
                                                     (73, 50),
                                                     (74, 49),
                                                     (74, 50),
                                                     (75, 49),
                                                     (75, 50),
                                                     (76, 49),
                                                     (76, 50);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (77, 51),
                                                     (77, 52),
                                                     (77, 53),
                                                     (78, 51),
                                                     (78, 52),
                                                     (78, 53),
                                                     (79, 51),
                                                     (79, 52),
                                                     (79, 53),
                                                     (80, 51),
                                                     (80, 52),
                                                     (80, 53);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (81, 54),
                                                     (81, 55),
                                                     (81, 56),
                                                     (82, 54),
                                                     (82, 55),
                                                     (82, 56),
                                                     (83, 54),
                                                     (83, 55),
                                                     (83, 56),
                                                     (84, 54),
                                                     (84, 55),
                                                     (84, 56);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (85, 57),
                                                     (85, 58),
                                                     (86, 57),
                                                     (86, 58),
                                                     (87, 57),
                                                     (87, 58),
                                                     (88, 57),
                                                     (88, 58);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (89, 59),
                                                     (89, 60),
                                                     (90, 59),
                                                     (90, 60),
                                                     (91, 59),
                                                     (91, 60),
                                                     (92, 59),
                                                     (92, 60);

INSERT INTO product_images (detail_id, image_id) VALUES
                                                     (93, 61),
                                                     (93, 62),
                                                     (94, 61),
                                                     (94, 62),
                                                     (95, 61),
                                                     (95, 62),
                                                     (96, 61),
                                                     (96, 62);

---- insert data to table product categories
INSERT INTO product_categories (product_id , category_id) VALUES
                                                              (1, 2),
                                                              (2, 2),
                                                              (3, 2),
                                                              (4, 2),
                                                              (5, 2),
                                                              (6, 2),
                                                              (7, 2),
                                                              (8, 2);

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

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

-- ============================================
-- PROMOTIONS - Khuyến mãi sản phẩm
-- ============================================

-- Promotion 1: Khuyến mãi Giáng Sinh 2024 - 20% cho áo thun
INSERT INTO promotions (name, type, value, start_at, end_at, is_active, created_at) VALUES
    ('Khuyến mãi Giáng Sinh 2024', 'PERCENT', 20.00, '2024-12-20 00:00:00', '2024-12-31 23:59:59', true, NOW());

-- Targets: Category Áo thun (id=3)
INSERT INTO promotion_targets (promotion_id, target_type, target_id) VALUES
    (1, 'CATEGORY', 3);

-- Promotion 2: Flash Sale cuối tuần - 15% cho một số sản phẩm cụ thể
INSERT INTO promotions (name, type, value, start_at, end_at, is_active, created_at) VALUES
    ('Flash Sale Cuối Tuần', 'PERCENT', 15.00, '2024-12-14 00:00:00', '2024-12-15 23:59:59', true, NOW());

-- Targets: Một số SKU cụ thể
INSERT INTO promotion_targets (promotion_id, target_type, target_id) VALUES
                                                                         (2, 'SKU', 1),   -- SKU đầu tiên
                                                                         (2, 'SKU', 5),   -- SKU thứ 5
                                                                         (2, 'SKU', 10),  -- SKU thứ 10
                                                                         (2, 'SKU', 15);  -- SKU thứ 15

-- Promotion 3: Tết Nguyên Đán 2025 - 25% cho quần
INSERT INTO promotions (name, type, value, start_at, end_at, is_active, created_at) VALUES
    ('Sale Tết Nguyên Đán 2025', 'PERCENT', 25.00, '2025-01-15 00:00:00', '2025-02-15 23:59:59', true, NOW());

-- Targets: Category Quần (id=9)
INSERT INTO promotion_targets (promotion_id, target_type, target_id) VALUES
    (3, 'CATEGORY', 9);

-- Promotion 4: Khuyến mãi sản phẩm mới - 10%
INSERT INTO promotions (name, type, value, start_at, end_at, is_active, created_at) VALUES
    ('Ưu đãi sản phẩm mới', 'PERCENT', 10.00, '2024-12-01 00:00:00', '2025-01-31 23:59:59', true, NOW());

-- Targets: Một số Product cụ thể
INSERT INTO promotion_targets (promotion_id, target_type, target_id) VALUES
                                                                         (4, 'PRODUCT', 50),
                                                                         (4, 'PRODUCT', 60),
                                                                         (4, 'PRODUCT', 70),
                                                                         (4, 'PRODUCT', 80);

-- Promotion 5: Black Friday - 30% (đã kết thúc)
INSERT INTO promotions (name, type, value, start_at, end_at, is_active, created_at) VALUES
    ('Black Friday 2024', 'PERCENT', 30.00, '2024-11-29 00:00:00', '2024-12-01 23:59:59', false, NOW());

-- Targets: Toàn bộ túi xách
INSERT INTO promotion_targets (promotion_id, target_type, target_id) VALUES
    (5, 'CATEGORY', 15);

-- ============================================
-- VOUCHERS - Mã giảm giá
-- ============================================

-- Voucher 1: Giảm 50K cho đơn từ 500K - Tất cả người dùng
INSERT INTO vouchers (name, code, type, value, max_discount, min_order_amount, usage_limit_total, usage_limit_per_user, start_at, end_at, is_active, audience_type, created_at) VALUES
    ('Giảm 50K đơn từ 500K', 'GIAM50K', 'FIXED', 50000.00, NULL, 500000.00, 1000, 1, '2025-12-01 00:00:00', '2026-01-31 23:59:59', true, 'ALL', NOW());

-- Voucher 2: Giảm 10% tối đa 100K cho đơn từ 300K - Tất cả người dùng
INSERT INTO vouchers (name, code, type, value, max_discount, min_order_amount, usage_limit_total, usage_limit_per_user, start_at, end_at, is_active, audience_type, created_at) VALUES
    ('Giảm 10% tối đa 100K', 'SALE10', 'PERCENT', 10.00, 100000.00, 300000.00, 500, 2, '2025-12-01 00:00:00', '2026-02-28 23:59:59', true, 'ALL', NOW());

-- Voucher 3: Giảm 15% cho thành viên Gold/Platinum - Đơn từ 1 triệu
INSERT INTO vouchers (name, code, type, value, max_discount, min_order_amount, usage_limit_total, usage_limit_per_user, start_at, end_at, is_active, audience_type, created_at) VALUES
    ('VIP 15% cho Gold & Platinum', 'VIP15', 'PERCENT', 15.00, 200000.00, 1000000.00, 200, 3, '2025-12-01 00:00:00', '2026-03-31 23:59:59', true, 'RANK', NOW());

-- Rank rules cho voucher VIP15 (chỉ Gold và Platinum)
INSERT INTO voucher_rank_rules (voucher_id, rank_id) VALUES
                                                         (3, 3),  -- Gold (rank_id = 3)
                                                         (3, 4);  -- Platinum (rank_id = 4)

-- Voucher 4: Giảm 100K cho khách mới - Đơn từ 400K
INSERT INTO vouchers (name, code, type, value, max_discount, min_order_amount, usage_limit_total, usage_limit_per_user, start_at, end_at, is_active, audience_type, created_at) VALUES
    ('Chào mừng khách mới - Giảm 100K', 'WELCOME100', 'FIXED', 100000.00, NULL, 400000.00, 2000, 1, '2025-12-01 00:00:00', '2026-12-31 23:59:59', true, 'ALL', NOW());

-- Voucher 5: Giảm 20% Tết Nguyên Đán - Tối đa 300K
INSERT INTO vouchers (name, code, type, value, max_discount, min_order_amount, usage_limit_total, usage_limit_per_user, start_at, end_at, is_active, audience_type, created_at) VALUES
    ('Sale Tết 20%', 'TET2025', 'PERCENT', 20.00, 300000.00, 500000.00, 1000, 2, '2026-01-20 00:00:00', '2026-02-10 23:59:59', true, 'ALL', NOW());

-- Voucher 6: Freeship - Giảm phí ship 30K
INSERT INTO vouchers (name, code, type, value, max_discount, min_order_amount, usage_limit_total, usage_limit_per_user, start_at, end_at, is_active, audience_type, created_at) VALUES
    ('Freeship 30K', 'FREESHIP30', 'FIXED', 30000.00, NULL, 200000.00, 5000, 5, '2025-12-01 00:00:00', '2026-06-30 23:59:59', true, 'ALL', NOW());

-- Voucher 7: Giảm 25% cho Diamond - VIP cao cấp
INSERT INTO vouchers (name, code, type, value, max_discount, min_order_amount, usage_limit_total, usage_limit_per_user, start_at, end_at, is_active, audience_type, created_at) VALUES
    ('Diamond VIP 25%', 'DIAMOND25', 'PERCENT', 25.00, 500000.00, 2000000.00, 100, 5, '2025-12-01 00:00:00', '2025-12-31 23:59:59', true, 'RANK', NOW());

-- Rank rules cho voucher DIAMOND25 (chỉ Diamond)
INSERT INTO voucher_rank_rules (voucher_id, rank_id) VALUES
    (7, 5);  -- Diamond (rank_id = 5)

-- Voucher 8: Flash Sale 50K - Hết hạn
INSERT INTO vouchers (name, code, type, value, max_discount, min_order_amount, usage_limit_total, usage_limit_per_user, start_at, end_at, is_active, audience_type, created_at) VALUES
    ('Flash Sale 50K', 'FLASH50', 'FIXED', 50000.00, NULL, 300000.00, 100, 1, '2024-11-01 00:00:00', '2024-11-30 23:59:59', false, 'ALL', NOW());

-- Voucher 9: Giảm 5% không giới hạn - Silver trở lên
INSERT INTO vouchers (name, code, type, value, max_discount, min_order_amount, usage_limit_total, usage_limit_per_user, start_at, end_at, is_active, audience_type, created_at) VALUES
    ('Member 5%', 'MEMBER5', 'PERCENT', 5.00, NULL, 100000.00, NULL, NULL, '2025-12-01 00:00:00', '2025-12-31 23:59:59', true, 'RANK', NOW());

-- Rank rules cho voucher MEMBER5 (Silver, Gold, Platinum, Diamond)
INSERT INTO voucher_rank_rules (voucher_id, rank_id) VALUES
                                                         (9, 2),  -- Silver
                                                         (9, 3),  -- Gold
                                                         (9, 4),  -- Platinum
                                                         (9, 5);  -- Diamond

-- Voucher 10: Giảm 200K đơn từ 2 triệu
INSERT INTO vouchers (name, code, type, value, max_discount, min_order_amount, usage_limit_total, usage_limit_per_user, start_at, end_at, is_active, audience_type, created_at) VALUES
    ('Mega Sale 200K', 'MEGA200', 'FIXED', 200000.00, NULL, 2000000.00, 300, 1, '2025-12-15 00:00:00', '2026-01-15 23:59:59', true, 'ALL', NOW());


BEGIN;
ALTER TABLE interactions
    ADD CONSTRAINT unique_user_product_action
        UNIQUE (user_id, product_id, action_type);
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
-- 2) BỔ SUNG USER CHO ĐỦ 1,000
-- =========================================
WITH p AS (SELECT target_users FROM _params)
INSERT INTO users (email, password, username, dob, phone, avatar_url,
                   is_active, email_verified, phone_verified, created_at, updated_at, rank_id)
SELECT
    'user' || gs || '@test.com',
    '$2a$10$1R8WIl8bIRyTQcNihmNue.65YnKTWVoL5b0Tz9oO21NJK3wxJulOO',
    'user' || gs,
    ('1990-01-01'::date + (random() * INTERVAL '30 years')::interval)::date,
    '0' || LPAD(FLOOR(random() * 999999999)::TEXT, 9, '0'),
    'https://example.com/avatars/user' || gs || '.jpg',
    TRUE, (random() < 0.8), (random() < 0.7),
    NOW() - (random() * INTERVAL '365 days'),
    NOW() - (random() * INTERVAL '365 days'),
    1
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
INSERT INTO interactions (user_id, product_id, action_type, count, created_at)
SELECT
    u.id AS user_id,
    p.id AS product_id,
    'PURCHASE' AS action_type,
    5 AS count, -- Điểm tuyệt đối (5 gốc * 5 hệ số)
    NOW() - (random() * interval '90 days')
FROM users u
         JOIN LATERAL (
    SELECT id
    FROM products p_sub
    WHERE
        -- Logic khớp Gu (Túi, Nón, Quần...)
        CASE
            WHEN (u.id % 5) = 1 AND (p_sub.title ILIKE '%Túi%' OR p_sub.title ILIKE '%Balo%' OR p_sub.title ILIKE '%Ví %' OR p_sub.title ILIKE '%Bag%') THEN TRUE
            WHEN (u.id % 5) = 2 AND (p_sub.title ILIKE '%Nón bóng chày%' OR p_sub.title ILIKE '%Cap%' OR p_sub.title ILIKE '%Mũ lưỡi trai%') THEN TRUE
            WHEN (u.id % 5) = 3 AND (p_sub.title ILIKE '%Nón bucket%' OR p_sub.title ILIKE '%Bucket%') THEN TRUE
            WHEN (u.id % 5) = 4 AND (p_sub.title ILIKE '%Quần%' OR p_sub.title ILIKE '%Short%' OR p_sub.title ILIKE '%Jogger%' OR p_sub.title ILIKE '%Jean%' OR p_sub.title ILIKE '%Skirt%') THEN TRUE
            WHEN (u.id % 5) = 0 AND (p_sub.title ILIKE '%Áo%' OR p_sub.title ILIKE '%Shirt%' OR p_sub.title ILIKE '%Top%' OR p_sub.title ILIKE '%Hoodie%') THEN TRUE
            ELSE FALSE
            END
    ORDER BY random()
    LIMIT 5
    ) p ON TRUE
WHERE u.is_active = TRUE;

-- =================================================================
-- BƯỚC 3: TẠO "NHIỄU" ĐA DẠNG (NOISE INJECTION) - [ĐÃ SỬA]
-- Mỗi user tương tác linh tinh với 5 món BẤT KỲ trong kho (Khác gu)
-- Có thể View, Like, hoặc Add to Cart nhưng điểm thấp
-- =================================================================
DELETE FROM interactions
WHERE action_type IN ('VIEW', 'LIKE', 'ADD_TO_CART');

-- 3.1. VIEW (Mỗi người xem 3 món linh tinh khác nhau)
INSERT INTO interactions (user_id, product_id, action_type, count, created_at)
SELECT
    u.id,
    p.id,
    'VIEW',
    1,
    NOW() - (random() * interval '7 days')
FROM users u
         CROSS JOIN LATERAL (
    SELECT id FROM products
    -- [FIX] Kỹ thuật "Neo": Buộc SQL phải nhìn vào u.id mỗi lần chạy
    -- p.id > 0 là luôn đúng, (u.id * 0) luôn bằng 0.
    -- Nhưng vì có u.id nên SQL không dám cache kết quả nữa.
    WHERE id > (u.id * 0)
    ORDER BY random()
    LIMIT 3
    ) p
ON CONFLICT (user_id, product_id, action_type) DO NOTHING;

-- 3.2. LIKE (Mỗi người like 1 món linh tinh khác nhau)
INSERT INTO interactions (user_id, product_id, action_type, count, created_at)
SELECT
    u.id,
    p.id,
    'LIKE',
    1,
    NOW() - (random() * interval '7 days')
FROM users u
         CROSS JOIN LATERAL (
    SELECT id FROM products
    WHERE id > (u.id * 0) -- [FIX] Thêm neo vào đây
    ORDER BY random()
    LIMIT 1
    ) p
ON CONFLICT (user_id, product_id, action_type) DO NOTHING;

-- 3.3. ADD_TO_CART (Mỗi người thêm giỏ 1 món linh tinh khác nhau)
INSERT INTO interactions (user_id, product_id, action_type, count, created_at)
SELECT
    u.id,
    p.id,
    'ADD_TO_CART',
    1,
    NOW() - (random() * interval '7 days')
FROM users u
         CROSS JOIN LATERAL (
    SELECT id FROM products
    WHERE id > (u.id * 0) -- [FIX] Thêm neo vào đây
    ORDER BY random()
    LIMIT 1
    ) p
ON CONFLICT (user_id, product_id, action_type) DO NOTHING;
COMMIT;


---- Insert data to table users (20 records)
-- Password is encoded with BCrypt for "12345678"
INSERT INTO users (id, email, password, username, dob, phone, avatar_url, is_active, email_verified, phone_verified, created_at, updated_at) VALUES
-- Admin users
(1, 'admin@fit.com', '$2a$10$1R8WIl8bIRyTQcNihmNue.65YnKTWVoL5b0Tz9oO21NJK3wxJulOO', 'admin', '1990-01-15', '0123456789', 'https://example.com/avatars/admin.jpg', true, true, true, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(2, 'superadmin@fit.com', '$2a$10$1R8WIl8bIRyTQcNihmNue.65YnKTWVoL5b0Tz9oO21NJK3wxJulOO', 'superadmin', '1985-05-20', '0987654321', 'https://example.com/avatars/superadmin.jpg', true, true, true, '2024-01-01 09:00:00', '2024-01-01 09:00:00');

---- Insert data to table user_roles
INSERT INTO user_roles (user_id, role_id, is_active, created_at, updated_at) VALUES
-- Admin roles
(1, 2, true, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),  -- admin@fit.com -> ADMIN
(2, 2, true, '2024-01-01 09:00:00', '2024-01-01 09:00:00');  -- superadmin@fit.com -> ADMIN

---- Insert data to table addresses (20 records)
INSERT INTO addresses (user_id, full_name, phone, line, ward, city, country_code, is_default, created_at, updated_at) VALUES
-- Admin addresses
(1, 'Admin User', '0123456789', '123 Đường Admin', 'Phường Admin', 'Quận 1', 'VN', true, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(2, 'Super Admin', '0987654321', '456 Đường Super Admin', 'Phường Super Admin', 'Quận 2', 'VN', true, '2024-01-01 09:00:00', '2024-01-01 09:00:00');
