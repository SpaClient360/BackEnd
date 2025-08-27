-- =====================================================
-- TRIGGERS AND FUNCTIONS
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply updated_at trigger to all tables with updated_at column
CREATE TRIGGER update_role_updated_at BEFORE UPDATE ON core.role
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_staff_user_updated_at BEFORE UPDATE ON core.staff_user
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tier_updated_at BEFORE UPDATE ON core.tier
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_customer_updated_at BEFORE UPDATE ON core.customer
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lead_updated_at BEFORE UPDATE ON lead.lead
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_service_updated_at BEFORE UPDATE ON service.service
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_customer_case_updated_at BEFORE UPDATE ON service.customer_case
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_case_service_updated_at BEFORE UPDATE ON service.case_service
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_case_photo_updated_at BEFORE UPDATE ON service.case_photo
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payment_updated_at BEFORE UPDATE ON billing.payment
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_promotion_updated_at BEFORE UPDATE ON billing.promotion
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_task_updated_at BEFORE UPDATE ON audit.task
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_retouch_schedule_updated_at BEFORE UPDATE ON audit.retouch_schedule
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to calculate case_service line_total
CREATE OR REPLACE FUNCTION calculate_case_service_line_total()
RETURNS TRIGGER AS $$
BEGIN
    NEW.line_total = (NEW.unit_price * NEW.qty) - NEW.discount_amount + NEW.tax_amount;
    IF NEW.line_total < 0 THEN
        NEW.line_total = 0;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER calc_case_service_total 
BEFORE INSERT OR UPDATE OF unit_price, qty, discount_amount, tax_amount 
ON service.case_service
FOR EACH ROW EXECUTE FUNCTION calculate_case_service_line_total();

-- Function to update invoice grand_total
CREATE OR REPLACE FUNCTION calculate_invoice_grand_total()
RETURNS TRIGGER AS $$
BEGIN
    NEW.grand_total = NEW.subtotal - NEW.discount_total + NEW.tax_total - NEW.points_value;
    IF NEW.grand_total < 0 THEN
        NEW.grand_total = 0;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER calc_invoice_total 
BEFORE INSERT OR UPDATE OF subtotal, discount_total, tax_total, points_value 
ON billing.invoice
FOR EACH ROW EXECUTE FUNCTION calculate_invoice_grand_total();

-- Function to create point transaction when invoice is paid
CREATE OR REPLACE FUNCTION create_point_transaction_on_payment()
RETURNS TRIGGER AS $$
DECLARE
    v_invoice billing.invoice%ROWTYPE;
    v_customer core.customer%ROWTYPE;
    v_points_earned INTEGER;
BEGIN
    -- Only process when invoice status changes to PAID
    IF NEW.status = 'PAID' AND OLD.status != 'PAID' THEN
        -- Get invoice and customer details
        SELECT * INTO v_invoice FROM billing.invoice WHERE invoice_id = NEW.invoice_id;
        SELECT * INTO v_customer FROM core.customer WHERE customer_id = NEW.customer_id;
        
        -- Calculate points (1 point per 100 currency units)
        v_points_earned := FLOOR(NEW.grand_total / 100);
        
        IF v_points_earned > 0 THEN
            -- Create point transaction
            INSERT INTO billing.point_transaction (
                customer_id, source, points, related_invoice_id,
                balance_before, balance_after, note
            ) VALUES (
                NEW.customer_id, 'EARN', v_points_earned, NEW.invoice_id,
                v_customer.total_points, v_customer.total_points + v_points_earned,
                'Points earned from invoice ' || NEW.invoice_number
            );
            
            -- Update customer total_points and total_spent
            UPDATE core.customer 
            SET total_points = total_points + v_points_earned,
                total_spent = total_spent + NEW.grand_total
            WHERE customer_id = NEW.customer_id;
        END IF;
        
        -- Set paid_at timestamp
        NEW.paid_at = CURRENT_TIMESTAMP;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER invoice_payment_complete 
BEFORE UPDATE OF status ON billing.invoice
FOR EACH ROW EXECUTE FUNCTION create_point_transaction_on_payment();

-- Function to update customer tier based on spending/points
CREATE OR REPLACE FUNCTION update_customer_tier()
RETURNS TRIGGER AS $$
DECLARE
    v_new_tier_id BIGINT;
BEGIN
    -- Find the highest tier the customer qualifies for
    SELECT tier_id INTO v_new_tier_id
    FROM core.tier
    WHERE NEW.total_spent >= min_spent 
      AND NEW.total_points >= min_points
    ORDER BY min_spent DESC, min_points DESC
    LIMIT 1;
    
    -- Update tier if different
    IF v_new_tier_id IS NOT NULL AND v_new_tier_id != NEW.tier_id THEN
        NEW.tier_id = v_new_tier_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER customer_tier_update 
