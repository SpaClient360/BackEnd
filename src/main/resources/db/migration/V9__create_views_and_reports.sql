-- =====================================================
-- VIEWS AND REPORTS
-- =====================================================

-- View: Daily Revenue Report
CREATE OR REPLACE VIEW billing.v_daily_revenue AS
SELECT 
    DATE(i.paid_at) as revenue_date,
    COUNT(DISTINCT i.invoice_id) as invoice_count,
    COUNT(DISTINCT i.customer_id) as customer_count,
    SUM(i.grand_total) as total_revenue,
    SUM(i.discount_total) as total_discount,
    SUM(i.tax_total) as total_tax,
    SUM(p.amount) as total_paid,
    STRING_AGG(DISTINCT p.method, ', ') as payment_methods
FROM billing.invoice i
LEFT JOIN billing.payment p ON i.invoice_id = p.invoice_id
WHERE i.status = 'PAID'
GROUP BY DATE(i.paid_at)
ORDER BY revenue_date DESC;

COMMENT ON VIEW billing.v_daily_revenue IS 'Daily revenue summary';

-- View: Monthly Revenue by Service
CREATE OR REPLACE VIEW billing.v_monthly_revenue_by_service AS
SELECT 
    DATE_TRUNC('month', i.paid_at) as revenue_month,
    s.category,
    s.name as service_name,
    COUNT(DISTINCT cc.case_id) as case_count,
    SUM(cs.line_total) as service_revenue,
    AVG(cs.line_total) as avg_service_price
FROM billing.invoice i
JOIN service.customer_case cc ON i.case_id = cc.case_id
JOIN service.case_service cs ON cc.case_id = cs.case_id
JOIN service.service s ON cs.service_id = s.service_id
WHERE i.status = 'PAID'
GROUP BY DATE_TRUNC('month', i.paid_at), s.category, s.name
ORDER BY revenue_month DESC, service_revenue DESC;

COMMENT ON VIEW billing.v_monthly_revenue_by_service IS 'Monthly revenue breakdown by service';

-- View: Lead Conversion Funnel
CREATE OR REPLACE VIEW lead.v_conversion_funnel AS
WITH lead_stats AS (
    SELECT 
        DATE_TRUNC('month', created_at) as month,
        COUNT(*) as total_leads,
        SUM(CASE WHEN status = 'NEW' THEN 1 ELSE 0 END) as new_leads,
        SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_leads,
        SUM(CASE WHEN status = 'WON' THEN 1 ELSE 0 END) as won_leads,
        SUM(CASE WHEN status = 'LOST' THEN 1 ELSE 0 END) as lost_leads
    FROM lead.lead
    GROUP BY DATE_TRUNC('month', created_at)
),
appointment_stats AS (
    SELECT 
        DATE_TRUNC('month', a.created_at) as month,
        COUNT(DISTINCT a.lead_id) as leads_with_appointment,
        COUNT(*) as total_appointments,
        SUM(CASE WHEN a.status = 'DONE' THEN 1 ELSE 0 END) as completed_appointments
    FROM lead.appointment a
    WHERE a.lead_id IS NOT NULL
    GROUP BY DATE_TRUNC('month', a.created_at)
)
SELECT 
    l.month,
    l.total_leads,
    l.new_leads,
    l.in_progress_leads,
    l.won_leads,
    l.lost_leads,
    COALESCE(a.leads_with_appointment, 0) as leads_with_appointment,
    COALESCE(a.total_appointments, 0) as total_appointments,
    COALESCE(a.completed_appointments, 0) as completed_appointments,
    ROUND((l.won_leads::NUMERIC / NULLIF(l.total_leads, 0)) * 100, 2) as conversion_rate,
    ROUND((COALESCE(a.completed_appointments, 0)::NUMERIC / NULLIF(a.total_appointments, 0)) * 100, 2) as appointment_completion_rate
FROM lead_stats l
LEFT JOIN appointment_stats a ON l.month = a.month
ORDER BY l.month DESC;

