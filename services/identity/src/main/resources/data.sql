-- Insert test user for identity service
-- Password is 'test' hashed with BCrypt
INSERT INTO users (id, username, email, password_hash, created_at, updated_at, enabled)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'test',
    'test@example.com',
    '$2a$10$8K2L0Hkd1DoHgcMGZLflFeF8rO8oOqkE6k1QJZ5nK5QJZ5nK5QJZ5', -- BCrypt hash for 'test'
    NOW(),
    NOW(),
    true
);

-- Insert admin user
INSERT INTO users (id, username, email, password_hash, created_at, updated_at, enabled)
VALUES (
    '550e8400-e29b-41d4-a716-446655440001',
    'admin',
    'admin@example.com',
    '$2a$10$8K2L0Hkd1DoHgcMGZLflFeF8rO8oOqkE6k1QJZ5nK5QJZ5nK5QJZ5', -- BCrypt hash for 'test'
    NOW(),
    NOW(),
    true
);