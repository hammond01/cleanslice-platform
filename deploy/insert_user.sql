DELETE FROM users;
INSERT INTO users (id, username, email, password_hash, created_at, updated_at, enabled)
VALUES (gen_random_uuid(), 'test', 'test@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', NOW(), NOW(), true);