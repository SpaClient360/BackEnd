-- =====================================================
-- BILLING SCHEMA TABLES
-- =====================================================

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS billing.invoice_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS billing.payment_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS billing.point_transaction_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS billing.promotion_seq START WITH 1 INCREMENT BY 1;

-- Invoice table
CREATE TABLE billing.invoice (
    invoice_id BIGINT PRIMARY KEY DEFAULT nextval('billing.invoice_seq'),
    case_id BIGINT UNIQUE,
    customer_id BIGINT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    discount_total DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_total DECIMAL(12,2) NOT NULL DEFAULT 0,
    grand_total DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    paid_at TIMESTAMPTZ,
    invoice_number VARCHAR(50) UNIQUE,
    due_date TIMESTAMPTZ,
    notes TEXT,
    promo_code VARCHAR(50),
    promo_discount DECIMAL(12,2),
    points_redeemed INTEGER DEFAULT 0,
    points_value DECIMAL(12,2) DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_invoice_case FOREIGN KEY (case_id) REFERENCES service.customer_case(case_id),
    CONSTRAINT fk_invoice_customer FOREIGN KEY (customer_id) REFERENCES core.customer(customer_id),
    CONSTRAINT ck_invoice_status CHECK (status IN ('DRAFT', 'UNPAID', 'PAID', 'VOID')),
    CONSTRAINT ck_invoice_amounts CHECK (subtotal >= 0 AND discount_total >= 0 AND tax_total >= 0 AND grand_total >= 0),
    CONSTRAINT ck_invoice_points CHECK (points_redeemed >= 0 AND points_value >= 0)
);

CREATE UNIQUE INDEX idx_invoice_case ON billing.invoice(case_id) WHERE case_id IS NOT NULL;
CREATE INDEX idx_invoice_customer ON billing.invoice(customer_id);
CREATE INDEX idx_invoice_status ON billing.invoice(status);
CREATE INDEX idx_invoice_created ON billing.invoice(created_at DESC);
CREATE INDEX idx_invoice_paid_at ON billing.invoice(paid_at) WHERE paid_at IS NOT NULL;
CREATE UNIQUE INDEX idx_invoice_number ON billing.invoice(invoice_number);

COMMENT ON TABLE billing.invoice IS 'Customer invoices';
COMMENT ON COLUMN billing.invoice.status IS 'DRAFT, UNPAID, PAID, VOID';
COMMENT ON COLUMN billing.invoice.grand_total IS 'subtotal - discount_total + tax_total - points_value';

-- Payment table
CREATE TABLE billing.payment (
    payment_id BIGINT PRIMARY KEY DEFAULT nextval('billing.payment_seq'),
    invoice_id BIGINT NOT NULL,
    method VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    txn_ref VARCHAR(100),
    paid_by BIGINT NOT NULL,
    paid_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note TEXT,
    card_last_four VARCHAR(4),
    bank_name VARCHAR(100),
    ewallet_provider VARCHAR(50),
    receipt_number VARCHAR(50) UNIQUE,
    is_refunded BOOLEAN DEFAULT FALSE,
    refunded_at TIMESTAMPTZ,
    refund_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES billing.invoice(invoice_id),
    CONSTRAINT fk_payment_staff FOREIGN KEY (paid_by) REFERENCES core.staff_user(staff_id),
    CONSTRAINT ck_payment_method CHECK (method IN ('CASH', 'CARD', 'BANK', 'EWALLET')),
    CONSTRAINT ck_payment_amount CHECK (amount > 0),
    CONSTRAINT ck_payment_card_digits CHECK (card_last_four IS NULL OR LENGTH(card_last_four) = 4)
);

