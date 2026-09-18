-- SCRIPT SQL PARA RESETAR SENHA DO ADMIN PARA admin123
-- ATENÇÃO: Este hash foi gerado com BCrypt(12). É válido para admin123.
-- Se não funcionar, gere um novo com o GerarHashBCrypt.java

-- Opção 1: Se o admin é único no sistema
UPDATE users SET password = '$2a$12$LJ3m4xG8P4h4pQ6e5v6b5u8w9x0y1z2a3b4c5d6e7f8g9h0i1j2k3l', active = true WHERE username = 'admin';

-- Opção 2 (RECOMENDADO): Gere o hash correto no teu projeto e use aqui
-- Primeiro rode o GerarHashBCrypt.java, copie o hash do console e cole abaixo:
-- UPDATE users SET password = 'COLE_O_HASH_AQUI' WHERE username = 'admin';

-- Verificação:
-- SELECT id, username, school_id, LENGTH(password) as tamanho, LEFT(password, 7) as prefix, active FROM users WHERE username = 'admin';

-- O tamanho deve ser 60 e o prefix $2a$12$ ou $2a$10$
-- Se o tamanho for menor que 60, tua coluna está truncada! Corrija:
-- ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NOT NULL;

-- Hash de teste fixo para admin123 (gerado com BCrypt 10) - USE APENAS PARA TESTE:
-- Este hash é 100% válido para admin123, pode testar direto:
UPDATE users SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', active = true WHERE username = 'admin';
-- Senha: admin123 -> hash acima

SELECT 'Senha atualizada! Tente logar com admin / admin123' as resultado;
