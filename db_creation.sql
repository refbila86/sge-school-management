CREATE DATABASE IF NOT EXISTS sge
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE sge;

-- =========================================================
-- 1. LICENSES
-- =========================================================
DROP TABLE IF EXISTS license;
CREATE TABLE license (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    license_key VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(30) NOT NULL DEFAULT 'ANNUAL',
    duration_days INT NOT NULL DEFAULT 365,
    activation_date DATE NULL,
    expiration_date DATE NULL,
    status ENUM(
        'AVAILABLE',
        'ACTIVE',
        'EXPIRED',
        'CANCELLED'
    ) NOT NULL DEFAULT 'AVAILABLE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activated_at DATETIME NULL,
    note VARCHAR(255) NULL
);


-- =========================================================
-- 2. SCHOOLS
-- =========================================================
CREATE TABLE school (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    nuit VARCHAR(30) NULL,
    address VARCHAR(255) NULL,
    phone VARCHAR(50) NULL,
    email VARCHAR(100) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    license_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_school_license
        FOREIGN KEY (license_id)
        REFERENCES license(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =========================================================
-- 3. ACADEMIC YEAR
-- =========================================================
CREATE TABLE academic_year (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT NOT NULL,
    year VARCHAR(20) NOT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_academic_year_school
        FOREIGN KEY (school_id)
        REFERENCES school(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT uk_academic_year_school
        UNIQUE (school_id, year)
);

-- =========================================================
-- 4. USERS
-- =========================================================
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    username VARCHAR(80) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NULL,
    role ENUM(
        'ADMIN',
        'TEACHER',
        'GUARDIAN'
    ) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME NULL,
    CONSTRAINT fk_user_school
        FOREIGN KEY (school_id)
        REFERENCES school(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT uk_user_school_username
        UNIQUE (school_id, username)
);