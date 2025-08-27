-- =====================================================
-- AUDIT SCHEMA TABLES
-- =====================================================

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS audit.audit_log_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS audit.task_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS audit.retouch_schedule_seq START WITH 1 INCREMENT BY 1;

-- Audit Log table
CREATE TABLE audit.audit_log (
    audit_id BIGINT PRIMARY KEY DEFAULT nextval('audit.audit_log_seq'),
    actor_id BIGINT NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    diff JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_id VARCHAR(100),
    session_id VARCHAR(100),
    old_values JSONB,
    new_values JSONB,
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES core.staff_user(staff_id),
    CONSTRAINT ck_audit_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'LOGIN'))
);

CREATE INDEX idx_audit_actor ON audit.audit_log(actor_id);
CREATE INDEX idx_audit_entity ON audit.audit_log(entity_name, entity_id);
CREATE INDEX idx_audit_action ON audit.audit_log(action);
CREATE INDEX idx_audit_created ON audit.audit_log(created_at DESC);
CREATE INDEX idx_audit_request ON audit.audit_log(request_id) WHERE request_id IS NOT NULL;

COMMENT ON TABLE audit.audit_log IS 'System audit trail';
COMMENT ON COLUMN audit.audit_log.action IS 'CREATE, UPDATE, DELETE, STATUS_CHANGE, LOGIN';
COMMENT ON COLUMN audit.audit_log.diff IS 'JSON diff of changes';

-- Task table
CREATE TABLE audit.task (
    task_id BIGINT PRIMARY KEY DEFAULT nextval('audit.task_seq'),
    title VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    due_at TIMESTAMPTZ NOT NULL,
    assignee_id BIGINT NOT NULL,
    related_lead_id BIGINT,
    related_case_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    note TEXT,
    priority INTEGER DEFAULT 3,
    completed_at TIMESTAMPTZ,
    completed_by BIGINT,
    reminder_sent BOOLEAN DEFAULT FALSE,
    reminder_sent_at TIMESTAMPTZ,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_pattern VARCHAR(50),
    contact_phone VARCHAR(20),
    contact_name VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES core.staff_user(staff_id),
    CONSTRAINT fk_task_lead FOREIGN KEY (related_lead_id) REFERENCES lead.lead(lead_id),
    CONSTRAINT fk_task_case FOREIGN KEY (related_case_id) REFERENCES service.customer_case(case_id),
    CONSTRAINT fk_task_completed_by FOREIGN KEY (completed_by) REFERENCES core.staff_user(staff_id),
    CONSTRAINT ck_task_type CHECK (type IN ('CALL', 'FOLLOW_UP', 'RETOUCH_REMINDER')),
    CONSTRAINT ck_task_status CHECK (status IN ('OPEN', 'DONE', 'CANCELLED')),
    CONSTRAINT ck_task_priority CHECK (priority >= 1 AND priority <= 3)
);

CREATE INDEX idx_task_assignee ON audit.task(assignee_id);
CREATE INDEX idx_task_lead ON audit.task(related_lead_id) WHERE related_lead_id IS NOT NULL;
CREATE INDEX idx_task_case ON audit.task(related_case_id) WHERE related_case_id IS NOT NULL;
CREATE INDEX idx_task_status ON audit.task(status);
CREATE INDEX idx_task_type ON audit.task(type);
CREATE INDEX idx_task_due ON audit.task(due_at);
CREATE INDEX idx_task_assignee_status ON audit.task(assignee_id, status, due_at);

COMMENT ON TABLE audit.task IS 'Follow-up tasks and reminders';
COMMENT ON COLUMN audit.task.type IS 'CALL, FOLLOW_UP, RETOUCH_REMINDER';
COMMENT ON COLUMN audit.task.priority IS '1=High, 2=Medium, 3=Low';

-- Retouch Schedule table
CREATE TABLE audit.retouch_schedule (
    retouch_id BIGINT PRIMARY KEY DEFAULT nextval('audit.retouch_schedule_seq'),
    case_id BIGINT NOT NULL,
    due_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    note TEXT,
    scheduled_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    reminder_sent BOOLEAN DEFAULT FALSE,
    reminder_sent_at TIMESTAMPTZ,
    days_before_reminder INTEGER DEFAULT 3,
    is_free_retouch BOOLEAN DEFAULT TRUE,
    discount_percentage INTEGER,
    cancellation_reason TEXT,
    rescheduled_from TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_retouch_case FOREIGN KEY (case_id) REFERENCES service.customer_case(case_id),
    CONSTRAINT ck_retouch_status CHECK (status IN ('PLANNED', 'DONE', 'MISSED')),
    CONSTRAINT ck_retouch_discount CHECK (discount_percentage IS NULL OR (discount_percentage >= 0 AND discount_percentage <= 100))
);

CREATE INDEX idx_retouch_case ON audit.retouch_schedule(case_id);
CREATE INDEX idx_retouch_status ON audit.retouch_schedule(status);
CREATE INDEX idx_retouch_due ON audit.retouch_schedule(due_at);
CREATE INDEX idx_retouch_created ON audit.retouch_schedule(created_at DESC);

COMMENT ON TABLE audit.retouch_schedule IS 'Service retouch scheduling';
COMMENT ON COLUMN audit.retouch_schedule.status IS 'PLANNED, DONE, MISSED';
