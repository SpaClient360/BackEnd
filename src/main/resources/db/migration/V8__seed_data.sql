-- =====================================================
-- SEED DATA
-- =====================================================

-- Insert Roles
INSERT INTO core.role (code, name, description) VALUES
('RECEPTIONIST', 'Receptionist', 'Front desk staff - handles leads, appointments, invoicing'),
('TECHNICIAN', 'Technician', 'Service provider - performs treatments'),
('MANAGER', 'Manager', 'System administrator - full access');

-- Insert Tiers
INSERT INTO core.tier (code, min_spent, min_points, benefits) VALUES
('REGULAR', 0, 0, '{"discount": 0, "points_multiplier": 1.0, "priority_booking": false}'::jsonb),
('SILVER', 1000000, 100, '{"discount": 5, "points_multiplier": 1.2, "priority_booking": false}'::jsonb),
('GOLD', 5000000, 500, '{"discount": 10, "points_multiplier": 1.5, "priority_booking": true}'::jsonb),
('VIP', 10000000, 1000, '{"discount": 15, "points_multiplier": 2.0, "priority_booking": true, "free_retouch": true}'::jsonb);

-- Insert Staff Users (password is 'password123' hashed)
INSERT INTO core.staff_user (full_name, phone, email, password_hash, status, role_id) VALUES
('Nguyễn Thị Lễ Tân', '0901234567', 'lettan@spa.vn', '$2a$10$x8X0HL8D8.YnVkz.password123hash', 'ACTIVE', 
    (SELECT role_id FROM core.role WHERE code = 'RECEPTIONIST')),
('Trần Thị Kỹ Thuật', '0901234568', 'kythuat@spa.vn', '$2a$10$x8X0HL8D8.YnVkz.password123hash', 'ACTIVE', 
    (SELECT role_id FROM core.role WHERE code = 'TECHNICIAN')),
('Lê Văn Quản Lý', '0901234569', 'quanly@spa.vn', '$2a$10$x8X0HL8D8.YnVkz.password123hash', 'ACTIVE', 
    (SELECT role_id FROM core.role WHERE code = 'MANAGER'));

-- Insert Services
INSERT INTO service.service (code, name, category, base_price, duration_min, description, requires_consultation, retouch_days, warranty_days) VALUES
('LIP_POWDER', 'Phun môi Powder Lips', 'LIP', 3500000, 120, 'Phun môi công nghệ Powder Lips - màu tự nhiên, lâu trôi', TRUE, 30, 365),
('LIP_BABY', 'Phun môi Baby Lips', 'LIP', 4500000, 150, 'Phun môi Baby Lips - môi hồng tự nhiên như em bé', TRUE, 30, 365),
('BROW_NATURAL', 'Điêu khắc chân mày tự nhiên', 'BROW', 2500000, 90, 'Điêu khắc chân mày phong cách tự nhiên', FALSE, 30, 180),
('BROW_OMBRE', 'Phun mày Ombre', 'BROW', 3000000, 120, 'Phun mày hiệu ứng Ombre - đậm nhạt tự nhiên', TRUE, 30, 365),
('CONSULTATION', 'Tư vấn miễn phí', 'OTHER', 0, 30, 'Tư vấn và thiết kế dáng phù hợp', FALSE, NULL, NULL);

-- Insert Leads
INSERT INTO lead.lead (full_name, phone, note, source, status, assigned_to, utm_source, utm_medium, utm_campaign) VALUES
('Phạm Thị Hương', '0912345678', 'Quan tâm phun môi, muốn tư vấn màu phù hợp', 'SEO', 'NEW', 
    (SELECT staff_id FROM core.staff_user WHERE email = 'lettan@spa.vn'), 'google', 'organic', 'lip-service'),
('Nguyễn Thị Mai', '0923456789', 'Muốn làm chân mày, có da nhạy cảm', 'SEO', 'IN_PROGRESS', 
    (SELECT staff_id FROM core.staff_user WHERE email = 'lettan@spa.vn'), 'facebook', 'social', 'brow-promo'),
('Trần Thị Lan', '0934567890', 'Đã từng phun môi nơi khác, muốn làm lại', 'SEO', 'WON', 
    (SELECT staff_id FROM core.staff_user WHERE email = 'lettan@spa.vn'), 'google', 'cpc', 'retouch-campaign'),
('Lê Thị Hoa', '0945678901', 'Hỏi giá dịch vụ', 'SEO', 'LOST', 
    (SELECT staff_id FROM core.staff_user WHERE email = 'lettan@spa.vn'), NULL, NULL, NULL),
('Vũ Thị Sen', '0956789012', 'Muốn book lịch phun mày và môi cùng lúc', 'SEO', 'NEW', 
    NULL, 'zalo', 'social', 'combo-offer');

-- Insert Customers (from converted leads)
INSERT INTO core.customer (full_name, phone, email, dob, gender, address, tier_id, notes) VALUES
('Trần Thị Lan', '0934567890', 'lan.tran@email.com', '1992-05-15', 'FEMALE', 
    '123 Nguyễn Huệ, Q1, TP.HCM', (SELECT tier_id FROM core.tier WHERE code = 'REGULAR'),
    'Khách hàng cũ, đã từng phun môi nơi khác'),
('Hoàng Thị Kim', '0967890123', 'kim.hoang@email.com', '1988-10-20', 'FEMALE', 
    '456 Lê Lợi, Q3, TP.HCM', (SELECT tier_id FROM core.tier WHERE code = 'SILVER'),
    'Khách VIP, giới thiệu nhiều khách mới');

