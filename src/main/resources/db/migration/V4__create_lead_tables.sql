-- =====================================================
-- LEAD SCHEMA TABLES
-- =====================================================

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS lead.lead_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS lead.appointment_seq START WITH 1 INCREMENT BY 1;

-- Lead table
CREATE TABLE lead.lead (
    lead_id BIGINT PRIMARY KEY DEFAULT nextval('lead.lead_seq'),
    full_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    note TEXT,
    source VARCHAR(50) NOT NULL DEFAULT 'SEO',
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    assigned_to BIGINT,
    created_date DATE NOT NULL DEFAULT CURRENT_DATE,
    utm_source VARCHAR(100),
    utm_medium VARCHAR(100),
    utm_campaign VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_lead_staff FOREIGN KEY (assigned_to) REFERENCES core.staff_user(staff_id),
    CONSTRAINT ck_lead_status CHECK (status IN ('NEW', 'IN_PROGRESS', 'WON', 'LOST')),
    CONSTRAINT ck_lead_phone_format CHECK (phone ~ '^[0-9]+$'),
    CONSTRAINT uk_lead_phone_date UNIQUE (phone, created_date)
);

CREATE INDEX idx_lead_phone ON lead.lead(phone);
CREATE INDEX idx_lead_status ON lead.lead(status);
CREATE INDEX idx_lead_assigned ON lead.lead(assigned_to) WHERE assigned_to IS NOT NULL;
CREATE INDEX idx_lead_created ON lead.lead(created_at DESC);
CREATE INDEX idx_lead_source ON lead.lead(source);

COMMENT ON TABLE lead.lead IS 'Leads from SEO/Marketing';
COMMENT ON COLUMN lead.lead.source IS 'Lead source: SEO, Facebook, Zalo, etc.';
COMMENT ON COLUMN lead.lead.status IS 'NEW, IN_PROGRESS, WON, LOST';
COMMENT ON CONSTRAINT uk_lead_phone_date ON lead.lead IS 'Prevent spam: unique phone per day';

-- Appointment table
CREATE TABLE lead.appointment (
    appt_id BIGINT PRIMARY KEY DEFAULT nextval('lead.appointment_seq'),
    lead_id BIGINT,
    customer_id BIGINT,
    service_id BIGINT,
    technician_id BIGINT NOT NULL,
    receptionist_id BIGINT NOT NULL,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    note TEXT,
    reminder_sent BOOLEAN DEFAULT FALSE,
    confirmed_at TIMESTAMPTZ,
    cancelled_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_appt_lead FOREIGN KEY (lead_id) REFERENCES lead.lead(lead_id),
    CONSTRAINT fk_appt_customer FOREIGN KEY (customer_id) REFERENCES core.customer(customer_id),
    CONSTRAINT fk_appt_service FOREIGN KEY (service_id) REFERENCES service.service(service_id),
    CONSTRAINT fk_appt_technician FOREIGN KEY (technician_id) REFERENCES core.staff_user(staff_id),
    CONSTRAINT fk_appt_receptionist FOREIGN KEY (receptionist_id) REFERENCES core.staff_user(staff_id),
    CONSTRAINT ck_appt_status CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'NO_SHOW', 'DONE', 'CANCELLED')),
    CONSTRAINT ck_appt_time_valid CHECK (end_at > start_at),
    CONSTRAINT ck_appt_has_contact CHECK (lead_id IS NOT NULL OR customer_id IS NOT NULL)
);

-- Prevent overlapping appointments for same technician
CREATE EXTENSION IF NOT EXISTS btree_gist;
ALTER TABLE lead.appointment ADD CONSTRAINT exclude_appt_overlap 
EXCLUDE USING gist (
    technician_id WITH =,
    tstzrange(start_at, end_at) WITH &&
) WHERE (status NOT IN ('CANCELLED', 'NO_SHOW'));

CREATE INDEX idx_appt_lead ON lead.appointment(lead_id) WHERE lead_id IS NOT NULL;
CREATE INDEX idx_appt_customer ON lead.appointment(customer_id) WHERE customer_id IS NOT NULL;
CREATE INDEX idx_appt_technician ON lead.appointment(technician_id);
CREATE INDEX idx_appt_start ON lead.appointment(start_at);
CREATE INDEX idx_appt_status ON lead.appointment(status);
CREATE INDEX idx_appt_tech_time ON lead.appointment(technician_id, start_at);

COMMENT ON TABLE lead.appointment IS 'Service appointments';
COMMENT ON CONSTRAINT exclude_appt_overlap ON lead.appointment IS 'Prevent double-booking technicians';