CREATE INDEX idx_payment_invoice ON billing.payment(invoice_id);
CREATE INDEX idx_payment_method ON billing.payment(method);
CREATE INDEX idx_payment_paid_by ON billing.payment(paid_by);
CREATE INDEX idx_payment_paid_at ON billing.payment(paid_at DESC);
CREATE INDEX idx_payment_txn_ref ON billing.payment(txn_ref) WHERE txn_ref IS NOT NULL;
CREATE UNIQUE INDEX idx_payment_receipt ON billing.payment(receipt_number);

COMMENT ON TABLE billing.payment IS 'Payment records';
COMMENT ON COLUMN billing.payment.method IS 'CASH, CARD, BANK, EWALLET';

-- Point Transaction table
CREATE TABLE billing.point_transaction (
    point_txn_id BIGINT PRIMARY KEY DEFAULT nextval('billing.point_transaction_seq'),
    customer_id BIGINT NOT NULL,
    source VARCHAR(20) NOT NULL,
    points INTEGER NOT NULL,
    related_invoice_id BIGINT,
    note TEXT,
    balance_before INTEGER NOT NULL,
    balance_after INTEGER NOT NULL,
    reference_number VARCHAR(50) UNIQUE,
    expires_at TIMESTAMPTZ,
    is_expired BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_point_txn_customer FOREIGN KEY (customer_id) REFERENCES core.customer(customer_id),
    CONSTRAINT fk_point_txn_invoice FOREIGN KEY (related_invoice_id) REFERENCES billing.invoice(invoice_id),
    CONSTRAINT ck_point_txn_source CHECK (source IN ('EARN', 'REDEEM', 'ADJUST')),
    CONSTRAINT ck_point_txn_balance CHECK (balance_after >= 0),
    CONSTRAINT ck_point_txn_calculation CHECK (balance_before + points = balance_after)
);

CREATE INDEX idx_point_txn_customer ON billing.point_transaction(customer_id);
CREATE INDEX idx_point_txn_invoice ON billing.point_transaction(related_invoice_id) WHERE related_invoice_id IS NOT NULL;
CREATE INDEX idx_point_txn_type ON billing.point_transaction(source);
CREATE INDEX idx_point_txn_created ON billing.point_transaction(created_at DESC);
CREATE UNIQUE INDEX idx_point_txn_ref ON billing.point_transaction(reference_number);

COMMENT ON TABLE billing.point_transaction IS 'Loyalty points ledger';
COMMENT ON COLUMN billing.point_transaction.source IS 'EARN, REDEEM, ADJUST';

-- Promotion table
CREATE TABLE billing.promotion (
    promo_id BIGINT PRIMARY KEY DEFAULT nextval('billing.promotion_seq'),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    value DECIMAL(12,2) NOT NULL,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    conditions JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    min_purchase_amount DECIMAL(12,2),
    max_discount_amount DECIMAL(12,2),
    usage_limit INTEGER,
    usage_count INTEGER DEFAULT 0,
    usage_limit_per_customer INTEGER,
    applicable_services TEXT,
    applicable_tiers TEXT,
    is_combinable BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT ck_promo_type CHECK (type IN ('PERCENT', 'AMOUNT')),
    CONSTRAINT ck_promo_value CHECK (value > 0),
    CONSTRAINT ck_promo_percent CHECK (type != 'PERCENT' OR value <= 100),
    CONSTRAINT ck_promo_dates CHECK (end_at > start_at),
    CONSTRAINT ck_promo_code_uppercase CHECK (code = UPPER(code))
);

CREATE UNIQUE INDEX idx_promo_code ON billing.promotion(code);
CREATE INDEX idx_promo_active ON billing.promotion(is_active);
CREATE INDEX idx_promo_dates ON billing.promotion(start_at, end_at);
CREATE INDEX idx_promo_type ON billing.promotion(type);

COMMENT ON TABLE billing.promotion IS 'Promotional campaigns';
COMMENT ON COLUMN billing.promotion.type IS 'PERCENT or AMOUNT';
COMMENT ON COLUMN billing.promotion.conditions IS 'JSON conditions for promotion eligibility';
