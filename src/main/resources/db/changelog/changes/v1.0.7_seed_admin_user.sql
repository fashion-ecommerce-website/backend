--liquibase formatted sql

--changeset fit-team:001-seed-admin-user
--comment Seed default admin user (admin@fit.com / 12345678v) and assign roles
INSERT INTO roles (role_name, is_active, created_at, updated_at) VALUES 
('USER', true, NOW(), NOW()),
('ADMIN', true, NOW(), NOW())
ON CONFLICT (role_name) DO NOTHING;

-- Insert or update admin user (BCrypt hash generated with strength 10 for password '12345678v')
INSERT INTO users (
    email,
    username,
    password,
    phone,
    avatar_url,
    is_active,
    email_verified,
    phone_verified,
    rank_id,
    total_spent,
    created_at,
    updated_at
) VALUES (
    'admin@fit.com',
    'admin',
    '$2a$10$ScetJLNn0OlCz2tELJYq0OqHv6U64uUjhoPoYGRnz9OFOVsz5z.IO',
    '0123456789',
    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80',
    true,
    true,
    true,
    COALESCE((SELECT id FROM user_ranks WHERE code = 'DIAMOND' LIMIT 1), 1),
    0.00,
    NOW(),
    NOW()
)
ON CONFLICT (email) DO UPDATE SET
    password = EXCLUDED.password,
    is_active = true,
    email_verified = true,
    updated_at = NOW();

-- Assign ADMIN role to admin@fit.com
INSERT INTO user_roles (user_id, role_id, is_active, created_at, updated_at)
SELECT u.id, r.id, true, NOW(), NOW()
FROM users u
CROSS JOIN roles r
WHERE u.email = 'admin@fit.com' AND r.role_name = 'ADMIN'
ON CONFLICT (user_id, role_id) DO UPDATE SET
    is_active = true,
    updated_at = NOW();

-- Assign USER role to admin@fit.com
INSERT INTO user_roles (user_id, role_id, is_active, created_at, updated_at)
SELECT u.id, r.id, true, NOW(), NOW()
FROM users u
CROSS JOIN roles r
WHERE u.email = 'admin@fit.com' AND r.role_name = 'USER'
ON CONFLICT (user_id, role_id) DO UPDATE SET
    is_active = true,
    updated_at = NOW();
