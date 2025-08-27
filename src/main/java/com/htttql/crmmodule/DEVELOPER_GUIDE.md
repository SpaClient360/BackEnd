# CRM SPA Developer Quick Reference

## Project Structure

```
src/main/java/com/htttql/crmmodule/
├── common/              # Shared components
│   ├── config/         # Configuration classes
│   ├── entity/         # Base entities
│   ├── enums/          # All enumerations
│   └── util/           # Utility classes
├── core/               # Core module
│   ├── entity/         # Role, StaffUser, Customer, Tier
│   ├── repository/     # JPA repositories
│   ├── service/        # Business logic
│   └── dto/            # Data transfer objects
├── lead/               # Lead management
│   ├── entity/         # Lead, Appointment
│   ├── repository/
│   ├── service/
│   └── dto/
├── service/            # Service management
│   ├── entity/         # Service, Case, Photo, Note
│   ├── repository/
│   ├── service/
│   └── dto/
├── billing/            # Billing module
│   ├── entity/         # Invoice, Payment, Points
│   ├── repository/
│   ├── service/
│   └── dto/
└── audit/              # Audit module
    ├── entity/         # AuditLog, Task, Retouch
    ├── repository/
    ├── service/
    └── dto/
```

## Quick Entity Reference

### Core Module

```java
@Entity @Table(schema = "core")
- Role (RECEPTIONIST, TECHNICIAN, MANAGER)
- StaffUser (system users)
- Customer (spa clients)
- Tier (REGULAR, SILVER, GOLD, VIP)
```

### Lead Module

```java
@Entity @Table(schema = "lead")
- Lead (NEW → IN_PROGRESS → WON/LOST)
- Appointment (SCHEDULED → CONFIRMED → DONE)
```

### Service Module

```java
@Entity @Table(schema = "service")
- Service (LIP, BROW, OTHER)
- CustomerCase (INTAKE → IN_PROGRESS → DONE)
- CaseService (services in a case)
- TechnicianNote (technical observations)
- CasePhoto (BEFORE/AFTER)
```

### Billing Module

```java
@Entity @Table(schema = "billing")
- Invoice (DRAFT → UNPAID → PAID)
- Payment (CASH, CARD, BANK, EWALLET)
- PointTransaction (EARN, REDEEM, ADJUST)
- Promotion (PERCENT, AMOUNT)
```

### Audit Module

```java
@Entity @Table(schema = "audit")
- AuditLog (all system changes)
- Task (CALL, FOLLOW_UP, RETOUCH_REMINDER)
- RetouchSchedule (PLANNED → DONE/MISSED)
```

## Common Patterns

### 1. Base Entity Extension

```java
@Entity
@Table(name = "your_table", schema = SchemaConstants.YOUR_SCHEMA)
public class YourEntity extends BaseEntity {
    // BaseEntity provides: createdAt, updatedAt
}
```

### 2. Enum Usage

```java
@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
private LeadStatus status = LeadStatus.NEW;
```

### 3. JSONB Fields

```java
@Type(JsonType.class)
@Column(name = "benefits", columnDefinition = "jsonb")
private Map<String, Object> benefits;
```

### 4. Multi-Schema Queries

```java
@Query("SELECT l FROM Lead l WHERE l.status = :status")
List<Lead> findByStatus(@Param("status") LeadStatus status);
```

### 5. Audit Logging

```java
// Automatic via triggers, or manual:
AuditLog.forEntity("lead.lead", leadId)
    .actor(currentUser)
    .action(AuditAction.UPDATE)
    .build();
```

## Business Rules Implementation

### 1. Lead Spam Prevention

```java
// Unique constraint enforced at DB level
@UniqueConstraint(name = "uk_lead_phone_date",
    columnNames = {"phone", "created_date"})
```

### 2. Appointment Overlap Prevention

```java
// EXCLUDE constraint in PostgreSQL
// No overlapping appointments for same technician
```

### 3. Invoice Status Validation

```java
// Trigger validates transitions:
// DRAFT → UNPAID → PAID (no reversal)
// Cannot VOID if payments exist
```

### 4. Automatic Point Calculation

```java
// Trigger on invoice status = PAID:
// - Creates PointTransaction
// - Updates customer points/spending
// - Checks tier upgrade
```

