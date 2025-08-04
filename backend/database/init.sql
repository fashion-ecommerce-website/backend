-- PostgreSQL Database Initialization Script
-- Run this script to create the database and user for FIT Backend

-- Create database
CREATE DATABASE fit_backend_dev;
CREATE DATABASE fit_backend_prod;

-- Create user (optional - you can use existing postgres user)
-- CREATE USER fit_user WITH PASSWORD 'fit_password';

-- Grant privileges
-- GRANT ALL PRIVILEGES ON DATABASE fit_backend_dev TO fit_user;
-- GRANT ALL PRIVILEGES ON DATABASE fit_backend_prod TO fit_user;

-- Connect to development database
\c fit_backend_dev;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Note: Tables will be created automatically by Hibernate/JPA
-- when the application starts with ddl-auto=create-drop or update

-- You can also create tables manually if needed:
/*
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(120) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_enabled ON users(enabled);
*/ 