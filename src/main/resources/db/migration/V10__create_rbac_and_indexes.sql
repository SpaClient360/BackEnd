-- =====================================================
-- ROLE-BASED ACCESS CONTROL (RBAC)
-- =====================================================

-- Create database roles for each system role
CREATE ROLE receptionist_role;
CREATE ROLE technician_role;
CREATE ROLE manager_role;

-- Grant schema usage
GRANT USAGE ON SCHEMA core, lead, service, billing, audit TO receptionist_role, technician_role, manager_role;

-- =====================================================
-- RECEPTIONIST PERMISSIONS
-- =====================================================

-- Core schema
GRANT SELECT ON ALL TABLES IN SCHEMA core TO receptionist_role;
GRANT INSERT, UPDATE ON core.customer TO receptionist_role;

-- Lead schema
GRANT SELECT, INSERT, UPDATE ON lead.lead TO receptionist_role;
GRANT SELECT, INSERT, UPDATE ON lead.appointment TO receptionist_role;

-- Service schema
GRANT SELECT ON service.service TO receptionist_role;
GRANT SELECT, INSERT ON service.customer_case TO receptionist_role;
GRANT SELECT ON service.case_service TO receptionist_role;
GRANT SELECT ON service.technician_note TO receptionist_role;
GRANT SELECT ON service.case_photo TO receptionist_role;

-- Billing schema
GRANT SELECT, INSERT, UPDATE ON billing.invoice TO receptionist_role;
GRANT SELECT, INSERT ON billing.payment TO receptionist_role;
GRANT SELECT ON billing.point_transaction TO receptionist_role;
GRANT SELECT ON billing.promotion TO receptionist_role;

-- Audit schema
GRANT SELECT ON audit.task TO receptionist_role;
GRANT INSERT ON audit.audit_log TO receptionist_role;

-- Views
GRANT SELECT ON billing.v_daily_revenue TO receptionist_role;
GRANT SELECT ON core.v_customer_lifetime_value TO receptionist_role;
GRANT SELECT ON audit.v_outstanding_tasks TO receptionist_role;

-- =====================================================
-- TECHNICIAN PERMISSIONS
-- =====================================================

-- Core schema
GRANT SELECT ON core.customer TO technician_role;
GRANT SELECT ON core.staff_user TO technician_role;

-- Lead schema
GRANT SELECT ON lead.appointment TO technician_role;

-- Service schema
GRANT SELECT ON service.service TO technician_role;
GRANT SELECT, UPDATE ON service.customer_case TO technician_role;
GRANT SELECT, UPDATE ON service.case_service TO technician_role;
GRANT SELECT, INSERT ON service.technician_note TO technician_role;
GRANT SELECT, INSERT ON service.case_photo TO technician_role;

-- Billing schema - No access to billing

-- Audit schema
GRANT SELECT, UPDATE ON audit.task TO technician_role;
GRANT SELECT ON audit.retouch_schedule TO technician_role;
GRANT INSERT ON audit.audit_log TO technician_role;

-- Views
GRANT SELECT ON service.v_technician_performance TO technician_role;
GRANT SELECT ON audit.v_outstanding_tasks TO technician_role;

-- =====================================================
-- MANAGER PERMISSIONS
-- =====================================================

-- Full access to all schemas
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA core TO manager_role;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA lead TO manager_role;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA service TO manager_role;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA billing TO manager_role;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA audit TO manager_role;

-- Grant access to all sequences
GRANT USAGE ON ALL SEQUENCES IN SCHEMA core TO manager_role;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA lead TO manager_role;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA service TO manager_role;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA billing TO manager_role;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA audit TO manager_role;

-- Grant access to all views
GRANT SELECT ON ALL TABLES IN SCHEMA billing TO manager_role;
GRANT SELECT ON ALL TABLES IN SCHEMA core TO manager_role;
GRANT SELECT ON ALL TABLES IN SCHEMA lead TO manager_role;
GRANT SELECT ON ALL TABLES IN SCHEMA service TO manager_role;
GRANT SELECT ON ALL TABLES IN SCHEMA audit TO manager_role;