### 5. Photo Requirements

```java
// Case cannot be DONE without both photos
@Check("status != 'DONE' OR (has_before_photo AND has_after_photo)")
```

## Repository Examples

### 1. Basic Repository

```java
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);
    List<Customer> findByTierCode(TierCode code);
}
```

### 2. Custom Query

```java
@Query(value = "SELECT * FROM lead.v_conversion_funnel WHERE month = :month",
       nativeQuery = true)
ConversionStats getConversionStats(@Param("month") LocalDate month);
```

### 3. Specification Query

```java
Specification<Lead> spec = Specification
    .where(LeadSpecs.hasStatus(LeadStatus.NEW))
    .and(LeadSpecs.createdAfter(LocalDate.now().minusDays(7)));
```

## Service Layer Patterns

### 1. Transaction Management

```java
@Service
@Transactional
public class InvoiceService {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Invoice processPayment(Long invoiceId, PaymentDto payment) {
        // Prevents concurrent payment processing
    }
}
```

### 2. Event Publishing

```java
@EventListener
public void handleInvoicePaid(InvoicePaidEvent event) {
    // Create point transaction
    // Send email receipt
    // Update customer tier
}
```

### 3. Validation

```java
@Validated
public class LeadService {
    public Lead createLead(@Valid LeadDto dto) {
        // DTO validation via Bean Validation
    }
}
```

## Security Integration

### 1. Get Current User

```java
@Service
public class SecurityService {
    public StaffUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return staffUserRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new UnauthorizedException());
    }
}
```

### 2. Role-Based Access

```java
@PreAuthorize("hasRole('MANAGER')")
public void deleteService(Long serviceId) {
    // Only managers can delete
}
```

### 3. Data Filtering

```java
@PostFilter("filterObject.assignee.email == authentication.name or hasRole('MANAGER')")
public List<Task> getTasks() {
    // Users see only their tasks, managers see all
}
```

## Testing

### 1. Repository Test

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Sql(scripts = "/test-data.sql")
class CustomerRepositoryTest {
    @Test
    void findByPhone_shouldReturnCustomer() {
        // Test with real PostgreSQL via Testcontainers
    }
}
```

### 2. Service Test

```java
@SpringBootTest
@Transactional
@Rollback
class InvoiceServiceTest {
    @MockBean
    private EmailService emailService;

    @Test
    void processPayment_shouldCreatePointTransaction() {
        // Test business logic with mocked dependencies
    }
}
```

## Performance Tips

1. **Use Projections**

   ```java
   interface CustomerSummary {
       String getFullName();
       String getPhone();
       BigDecimal getTotalSpent();
   }
   ```

2. **Batch Operations**

   ```java
   @Modifying
   @Query("UPDATE Lead l SET l.status = :status WHERE l.id IN :ids")
   void updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") LeadStatus status);
   ```

3. **Lazy Loading**

   ```java
   @OneToMany(fetch = FetchType.LAZY)
   private List<CasePhoto> photos;
   ```

4. **Query Hints**
   ```java
   @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
   List<Service> findActiveServices();
   ```

## Common Issues

1. **N+1 Queries**: Use `@EntityGraph` or `JOIN FETCH`
2. **Schema Not Found**: Ensure schemas are created before JPA init
3. **JSONB Mapping**: Add hypersistence-utils dependency
4. **Timezone Issues**: Use `TIMESTAMPTZ` and configure JVM timezone
5. **Transaction Timeout**: Adjust for long-running operations

## Useful Queries

```sql
-- Check schema sizes
SELECT schema_name,
       pg_size_pretty(sum(pg_total_relation_size(quote_ident(schema_name)||'.'||quote_ident(table_name)))) as size
FROM information_schema.tables
WHERE schema_name IN ('core','lead','service','billing','audit')
GROUP BY schema_name;

-- Active sessions
SELECT pid, usename, application_name, client_addr, state, query
FROM pg_stat_activity
WHERE datname = 'crm_spa';

-- Slow queries
SELECT query, calls, mean_exec_time
FROM pg_stat_statements
WHERE dbid = (SELECT oid FROM pg_database WHERE datname = 'crm_spa')
ORDER BY mean_exec_time DESC
LIMIT 10;
```
