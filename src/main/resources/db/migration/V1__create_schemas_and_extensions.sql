-- Create schemas for multi-schema architecture
CREATE SCHEMA IF NOT EXISTS core;
CREATE SCHEMA IF NOT EXISTS lead;
CREATE SCHEMA IF NOT EXISTS service;
CREATE SCHEMA IF NOT EXISTS billing;
CREATE SCHEMA IF NOT EXISTS audit;

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "btree_gist"; -- For EXCLUDE constraints

-- Set search path
SET search_path TO core, lead, service, billing, audit, public;

-- Grant permissions on schemas
GRANT USAGE ON SCHEMA core TO PUBLIC;
GRANT USAGE ON SCHEMA lead TO PUBLIC;
GRANT USAGE ON SCHEMA service TO PUBLIC;
GRANT USAGE ON SCHEMA billing TO PUBLIC;
GRANT USAGE ON SCHEMA audit TO PUBLIC;

COMMENT ON SCHEMA core IS 'Core module: Users, Roles, Customers, Tiers';
COMMENT ON SCHEMA lead IS 'Lead management module: Leads, Appointments';
COMMENT ON SCHEMA service IS 'Service module: Services, Cases, Photos';
COMMENT ON SCHEMA billing IS 'Billing module: Invoices, Payments, Points';
COMMENT ON SCHEMA audit IS 'Audit module: Logs, Tasks, Schedules';