BEFORE UPDATE OF total_spent, total_points ON core.customer
FOR EACH ROW EXECUTE FUNCTION update_customer_tier();

-- Function to update case photo flags
CREATE OR REPLACE FUNCTION update_case_photo_flags()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        -- Update has_before_photo flag
        UPDATE service.customer_case 
        SET has_before_photo = EXISTS (
            SELECT 1 FROM service.case_photo 
            WHERE case_id = NEW.case_id AND type = 'BEFORE'
        )
        WHERE case_id = NEW.case_id;
        
        -- Update has_after_photo flag
        UPDATE service.customer_case 
        SET has_after_photo = EXISTS (
            SELECT 1 FROM service.case_photo 
            WHERE case_id = NEW.case_id AND type = 'AFTER'
        )
        WHERE case_id = NEW.case_id;
    ELSIF TG_OP = 'DELETE' THEN
        -- Update flags after deletion
        UPDATE service.customer_case 
        SET has_before_photo = EXISTS (
            SELECT 1 FROM service.case_photo 
            WHERE case_id = OLD.case_id AND type = 'BEFORE'
        ),
        has_after_photo = EXISTS (
            SELECT 1 FROM service.case_photo 
            WHERE case_id = OLD.case_id AND type = 'AFTER'
        )
        WHERE case_id = OLD.case_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_case_photos_flags 
AFTER INSERT OR UPDATE OR DELETE ON service.case_photo
FOR EACH ROW EXECUTE FUNCTION update_case_photo_flags();

-- Function for audit logging
CREATE OR REPLACE FUNCTION create_audit_log()
RETURNS TRIGGER AS $$
DECLARE
    v_entity_name TEXT;
    v_entity_id BIGINT;
    v_action audit.audit_log.action%TYPE;
    v_old_values JSONB;
    v_new_values JSONB;
BEGIN
    -- Determine entity name from table
    v_entity_name := TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME;
    
    -- Determine action
    IF TG_OP = 'INSERT' THEN
        v_action := 'CREATE';
        v_entity_id := NEW.id; -- Assumes primary key is named 'id'
        v_new_values := to_jsonb(NEW);
    ELSIF TG_OP = 'UPDATE' THEN
        v_action := 'UPDATE';
        v_entity_id := NEW.id;
        v_old_values := to_jsonb(OLD);
        v_new_values := to_jsonb(NEW);
    ELSIF TG_OP = 'DELETE' THEN
        v_action := 'DELETE';
        v_entity_id := OLD.id;
        v_old_values := to_jsonb(OLD);
    END IF;
    
    -- Insert audit log (actor_id should be set by application)
    -- This is a simplified version - in production, actor_id would come from session
    INSERT INTO audit.audit_log (
        actor_id, entity_name, entity_id, action,
        old_values, new_values, created_at
    ) VALUES (
        1, -- Placeholder - should be current user
        v_entity_name, v_entity_id, v_action,
        v_old_values, v_new_values, CURRENT_TIMESTAMP
    );
    
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Helper function to validate invoice status transitions
CREATE OR REPLACE FUNCTION validate_invoice_status_transition()
RETURNS TRIGGER AS $$
BEGIN
    -- Check valid status transitions
    IF OLD.status = 'DRAFT' AND NEW.status NOT IN ('UNPAID', 'VOID') THEN
        RAISE EXCEPTION 'Invalid status transition from DRAFT to %', NEW.status;
    ELSIF OLD.status = 'UNPAID' AND NEW.status NOT IN ('PAID', 'VOID') THEN
        RAISE EXCEPTION 'Invalid status transition from UNPAID to %', NEW.status;
    ELSIF OLD.status = 'PAID' AND NEW.status != 'PAID' THEN
        RAISE EXCEPTION 'Cannot change status from PAID';
    ELSIF OLD.status = 'VOID' AND NEW.status != 'VOID' THEN
        RAISE EXCEPTION 'Cannot change status from VOID';
    END IF;
    
    -- Cannot void invoice with payments
    IF NEW.status = 'VOID' AND EXISTS (
        SELECT 1 FROM billing.payment WHERE invoice_id = NEW.invoice_id
    ) THEN
        RAISE EXCEPTION 'Cannot void invoice with existing payments';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_invoice_status 
BEFORE UPDATE OF status ON billing.invoice
FOR EACH ROW EXECUTE FUNCTION validate_invoice_status_transition();
