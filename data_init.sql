USE sge;
START TRANSACTION;
-- ============================================================
-- 1. LICENSE
-- ============================================================
INSERT INTO license (
    uuid,
    license_key,
    type,
    duration_days,
    activation_date,
    expiration_date,
    status,
    created_at,
    activated_at,
    note
) VALUES (
    UUID(),
    'SGE-MZ-7K4P-X9QM-2V8R-H6TW',
    'ANNUAL',
    365,
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 1 YEAR),
    'ACTIVE',
    NOW(),
    NOW(),
    'Annual demo license'
);
-- ============================================================
-- 2. SCHOOL
-- ============================================================
INSERT INTO school (
    name,
    code,
    nuit,
    address,
    phone,
    email,
    active,
    license_id,
    created_at
)
SELECT
    'Colégio Horizonte',
    'COL-HOR-001',
    '400123456',
    'Maputo - Moçambique',
    '+258 84 000 0000',
    'info@colegiohorizonte.co.mz',
    TRUE,
    id,
    NOW()
FROM license
WHERE license_key = 'SGE-MZ-7K4P-X9QM-2V8R-H6TW';
-- ============================================================
-- 3. ACADEMIC YEARS
-- ============================================================
INSERT INTO academic_year (
    school_id,
    year,
    start_date,
    end_date,
    active,
    created_at
)
SELECT
    id,
    '2026',
    '2026-01-01',
    '2026-12-31',
    TRUE,
    NOW()
FROM school
WHERE code = 'COL-HOR-001';

INSERT INTO academic_year (
    school_id,
    year,
    start_date,
    end_date,
    active,
    created_at
)
SELECT
    id,
    '2027',
    '2027-01-01',
    '2027-12-31',
    FALSE,
    NOW()
FROM school
WHERE code = 'COL-HOR-001';
-- ============================================================
-- 4. ADMIN USER
-- ============================================================
INSERT INTO user (
    school_id,
    name,
    username,
    password,
    email,
    role,
    active,
    created_at
)
SELECT
    id,
    'Administrador do Sistema',
    'admin',
    'admin123',
    'admin@colegiohorizonte.co.mz',
    'ADMIN',
    TRUE,
    NOW()
FROM school
WHERE code = 'COL-HOR-001';
-- ============================================================
-- 5. TEACHER USER
-- ============================================================
INSERT INTO user (
    school_id,
    name,
    username,
    password,
    email,
    role,
    active,
    created_at
)
SELECT
    id,
    'João Manuel',
    'professor',
    'prof123',
    'professor@colegiohorizonte.co.mz',
    'TEACHER',
    TRUE,
    NOW()
FROM school
WHERE code = 'COL-HOR-001';
-- ============================================================
-- 6. GUARDIAN USER
-- ============================================================
INSERT INTO user (
    school_id,
    name,
    username,
    password,
    email,
    role,
    active,
    created_at
)
SELECT
    id,
    'Maria José',
    'encarregado',
    'enc123',
    'encarregado@colegiohorizonte.co.mz',
    'GUARDIAN',
    TRUE,
    NOW()
FROM school
WHERE code = 'COL-HOR-001';
COMMIT;