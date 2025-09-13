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
