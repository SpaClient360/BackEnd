# CRM Spa Database Migration Guide

## Overview

This directory contains database migration scripts for the CRM Spa system using PostgreSQL with multi-schema architecture.

## Schema Architecture

The system uses 5 separate schemas for better modularity:

- **core**: User management, roles, customers, tiers
- **lead**: Lead management and appointments
- **service**: Service catalog, cases, photos, notes
- **billing**: Invoices, payments, points, promotions
- **audit**: Audit logs, tasks, retouch schedules

## Migration Files

1. **V1\_\_create_schemas_and_extensions.sql**

   - Creates all schemas
   - Enables required PostgreSQL extensions

2. **V2\_\_create_core_tables.sql**

   - Role, StaffUser, Tier, Customer tables
   - Basic user and customer management

3. **V3\_\_create_lead_tables.sql**

   - Lead and Appointment tables
   - Lead conversion tracking

4. **V4\_\_create_service_tables.sql**

   - Service catalog
   - Customer cases and service records
   - Technician notes and photos

5. **V5\_\_create_billing_tables.sql**

   - Invoice and Payment tracking
   - Point transactions and loyalty
   - Promotions and discounts

6. **V6\_\_create_audit_tables.sql**

   - Audit logging
   - Task management
   - Retouch scheduling

7. **V7\_\_create_triggers_and_functions.sql**

   - Auto-update timestamps
   - Business rule enforcement
   - Point calculation
   - Tier updates

8. **V8\_\_seed_data.sql**

   - Sample roles, users, services
   - Test customers and cases
   - Demo data

9. **V9\_\_create_views_and_reports.sql**

   - Reporting views
   - KPI dashboards
   - Analytics

10. **V10\_\_create_rbac_and_indexes.sql**
    - Role-based access control
    - Performance indexes
    - Materialized views

## Running Migrations

### Prerequisites

- PostgreSQL 14+
- Database user with CREATE SCHEMA privileges

### Manual Execution

```bash
# Create database
createdb crm_spa

# Run migrations in order
psql -U postgres -d crm_spa -f V1__create_schemas_and_extensions.sql
psql -U postgres -d crm_spa -f V2__create_core_tables.sql
# ... continue for all files
```

### Using Flyway (Recommended)

```bash
# Configure flyway.conf with database connection
flyway migrate
```

### Using Spring Boot

Application will auto-migrate on startup if configured with:

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

## Rollback Scripts

Create down migrations by reversing operations:

```sql
-- Example: V2__create_core_tables_down.sql
DROP TABLE IF EXISTS core.customer CASCADE;
DROP TABLE IF EXISTS core.staff_user CASCADE;
DROP TABLE IF EXISTS core.tier CASCADE;
DROP TABLE IF EXISTS core.role CASCADE;

DROP SEQUENCE IF EXISTS core.customer_seq;
DROP SEQUENCE IF EXISTS core.staff_user_seq;
DROP SEQUENCE IF EXISTS core.tier_seq;
DROP SEQUENCE IF EXISTS core.role_seq;
```

## Key Business Rules

1. **Lead Uniqueness**: One phone number per day to prevent spam
2. **Appointment Overlap**: No double-booking for technicians
3. **Invoice Status**: DRAFT → UNPAID → PAID (no reversal)
4. **Photo Requirements**: Cases need before/after photos to complete
5. **Point Earning**: 1 point per 100 currency units on paid invoices
6. **Tier Updates**: Automatic based on spending and points

## State Machines

### Lead Status

```
NEW → IN_PROGRESS → WON
                 ↘ LOST
```

### Invoice Status

```
DRAFT → UNPAID → PAID
     ↘ VOID
```

### Case Status

```
INTAKE → IN_PROGRESS → DONE
                    ↘ FOLLOW_UP
```

## Security

### Role Permissions

| Entity      | Receptionist | Technician | Manager |
| ----------- | ------------ | ---------- | ------- |
| Customer    | CRUD         | R          | CRUD    |
| Lead        | CRUD         | -          | CRUD    |
| Appointment | CRUD         | R (own)    | CRUD    |
| Case        | CR           | RU         | CRUD    |
| Invoice     | CRU          | -          | CRUD    |
| Payment     | CR           | -          | CRUD    |

### Data Privacy

- Case photos can be anonymized
- Customer data supports GDPR deletion
- Audit trail for all changes

## Performance Considerations

1. **Indexes**: Created for all foreign keys and common queries
2. **Full-text Search**: Vietnamese language support on names/descriptions
3. **Materialized Views**: Dashboard KPIs cached for performance
4. **Partitioning**: Consider for audit_log table at scale

## Monitoring

Key metrics to track:

- Lead conversion rate
- Service popularity
- Technician utilization
- Customer retention
- Revenue by service category

## Backup Strategy

```bash
# Daily backup
pg_dump -U postgres -d crm_spa -F custom -f crm_spa_$(date +%Y%m%d).backup

# Restore
pg_restore -U postgres -d crm_spa_restore crm_spa_20240101.backup
```

## Troubleshooting

### Common Issues

1. **Extension not available**

   ```sql
   -- Check available extensions
   SELECT * FROM pg_available_extensions;
   ```

2. **Permission denied**

   ```sql
   -- Grant schema permissions
   GRANT ALL ON SCHEMA core TO your_user;
   ```

3. **Foreign key violations**
   - Ensure migrations run in correct order
   - Check for orphaned records

## Contact

For issues or questions about the database schema, contact the development team.
