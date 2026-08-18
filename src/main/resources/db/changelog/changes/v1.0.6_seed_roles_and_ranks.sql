--liquibase formatted sql

--changeset fit-team:001-seed-roles-and-ranks
--comment Seed default roles and user ranks if not present
INSERT INTO roles (role_name, is_active, created_at, updated_at) VALUES 
('USER', true, NOW(), NOW()),
('ADMIN', true, NOW(), NOW())
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO user_ranks (code, name) VALUES
('BRONZE', 'Bronze'),
('SILVER', 'Silver'),
('GOLD', 'Gold'),
('PLATINUM', 'Platinum'),
('DIAMOND', 'Diamond')
ON CONFLICT (code) DO NOTHING;
