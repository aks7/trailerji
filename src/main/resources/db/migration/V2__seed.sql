INSERT INTO roles (name, description) VALUES ('USER', 'Default user role');

INSERT INTO users (username, email, password, first_name, last_name, created_at, updated_at, is_enabled, is_account_non_expired, is_account_non_locked, is_credentials_non_expired)
VALUES ('amit', 'amitamit@example.com', '$2a$10$t5tVOoMA5poT5Cl/9ytaROCGR50ncv0qG4emojZPs1iN0ew88uQQ6', 'John', 'Doe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE, TRUE, TRUE, TRUE);

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
