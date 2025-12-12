-- 1. Create Users Table (For Authentication)
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    roles VARCHAR(50) NOT NULL
);

-- 2. Create Job Table (For Scheduler)
CREATE TABLE IF NOT EXISTS job (
    id SERIAL PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    payload JSONB,
    is_recurring BOOLEAN DEFAULT FALSE,
    interval_seconds BIGINT DEFAULT 0,
    next_execution_time TIMESTAMP,
    retry_count INT DEFAULT 0,
    max_retry INT DEFAULT 3,
    max_execution_time BIGINT DEFAULT 300,
    is_enabled BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 3. Seed Default Admin User
-- Password is 'password' (BCrypt encoded)
INSERT INTO users (username, password, roles)
VALUES ('admin@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ROLE_ADMIN')
ON CONFLICT (username) DO NOTHING;

-- 4. Seed One Non-Recurring Job (Targets HttpBin)
INSERT INTO job (
    status, name, payload, is_recurring, interval_seconds,
    next_execution_time, retry_count, max_retry, max_execution_time,
    is_enabled, is_deleted, created_by, created_at, updated_at
)
VALUES (
    'PENDING',                -- Ready to be picked up immediately
    'HttpBin_Test_Request',
    jsonb_build_object(
        'url', 'https://httpbin.org/get',
        'method', 'GET',
        'headers', jsonb_build_object('User-Agent', 'Chronos-Worker/1.0'),
        'body', NULL
    ),
    FALSE,                   -- Not recurring
    0,                       -- No interval
    NOW(),                   -- Run immediately
    0, 3, 60,                -- Retry logic
    TRUE, FALSE,             -- Enabled, Not deleted
    'admin',                 -- Created by
    NOW(), NOW()
);