COMMENT ON VIEW lead.v_conversion_funnel IS 'Lead to customer conversion funnel analysis';

-- View: Technician Performance
CREATE OR REPLACE VIEW service.v_technician_performance AS
WITH technician_cases AS (
    SELECT 
        tn.technician_id,
        DATE_TRUNC('month', tn.created_at) as month,
        COUNT(DISTINCT tn.case_id) as cases_handled,
        AVG(tn.satisfaction_rating) as avg_satisfaction
    FROM service.technician_note tn
    GROUP BY tn.technician_id, DATE_TRUNC('month', tn.created_at)
),
technician_revenue AS (
    SELECT 
        a.technician_id,
        DATE_TRUNC('month', i.paid_at) as month,
        COUNT(DISTINCT i.invoice_id) as invoices_generated,
        SUM(i.grand_total) as revenue_generated
    FROM lead.appointment a
    JOIN service.customer_case cc ON a.appt_id = cc.case_id
    JOIN billing.invoice i ON cc.case_id = i.case_id
    WHERE i.status = 'PAID'
    GROUP BY a.technician_id, DATE_TRUNC('month', i.paid_at)
)
SELECT 
    s.staff_id,
    s.full_name as technician_name,
    tc.month,
    COALESCE(tc.cases_handled, 0) as cases_handled,
    COALESCE(tc.avg_satisfaction, 0) as avg_satisfaction,
    COALESCE(tr.invoices_generated, 0) as invoices_generated,
    COALESCE(tr.revenue_generated, 0) as revenue_generated,
    ROUND(COALESCE(tr.revenue_generated, 0) / NULLIF(tc.cases_handled, 0), 2) as avg_revenue_per_case
FROM core.staff_user s
JOIN core.role r ON s.role_id = r.role_id
LEFT JOIN technician_cases tc ON s.staff_id = tc.technician_id
LEFT JOIN technician_revenue tr ON s.staff_id = tr.technician_id AND tc.month = tr.month
WHERE r.code = 'TECHNICIAN'
ORDER BY tc.month DESC, revenue_generated DESC;

COMMENT ON VIEW service.v_technician_performance IS 'Technician performance metrics';

-- View: Customer Lifetime Value
CREATE OR REPLACE VIEW core.v_customer_lifetime_value AS
WITH customer_stats AS (
    SELECT 
        c.customer_id,
        c.full_name,
        c.phone,
        c.email,
        c.created_at as customer_since,
        t.code as tier,
        c.total_spent,
        c.total_points,
        COUNT(DISTINCT cc.case_id) as total_visits,
        COUNT(DISTINCT i.invoice_id) as total_purchases,
        MAX(i.paid_at) as last_purchase_date,
        AVG(i.grand_total) as avg_purchase_value
    FROM core.customer c
    JOIN core.tier t ON c.tier_id = t.tier_id
    LEFT JOIN service.customer_case cc ON c.customer_id = cc.customer_id
    LEFT JOIN billing.invoice i ON c.customer_id = i.customer_id AND i.status = 'PAID'
    GROUP BY c.customer_id, c.full_name, c.phone, c.email, c.created_at, t.code, c.total_spent, c.total_points
)
SELECT 
    *,
    CASE 
        WHEN last_purchase_date IS NULL THEN 'Never'
        WHEN last_purchase_date > CURRENT_DATE - INTERVAL '30 days' THEN 'Active'
        WHEN last_purchase_date > CURRENT_DATE - INTERVAL '90 days' THEN 'At Risk'
        ELSE 'Churned'
    END as customer_status,
    EXTRACT(DAYS FROM AGE(COALESCE(last_purchase_date, customer_since), customer_since)) as customer_lifetime_days
FROM customer_stats
ORDER BY total_spent DESC;

COMMENT ON VIEW core.v_customer_lifetime_value IS 'Customer lifetime value and status analysis';

