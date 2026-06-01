INSERT INTO trailerji_db.users (
    id,
    is_account_non_expired,
    is_account_non_locked,
    created_at,
    is_credentials_non_expired,
    email,
    is_enabled,
    first_name,
    last_name,
    password,
    updated_at,
    username
) VALUES (
             1,
             TRUE,
             TRUE,
             CURRENT_TIMESTAMP,
             TRUE,
             'amitamit@example.com',
             TRUE,
             'John',
             'Doe',
             '$2a$10$t5tVOoMA5poT5Cl/9ytaROCGR50ncv0qG4emojZPs1iN0ew88uQQ6',
             CURRENT_TIMESTAMP,
             'amit'
         );