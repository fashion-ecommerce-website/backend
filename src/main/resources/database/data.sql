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

---- Insert data to table users (20 records)
-- Password is encoded with BCrypt for "password123"
INSERT INTO users (id, email, password, username, dob, phone, avatar_url, is_active, email_verified, phone_verified, created_at, updated_at) VALUES
-- Admin users
(1, 'admin@fit.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'admin', '1990-01-15', '0123456789', 'https://example.com/avatars/admin.jpg', true, true, true, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(2, 'superadmin@fit.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'superadmin', '1985-05-20', '0987654321', 'https://example.com/avatars/superadmin.jpg', true, true, true, '2024-01-01 09:00:00', '2024-01-01 09:00:00'),

-- Regular users
(3, 'nguyenvana@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'nguyenvana', '1995-03-10', '0123456789', 'https://example.com/avatars/nguyenvana.jpg', true, true, true, '2024-01-02 08:30:00', '2024-01-02 08:30:00'),
(4, 'tranthib@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'tranthib', '1998-07-22', '0987654321', 'https://example.com/avatars/tranthib.jpg', true, true, false, '2024-01-02 09:15:00', '2024-01-02 09:15:00'),
(5, 'levanc@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'levanc', '1992-11-05', '0369123456', 'https://example.com/avatars/levanc.jpg', true, false, true, '2024-01-03 10:20:00', '2024-01-03 10:20:00'),
(6, 'phamthid@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'phamthid', '1996-09-18', '0123987654', 'https://example.com/avatars/phamthid.jpg', true, true, true, '2024-01-03 11:45:00', '2024-01-03 11:45:00'),
(7, 'hoangvane@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'hoangvane', '1993-04-12', '0987123456', 'https://example.com/avatars/hoangvane.jpg', true, true, false, '2024-01-04 12:00:00', '2024-01-04 12:00:00'),
(8, 'vuthif@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'vuthif', '1997-08-25', '0123567890', 'https://example.com/avatars/vuthif.jpg', true, false, true, '2024-01-04 13:30:00', '2024-01-04 13:30:00'),
(9, 'dinhvang@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'dinhvang', '1994-12-03', '0987654321', 'https://example.com/avatars/dinhvang.jpg', true, true, true, '2024-01-05 14:15:00', '2024-01-05 14:15:00'),
(10, 'buitranh@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'buitranh', '1999-06-14', '0123789456', 'https://example.com/avatars/buitranh.jpg', true, true, false, '2024-01-05 15:45:00', '2024-01-05 15:45:00'),
(11, 'nguyenvanj@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'nguyenvanj', '1991-02-28', '0987456321', 'https://example.com/avatars/nguyenvanj.jpg', true, false, true, '2024-01-06 16:20:00', '2024-01-06 16:20:00'),
(12, 'tranthik@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'tranthik', '1996-10-07', '0123456789', 'https://example.com/avatars/tranthik.jpg', true, true, true, '2024-01-06 17:00:00', '2024-01-06 17:00:00'),
(13, 'levanl@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'levanl', '1993-05-19', '0987123456', 'https://example.com/avatars/levanl.jpg', true, true, false, '2024-01-07 18:30:00', '2024-01-07 18:30:00'),
(14, 'phamthim@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'phamthim', '1998-01-11', '0123789456', 'https://example.com/avatars/phamthim.jpg', true, false, true, '2024-01-07 19:15:00', '2024-01-07 19:15:00'),
(15, 'hoangvann@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'hoangvann', '1995-07-26', '0987654321', 'https://example.com/avatars/hoangvann.jpg', true, true, true, '2024-01-08 20:00:00', '2024-01-08 20:00:00'),
(16, 'vuthio@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'vuthio', '1992-03-17', '0123456789', 'https://example.com/avatars/vuthio.jpg', true, true, false, '2024-01-08 21:45:00', '2024-01-08 21:45:00'),
(17, 'dinhvanp@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'dinhvanp', '1997-09-04', '0987123456', 'https://example.com/avatars/dinhvanp.jpg', true, false, true, '2024-01-09 22:30:00', '2024-01-09 22:30:00'),
(18, 'buitranq@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'buitranq', '1994-12-21', '0123789456', 'https://example.com/avatars/buitranq.jpg', true, true, true, '2024-01-09 23:15:00', '2024-01-09 23:15:00'),
(19, 'nguyenvanr@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'nguyenvanr', '1991-08-13', '0987654321', 'https://example.com/avatars/nguyenvanr.jpg', true, true, false, '2024-01-10 00:00:00', '2024-01-10 00:00:00'),
(20, 'inactive_user@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbzXjQ9Nx9n4Kb8lO', 'inactive_user', '1990-06-30', '0123456789', 'https://example.com/avatars/inactive.jpg', false, true, true, '2024-01-10 01:00:00', '2024-01-10 01:00:00');

---- Insert data to table user_roles
INSERT INTO user_roles (user_id, role_id, is_active, created_at, updated_at) VALUES
-- Admin roles
(1, 2, true, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),  -- admin@fit.com -> ADMIN
(2, 2, true, '2024-01-01 09:00:00', '2024-01-01 09:00:00'),  -- superadmin@fit.com -> ADMIN

-- User roles
(3, 1, true, '2024-01-02 08:30:00', '2024-01-02 08:30:00'),  -- nguyenvana@email.com -> USER
(4, 1, true, '2024-01-02 09:15:00', '2024-01-02 09:15:00'),  -- tranthib@email.com -> USER
(5, 1, true, '2024-01-03 10:20:00', '2024-01-03 10:20:00'),  -- levanc@email.com -> USER
(6, 1, true, '2024-01-03 11:45:00', '2024-01-03 11:45:00'),  -- phamthid@email.com -> USER
(7, 1, true, '2024-01-04 12:00:00', '2024-01-04 12:00:00'),  -- hoangvane@email.com -> USER
(8, 1, true, '2024-01-04 13:30:00', '2024-01-04 13:30:00'),  -- vuthif@email.com -> USER
(9, 1, true, '2024-01-05 14:15:00', '2024-01-05 14:15:00'),  -- dinhvang@email.com -> USER
(10, 1, true, '2024-01-05 15:45:00', '2024-01-05 15:45:00'), -- buitranh@email.com -> USER
(11, 1, true, '2024-01-06 16:20:00', '2024-01-06 16:20:00'), -- nguyenvanj@email.com -> USER
(12, 1, true, '2024-01-06 17:00:00', '2024-01-06 17:00:00'), -- tranthik@email.com -> USER
(13, 1, true, '2024-01-07 18:30:00', '2024-01-07 18:30:00'), -- levanl@email.com -> USER
(14, 1, true, '2024-01-07 19:15:00', '2024-01-07 19:15:00'), -- phamthim@email.com -> USER
(15, 1, true, '2024-01-08 20:00:00', '2024-01-08 20:00:00'), -- hoangvann@email.com -> USER
(16, 1, true, '2024-01-08 21:45:00', '2024-01-08 21:45:00'), -- vuthio@email.com -> USER
(17, 1, true, '2024-01-09 22:30:00', '2024-01-09 22:30:00'), -- dinhvanp@email.com -> USER
(18, 1, true, '2024-01-09 23:15:00', '2024-01-09 23:15:00'), -- buitranq@email.com -> USER
(19, 1, true, '2024-01-10 00:00:00', '2024-01-10 00:00:00'), -- nguyenvanr@email.com -> USER
(20, 1, true, '2024-01-10 01:00:00', '2024-01-10 01:00:00'); -- inactive_user@email.com -> USER

---- Insert data to table addresses (20 records)
INSERT INTO addresses (user_id, full_name, phone, line, ward, city, country_code, is_default, created_at, updated_at) VALUES
-- Admin addresses
(1, 'Admin User', '0123456789', '123 Đường Admin', 'Phường Admin', 'Quận 1', 'VN', true, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
(2, 'Super Admin', '0987654321', '456 Đường Super Admin', 'Phường Super Admin', 'Quận 2', 'VN', true, '2024-01-01 09:00:00', '2024-01-01 09:00:00'),

-- Regular user addresses
(3, 'Nguyễn Văn A', '0123456789', '789 Đường Nguyễn Huệ', 'Phường Bến Nghé', 'Quận 1', 'VN', true, '2024-01-02 08:30:00', '2024-01-02 08:30:00'),
(3, 'Nguyễn Văn A', '0123456789', '101 Đường Lê Lợi', 'Phường Bến Nghé', 'Quận 1', 'VN', false, '2024-01-02 09:00:00', '2024-01-02 09:00:00'),

(4, 'Trần Thị B', '0987654321', '202 Đường Điện Biên Phủ', 'Phường 25', 'Quận Bình Thạnh', 'VN', true, '2024-01-02 09:15:00', '2024-01-02 09:15:00'),
(4, 'Trần Thị B', '0987654321', '303 Đường Cách Mạng Tháng 8', 'Phường 10', 'Quận 3', 'VN', false, '2024-01-02 10:00:00', '2024-01-02 10:00:00'),

(5, 'Lê Văn C', '0369123456', '404 Đường Võ Văn Tần', 'Phường 6', 'Quận 3', 'VN', true, '2024-01-03 10:20:00', '2024-01-03 10:20:00'),

(6, 'Phạm Thị D', '0123987654', '505 Đường Pasteur', 'Phường 6', 'Quận 3', 'VN', true, '2024-01-03 11:45:00', '2024-01-03 11:45:00'),
(6, 'Phạm Thị D', '0123987654', '606 Đường Nam Kỳ Khởi Nghĩa', 'Phường 8', 'Quận 3', 'VN', false, '2024-01-03 12:00:00', '2024-01-03 12:00:00'),

(7, 'Hoàng Văn E', '0987123456', '707 Đường Hai Bà Trưng', 'Phường 6', 'Quận 3', 'VN', true, '2024-01-04 12:00:00', '2024-01-04 12:00:00'),

(8, 'Vũ Thị F', '0123567890', '808 Đường Lý Tự Trọng', 'Phường Bến Nghé', 'Quận 1', 'VN', true, '2024-01-04 13:30:00', '2024-01-04 13:30:00'),
(8, 'Vũ Thị F', '0123567890', '909 Đường Nguyễn Du', 'Phường Bến Nghé', 'Quận 1', 'VN', false, '2024-01-04 14:00:00', '2024-01-04 14:00:00'),

(9, 'Đinh Văn G', '0987654321', '111 Đường Tôn Đức Thắng', 'Phường Bến Nghé', 'Quận 1', 'VN', true, '2024-01-05 14:15:00', '2024-01-05 14:15:00'),

(10, 'Bùi Trần H', '0123789456', '222 Đường Đồng Khởi', 'Phường Bến Nghé', 'Quận 1', 'VN', true, '2024-01-05 15:45:00', '2024-01-05 15:45:00'),

(11, 'Nguyễn Văn J', '0987456321', '333 Đường Lê Văn Việt', 'Phường Hiệp Phú', 'Quận 9', 'VN', true, '2024-01-06 16:20:00', '2024-01-06 16:20:00'),
(11, 'Nguyễn Văn J', '0987456321', '444 Đường Nguyễn Duy Trinh', 'Phường Bình Trưng Đông', 'Quận 2', 'VN', false, '2024-01-06 17:00:00', '2024-01-06 17:00:00'),

(12, 'Trần Thị K', '0123456789', '555 Đường Võ Thị Sáu', 'Phường 7', 'Quận 3', 'VN', true, '2024-01-06 17:00:00', '2024-01-06 17:00:00'),

(13, 'Lê Văn L', '0987123456', '666 Đường Trần Hưng Đạo', 'Phường Cầu Ông Lãnh', 'Quận 1', 'VN', true, '2024-01-07 18:30:00', '2024-01-07 18:30:00'),

(14, 'Phạm Thị M', '0123789456', '777 Đường Nguyễn Thị Minh Khai', 'Phường 6', 'Quận 3', 'VN', true, '2024-01-07 19:15:00', '2024-01-07 19:15:00'),

(15, 'Hoàng Văn N', '0987654321', '888 Đường Lý Chính Thắng', 'Phường 9', 'Quận 3', 'VN', true, '2024-01-08 20:00:00', '2024-01-08 20:00:00'),

(16, 'Vũ Thị O', '0123456789', '999 Đường Cao Thắng', 'Phường 5', 'Quận 10', 'VN', true, '2024-01-08 21:45:00', '2024-01-08 21:45:00'),

(17, 'Đinh Văn P', '0987123456', '101 Đường Nguyễn Tri Phương', 'Phường 10', 'Quận 10', 'VN', true, '2024-01-09 22:30:00', '2024-01-09 22:30:00'),

(18, 'Bùi Trần Q', '0123789456', '202 Đường Sư Vạn Hạnh', 'Phường 12', 'Quận 10', 'VN', true, '2024-01-09 23:15:00', '2024-01-09 23:15:00'),

(19, 'Nguyễn Văn R', '0987654321', '303 Đường 3/2', 'Phường 11', 'Quận 10', 'VN', true, '2024-01-10 00:00:00', '2024-01-10 00:00:00'),

(20, 'Inactive User', '0123456789', '404 Đường Test', 'Phường Test', 'Quận Test', 'VN', true, '2024-01-10 01:00:00', '2024-01-10 01:00:00');

---- Update some users with last login time
UPDATE users SET last_login_at = '2024-01-15 10:30:00' WHERE email = 'admin@fit.com';
UPDATE users SET last_login_at = '2024-01-15 09:15:00' WHERE email = 'nguyenvana@email.com';
UPDATE users SET last_login_at = '2024-01-15 08:45:00' WHERE email = 'tranthib@email.com';
UPDATE users SET last_login_at = '2024-01-14 16:20:00' WHERE email = 'levanc@email.com';
UPDATE users SET last_login_at = '2024-01-14 14:30:00' WHERE email = 'phamthid@email.com';


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