-- Insert Customer Cases
INSERT INTO service.customer_case (customer_id, primary_service_id, intake_note, contraindication_flags, consent_signed_at, status, has_before_photo, has_after_photo) VALUES
((SELECT customer_id FROM core.customer WHERE phone = '0934567890'),
 (SELECT service_id FROM service.service WHERE code = 'LIP_POWDER'),
 'Khách đã từng phun môi cách đây 2 năm, màu cũ đã nhạt. Mong muốn màu hồng tự nhiên.',
 '{"allergies": [], "medications": [], "conditions": ["none"]}'::jsonb,
 CURRENT_TIMESTAMP - INTERVAL '7 days',
 'DONE', TRUE, TRUE),
((SELECT customer_id FROM core.customer WHERE phone = '0967890123'),
 (SELECT service_id FROM service.service WHERE code = 'BROW_OMBRE'),
 'Khách có chân mày thưa, muốn dáng mày cong tự nhiên.',
 '{"allergies": ["lidocaine"], "medications": [], "conditions": ["sensitive_skin"]}'::jsonb,
 CURRENT_TIMESTAMP - INTERVAL '1 day',
 'IN_PROGRESS', FALSE, FALSE);

-- Insert Case Services
INSERT INTO service.case_service (case_id, service_id, unit_price, qty, discount_amount, tax_amount, status) VALUES
((SELECT case_id FROM service.customer_case WHERE customer_id = (SELECT customer_id FROM core.customer WHERE phone = '0934567890')), 
 (SELECT service_id FROM service.service WHERE code = 'LIP_POWDER'), 3500000, 1, 0, 0, 'DONE'),
((SELECT case_id FROM service.customer_case WHERE customer_id = (SELECT customer_id FROM core.customer WHERE phone = '0967890123')), 
 (SELECT service_id FROM service.service WHERE code = 'BROW_OMBRE'), 3000000, 1, 150000, 0, 'IN_PROGRESS');

-- Insert Invoices
INSERT INTO billing.invoice (case_id, customer_id, subtotal, discount_total, tax_total, status, invoice_number) VALUES
((SELECT case_id FROM service.customer_case WHERE customer_id = (SELECT customer_id FROM core.customer WHERE phone = '0934567890')), 
 (SELECT customer_id FROM core.customer WHERE phone = '0934567890'), 
 3500000, 0, 0, 'PAID', 'INV-2024-00001');

-- Insert Payment
INSERT INTO billing.payment (invoice_id, method, amount, paid_by, receipt_number) VALUES
((SELECT invoice_id FROM billing.invoice WHERE invoice_number = 'INV-2024-00001'), 'CARD', 3500000, 
 (SELECT staff_id FROM core.staff_user WHERE email = 'lettan@spa.vn'), 'RCP-2024-00001');

-- Insert Promotion
INSERT INTO billing.promotion (code, name, type, value, start_at, end_at, min_purchase_amount, description) VALUES
('NEWCUSTOMER', 'Khuyến mãi khách hàng mới', 'PERCENT', 10, 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', 2000000,
 'Giảm 10% cho khách hàng lần đầu sử dụng dịch vụ'),
('SUMMER2024', 'Khuyến mãi hè 2024', 'AMOUNT', 500000, 
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '60 days', 3000000,
 'Giảm 500k cho hóa đơn từ 3 triệu');

-- Insert sample appointments
INSERT INTO lead.appointment (lead_id, service_id, technician_id, receptionist_id, start_at, end_at, status) VALUES
((SELECT lead_id FROM lead.lead WHERE phone = '0923456789'),
 (SELECT service_id FROM service.service WHERE code = 'CONSULTATION'),
 (SELECT staff_id FROM core.staff_user WHERE email = 'kythuat@spa.vn'),
 (SELECT staff_id FROM core.staff_user WHERE email = 'lettan@spa.vn'),
 CURRENT_TIMESTAMP + INTERVAL '2 days',
 CURRENT_TIMESTAMP + INTERVAL '2 days' + INTERVAL '30 minutes',
 'SCHEDULED');

-- Update sequences to continue from inserted data
SELECT setval('core.role_seq', (SELECT MAX(role_id) FROM core.role));
SELECT setval('core.staff_user_seq', (SELECT MAX(staff_id) FROM core.staff_user));
SELECT setval('core.tier_seq', (SELECT MAX(tier_id) FROM core.tier));
SELECT setval('core.customer_seq', (SELECT MAX(customer_id) FROM core.customer));
SELECT setval('lead.lead_seq', (SELECT MAX(lead_id) FROM lead.lead));
SELECT setval('lead.appointment_seq', (SELECT MAX(appt_id) FROM lead.appointment));
SELECT setval('service.service_seq', (SELECT MAX(service_id) FROM service.service));
SELECT setval('service.customer_case_seq', (SELECT MAX(case_id) FROM service.customer_case));
SELECT setval('service.case_service_seq', (SELECT MAX(case_service_id) FROM service.case_service));
SELECT setval('billing.invoice_seq', (SELECT MAX(invoice_id) FROM billing.invoice));
SELECT setval('billing.payment_seq', (SELECT MAX(payment_id) FROM billing.payment));
SELECT setval('billing.promotion_seq', (SELECT MAX(promo_id) FROM billing.promotion));
