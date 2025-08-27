-- =====================================================
-- SERVICE SCHEMA TABLES
-- =====================================================

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS service.service_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS service.customer_case_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS service.case_service_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS service.technician_note_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS service.case_photo_seq START WITH 1 INCREMENT BY 1;

-- Service table
CREATE TABLE service.service (
    service_id BIGINT PRIMARY KEY DEFAULT nextval('service.service_seq'),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(20) NOT NULL,
    base_price DECIMAL(12,2) NOT NULL,
    duration_min INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    requires_consultation BOOLEAN DEFAULT FALSE,
    requires_patch_test BOOLEAN DEFAULT FALSE,
    retouch_days INTEGER,
    warranty_days INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT ck_service_category CHECK (category IN ('LIP', 'BROW', 'OTHER')),
    CONSTRAINT ck_service_price CHECK (base_price >= 0),
    CONSTRAINT ck_service_duration CHECK (duration_min > 0),
    CONSTRAINT ck_service_code_uppercase CHECK (code = UPPER(code))
);

CREATE UNIQUE INDEX idx_service_code ON service.service(code);
CREATE INDEX idx_service_category ON service.service(category);
CREATE INDEX idx_service_active ON service.service(is_active);
CREATE INDEX idx_service_name ON service.service(name);

COMMENT ON TABLE service.service IS 'Spa services catalog';
COMMENT ON COLUMN service.service.category IS 'LIP, BROW, OTHER';
COMMENT ON COLUMN service.service.retouch_days IS 'Days after service for retouch';

-- Customer Case table
CREATE TABLE service.customer_case (
    case_id BIGINT PRIMARY KEY DEFAULT nextval('service.customer_case_seq'),
    customer_id BIGINT NOT NULL,
    primary_service_id BIGINT,
    intake_note TEXT,
    contraindication_flags JSONB,
    consent_signed_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'INTAKE',
    has_before_photo BOOLEAN DEFAULT FALSE,
    has_after_photo BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_case_customer FOREIGN KEY (customer_id) REFERENCES core.customer(customer_id),
    CONSTRAINT fk_case_primary_service FOREIGN KEY (primary_service_id) REFERENCES service.service(service_id),
    CONSTRAINT ck_case_status CHECK (status IN ('INTAKE', 'IN_PROGRESS', 'DONE', 'FOLLOW_UP')),
    CONSTRAINT ck_case_complete_photos CHECK (status != 'DONE' OR (has_before_photo = TRUE AND has_after_photo = TRUE))
);

CREATE INDEX idx_case_customer ON service.customer_case(customer_id);
CREATE INDEX idx_case_service ON service.customer_case(primary_service_id) WHERE primary_service_id IS NOT NULL;
CREATE INDEX idx_case_status ON service.customer_case(status);
CREATE INDEX idx_case_created ON service.customer_case(created_at DESC);

COMMENT ON TABLE service.customer_case IS 'Service session records';
COMMENT ON COLUMN service.customer_case.contraindication_flags IS 'Medical contraindications as JSON';
COMMENT ON CONSTRAINT ck_case_complete_photos ON service.customer_case IS 'Case must have before/after photos to be DONE';

-- Case Service table (services performed in a case)
CREATE TABLE service.case_service (
    case_service_id BIGINT PRIMARY KEY DEFAULT nextval('service.case_service_seq'),
    case_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    qty INTEGER NOT NULL DEFAULT 1,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    line_total DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    notes TEXT,
    discount_reason VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_case_service_case FOREIGN KEY (case_id) REFERENCES service.customer_case(case_id),
    CONSTRAINT fk_case_service_service FOREIGN KEY (service_id) REFERENCES service.service(service_id),
    CONSTRAINT ck_case_service_status CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'DONE', 'CANCELLED')),
    CONSTRAINT ck_case_service_qty CHECK (qty > 0),
    CONSTRAINT ck_case_service_amounts CHECK (unit_price >= 0 AND discount_amount >= 0 AND tax_amount >= 0 AND line_total >= 0)
);

CREATE INDEX idx_case_service_case ON service.case_service(case_id);
CREATE INDEX idx_case_service_service ON service.case_service(service_id);
CREATE INDEX idx_case_service_status ON service.case_service(status);

COMMENT ON TABLE service.case_service IS 'Services performed in each case';
COMMENT ON COLUMN service.case_service.line_total IS 'Calculated as (unit_price * qty) - discount_amount + tax_amount';

-- Technician Note table
CREATE TABLE service.technician_note (
    tech_note_id BIGINT PRIMARY KEY DEFAULT nextval('service.technician_note_seq'),
    case_id BIGINT NOT NULL,
    technician_id BIGINT NOT NULL,
    note TEXT NOT NULL,
    complications JSONB,
    aftercare JSONB,
    technique_used VARCHAR(200),
    products_used TEXT,
    color_code VARCHAR(50),
    is_retouch_needed BOOLEAN DEFAULT FALSE,
    retouch_notes TEXT,
    client_feedback TEXT,
    satisfaction_rating INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tech_note_case FOREIGN KEY (case_id) REFERENCES service.customer_case(case_id),
    CONSTRAINT fk_tech_note_technician FOREIGN KEY (technician_id) REFERENCES core.staff_user(staff_id),
    CONSTRAINT ck_tech_note_rating CHECK (satisfaction_rating IS NULL OR (satisfaction_rating >= 1 AND satisfaction_rating <= 5))
);

CREATE INDEX idx_tech_note_case ON service.technician_note(case_id);
CREATE INDEX idx_tech_note_technician ON service.technician_note(technician_id);
CREATE INDEX idx_tech_note_created ON service.technician_note(created_at DESC);

COMMENT ON TABLE service.technician_note IS 'Technical notes from service providers';
COMMENT ON COLUMN service.technician_note.satisfaction_rating IS '1-5 star rating';

-- Case Photo table
CREATE TABLE service.case_photo (
    photo_id BIGINT PRIMARY KEY DEFAULT nextval('service.case_photo_seq'),
    case_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    taken_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    taken_by BIGINT NOT NULL,
    note TEXT,
    file_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    is_public BOOLEAN DEFAULT FALSE,
    consent_for_marketing BOOLEAN DEFAULT FALSE,
    anonymized BOOLEAN DEFAULT FALSE,
    deletion_requested_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_photo_case FOREIGN KEY (case_id) REFERENCES service.customer_case(case_id),
    CONSTRAINT fk_photo_staff FOREIGN KEY (taken_by) REFERENCES core.staff_user(staff_id),
    CONSTRAINT ck_photo_type CHECK (type IN ('BEFORE', 'AFTER'))
);

CREATE INDEX idx_case_photo_case ON service.case_photo(case_id);
CREATE INDEX idx_case_photo_type ON service.case_photo(type);
CREATE INDEX idx_case_photo_taken_by ON service.case_photo(taken_by);
CREATE INDEX idx_case_photo_taken_at ON service.case_photo(taken_at DESC);

COMMENT ON TABLE service.case_photo IS 'Before/after service photos';
COMMENT ON COLUMN service.case_photo.consent_for_marketing IS 'Customer consent for marketing use';