-- View: Service Popularity
CREATE OR REPLACE VIEW service.v_service_popularity AS
SELECT 
    s.service_id,
    s.code,
    s.name,
    s.category,
    s.base_price,
    COUNT(DISTINCT cs.case_id) as times_performed,
    COUNT(DISTINCT cc.customer_id) as unique_customers,
    SUM(cs.line_total) as total_revenue,
    AVG(cs.line_total) as avg_selling_price,
    SUM(cs.discount_amount) as total_discounts_given,
    MAX(cc.created_at) as last_performed
FROM service.service s
LEFT JOIN service.case_service cs ON s.service_id = cs.service_id
LEFT JOIN service.customer_case cc ON cs.case_id = cc.case_id
WHERE s.is_active = TRUE
GROUP BY s.service_id, s.code, s.name, s.category, s.base_price
ORDER BY times_performed DESC;

COMMENT ON VIEW service.v_service_popularity IS 'Service popularity and revenue analysis';

-- View: Outstanding Tasks
CREATE OR REPLACE VIEW audit.v_outstanding_tasks AS
SELECT 
    t.task_id,
    t.title,
    t.type,
    t.due_at,
    t.priority,
    t.status,
    s.full_name as assignee_name,
    l.full_name as lead_name,
    l.phone as lead_phone,
    c.full_name as customer_name,
    c.phone as customer_phone,
    CASE 
        WHEN t.due_at < CURRENT_TIMESTAMP THEN 'Overdue'
        WHEN t.due_at < CURRENT_TIMESTAMP + INTERVAL '1 day' THEN 'Due Today'
        WHEN t.due_at < CURRENT_TIMESTAMP + INTERVAL '3 days' THEN 'Due Soon'
        ELSE 'Future'
    END as urgency
FROM audit.task t
JOIN core.staff_user s ON t.assignee_id = s.staff_id
LEFT JOIN lead.lead l ON t.related_lead_id = l.lead_id
LEFT JOIN service.customer_case cc ON t.related_case_id = cc.case_id
LEFT JOIN core.customer c ON cc.customer_id = c.customer_id
WHERE t.status = 'OPEN'
ORDER BY t.priority ASC, t.due_at ASC;

COMMENT ON VIEW audit.v_outstanding_tasks IS 'Outstanding tasks by priority and due date';

-- View: Customer Retention Analysis
CREATE OR REPLACE VIEW core.v_customer_retention AS
WITH customer_cohorts AS (
    SELECT 
        c.customer_id,
        DATE_TRUNC('month', c.created_at) as cohort_month,
        c.created_at as first_visit
    FROM core.customer c
),
customer_activity AS (
    SELECT 
        i.customer_id,
        DATE_TRUNC('month', i.paid_at) as activity_month
    FROM billing.invoice i
    WHERE i.status = 'PAID'
)
SELECT 
    cc.cohort_month,
    COUNT(DISTINCT cc.customer_id) as cohort_size,
    COUNT(DISTINCT CASE 
        WHEN ca.activity_month = cc.cohort_month THEN ca.customer_id 
    END) as month_0,
    COUNT(DISTINCT CASE 
        WHEN ca.activity_month = cc.cohort_month + INTERVAL '1 month' THEN ca.customer_id 
    END) as month_1,
    COUNT(DISTINCT CASE 
        WHEN ca.activity_month = cc.cohort_month + INTERVAL '2 months' THEN ca.customer_id 
    END) as month_2,
    COUNT(DISTINCT CASE 
        WHEN ca.activity_month = cc.cohort_month + INTERVAL '3 months' THEN ca.customer_id 
    END) as month_3,
    COUNT(DISTINCT CASE 
        WHEN ca.activity_month >= cc.cohort_month + INTERVAL '6 months' THEN ca.customer_id 
    END) as month_6_plus
FROM customer_cohorts cc
LEFT JOIN customer_activity ca ON cc.customer_id = ca.customer_id
GROUP BY cc.cohort_month
ORDER BY cc.cohort_month DESC;

COMMENT ON VIEW core.v_customer_retention IS 'Customer retention cohort analysis';
