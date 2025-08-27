-- =====================================================
-- CORE SCHEMA TABLES
-- =====================================================

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS core.role_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS core.staff_user_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS core.tier_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS core.customer_seq START WITH 1 INCREMENT BY 1;

-- Role table
CREATE TABLE core.role (
    role_id BIGINT PRIMARY KEY DEFAULT nextval('core.role_seq'),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT ck_role_code_uppercase CHECK (code = UPPER(code))
);

CREATE UNIQUE INDEX idx_role_code ON core.role(code);
COMMENT ON TABLE core.role IS 'System roles: RECEPTIONIST, TECHNICIAN, MANAGER';
COMMENT ON COLUMN core.role.code IS 'Role code (uppercase)';

-- Staff User table
CREATE TABLE core.staff_user (
    staff_id BIGINT PRIMARY KEY DEFAULT nextval('core.staff_user_seq'),
    full_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    role_id BIGINT NOT NULL,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_staff_role FOREIGN KEY (role_id) REFERENCES core.role(role_id),
    CONSTRAINT ck_staff_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    CONSTRAINT ck_staff_phone_format CHECK (phone ~ '^[0-9]+$'),
    CONSTRAINT ck_staff_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE UNIQUE INDEX idx_staff_phone ON core.staff_user(phone);
CREATE UNIQUE INDEX idx_staff_email ON core.staff_user(email) WHERE email IS NOT NULL;
CREATE INDEX idx_staff_role ON core.staff_user(role_id);
CREATE INDEX idx_staff_status ON core.staff_user(status);

COMMENT ON TABLE core.staff_user IS 'System users (staff members)';
COMMENT ON COLUMN core.staff_user.status IS 'ACTIVE, INACTIVE, SUSPENDED';

-- Tier table
CREATE TABLE core.tier (
    tier_id BIGINT PRIMARY KEY DEFAULT nextval('core.tier_seq'),
    code VARCHAR(20) NOT NULL UNIQUE,
    min_spent DECIMAL(12,2) NOT NULL DEFAULT 0,
    min_points INTEGER NOT NULL DEFAULT 0,
    benefits JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT ck_tier_code CHECK (code IN ('REGULAR', 'SILVER', 'GOLD', 'VIP')),
    CONSTRAINT ck_tier_min_spent CHECK (min_spent >= 0),
    CONSTRAINT ck_tier_min_points CHECK (min_points >= 0)
);

CREATE UNIQUE INDEX idx_tier_code ON core.tier(code);
CREATE INDEX idx_tier_min_spent ON core.tier(min_spent);
CREATE INDEX idx_tier_min_points ON core.tier(min_points);

COMMENT ON TABLE core.tier IS 'Customer loyalty tiers';
COMMENT ON COLUMN core.tier.benefits IS 'JSON object with tier benefits configuration';

-- Customer table
CREATE TABLE core.customer (
    customer_id BIGINT PRIMARY KEY DEFAULT nextval('core.customer_seq'),
    full_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    dob DATE,
    gender VARCHAR(20),
    address TEXT,
    tier_id BIGINT NOT NULL,
    total_points INTEGER NOT NULL DEFAULT 0,
    total_spent DECIMAL(12,2) NOT NULL DEFAULT 0,
    notes TEXT,
    is_vip BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_customer_tier FOREIGN KEY (tier_id) REFERENCES core.tier(tier_id),
    CONSTRAINT ck_customer_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    CONSTRAINT ck_customer_phone_format CHECK (phone ~ '^[0-9]+$'),
    CONSTRAINT ck_customer_email_format CHECK (email IS NULL OR email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT ck_customer_total_points CHECK (total_points >= 0),
    CONSTRAINT ck_customer_total_spent CHECK (total_spent >= 0)
);

CREATE UNIQUE INDEX idx_customer_phone ON core.customer(phone);
CREATE INDEX idx_customer_email ON core.customer(email) WHERE email IS NOT NULL;
CREATE INDEX idx_customer_tier ON core.customer(tier_id);
CREATE INDEX idx_customer_total_spent ON core.customer(total_spent DESC);
CREATE INDEX idx_customer_total_points ON core.customer(total_points DESC);
CREATE INDEX idx_customer_created ON core.customer(created_at DESC);

COMMENT ON TABLE core.customer IS 'Customer records';
COMMENT ON COLUMN core.customer.is_vip IS 'VIP flag for special handling';
