-- =====================================================
-- CRM SPA DATABASE CREATION SCRIPT
-- Run this script to create the complete database
-- =====================================================

-- Create database
DROP DATABASE IF EXISTS crm_spa;
CREATE DATABASE crm_spa
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Connect to the new database
\c crm_spa;

-- Now run all migration scripts in order:
-- \i V1__create_schemas_and_extensions.sql
-- \i V2__create_core_tables.sql
-- \i V3__create_lead_tables.sql
-- \i V4__create_service_tables.sql
-- \i V5__create_billing_tables.sql
-- \i V6__create_audit_tables.sql
-- \i V7__create_triggers_and_functions.sql
-- \i V8__seed_data.sql
-- \i V9__create_views_and_reports.sql
-- \i V10__create_rbac_and_indexes.sql

-- Or use this single command from the migration directory:
-- psql -U postgres -f create_database.sql && for f in V*.sql; do psql -U postgres -d crm_spa -f "$f"; done