-- =====================================================
-- ADDITIONAL PERFORMANCE INDEXES
-- =====================================================

-- Full-text search indexes
CREATE INDEX idx_customer_fulltext ON core.customer USING gin(to_tsvector('simple', full_name || ' ' || COALESCE(phone, '') || ' ' || COALESCE(email, '')));
CREATE INDEX idx_lead_fulltext ON lead.lead USING gin(to_tsvector('simple', full_name || ' ' || phone || ' ' || COALESCE(note, '')));
CREATE INDEX idx_service_fulltext ON service.service USING gin(to_tsvector('simple', name || ' ' || COALESCE(description, '')));

-- Composite indexes for common queries
CREATE INDEX idx_invoice_customer_status ON billing.invoice(customer_id, status, created_at DESC);
CREATE INDEX idx_appointment_date_range ON lead.appointment(start_at, end_at) WHERE status NOT IN ('CANCELLED', 'NO_SHOW');
CREATE INDEX idx_case_customer_status ON service.customer_case(customer_id, status, created_at DESC);
CREATE INDEX idx_payment_date_method ON billing.payment(paid_at DESC, method);

-- Partial indexes for active records
CREATE INDEX idx_active_services ON service.service(category, base_price) WHERE is_active = TRUE;
CREATE INDEX idx_active_promotions ON billing.promotion(start_at, end_at) WHERE is_active = TRUE;
CREATE INDEX idx_pending_tasks ON audit.task(assignee_id, due_at) WHERE status = 'OPEN';

-- JSON indexes
CREATE INDEX idx_case_contraindications ON service.customer_case USING gin(contraindication_flags);
CREATE INDEX idx_tier_benefits ON core.tier USING gin(benefits);
CREATE INDEX idx_promotion_conditions ON billing.promotion USING gin(conditions);

-- =====================================================
-- MATERIALIZED VIEWS FOR REPORTING
-- =====================================================

-- Materialized view for dashboard KPIs
CREATE MATERIALIZED VIEW core.mv_dashboard_kpis AS
WITH date_range AS (
    SELECT CURRENT_DATE - INTERVAL '30 days' as start_date,
           CURRENT_DATE as end_date
)
SELECT 
    -- Customer metrics
    (SELECT COUNT(*) FROM core.customer WHERE created_at >= (SELECT start_date FROM date_range)) as new_customers_30d,
    (SELECT COUNT(*) FROM core.customer) as total_customers,
    
    -- Lead metrics
    (SELECT COUNT(*) FROM lead.lead WHERE created_at >= (SELECT start_date FROM date_range)) as new_leads_30d,
    (SELECT COUNT(*) FROM lead.lead WHERE status = 'WON' AND updated_at >= (SELECT start_date FROM date_range)) as converted_leads_30d,
    
    -- Revenue metrics
    (SELECT SUM(grand_total) FROM billing.invoice WHERE status = 'PAID' AND paid_at >= (SELECT start_date FROM date_range)) as revenue_30d,
    (SELECT AVG(grand_total) FROM billing.invoice WHERE status = 'PAID' AND paid_at >= (SELECT start_date FROM date_range)) as avg_invoice_30d,
    
    -- Service metrics
    (SELECT COUNT(*) FROM service.customer_case WHERE created_at >= (SELECT start_date FROM date_range)) as services_performed_30d,
    
    -- Current timestamp
    CURRENT_TIMESTAMP as last_updated;

-- Create index on materialized view
CREATE UNIQUE INDEX idx_mv_dashboard_kpis ON core.mv_dashboard_kpis(last_updated);

-- Function to refresh materialized views
CREATE OR REPLACE FUNCTION refresh_materialized_views()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY core.mv_dashboard_kpis;
END;
$$ LANGUAGE plpgsql;

-- Schedule refresh (would typically be done via cron job or scheduler)
COMMENT ON FUNCTION refresh_materialized_views() IS 'Call this function periodically to refresh materialized views';
