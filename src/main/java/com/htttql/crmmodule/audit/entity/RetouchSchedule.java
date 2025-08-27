package com.htttql.crmmodule.audit.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.RetouchStatus;
import com.htttql.crmmodule.service.entity.CustomerCase;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Retouch schedule entity for tracking service touch-ups
 * Manages scheduled retouch appointments for services like lip/brow tattoos
 */
@Entity
@Table(name = "retouch_schedule", schema = SchemaConstants.AUDIT_SCHEMA, indexes = {
        @Index(name = "idx_retouch_case", columnList = "case_id"),
        @Index(name = "idx_retouch_status", columnList = "status"),
        @Index(name = "idx_retouch_due", columnList = "due_at"),
        @Index(name = "idx_retouch_created", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetouchSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "retouch_seq")
    @SequenceGenerator(name = "retouch_seq", sequenceName = SchemaConstants.AUDIT_SCHEMA
            + ".retouch_schedule_seq", allocationSize = 1)
    @Column(name = "retouch_id")
    private Long retouchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false, foreignKey = @ForeignKey(name = "fk_retouch_case"))
    private CustomerCase customerCase;

    @Column(name = "due_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime dueAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RetouchStatus status = RetouchStatus.PLANNED;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Additional scheduling details
    @Column(name = "scheduled_at", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime scheduledAt;

    @Column(name = "completed_at", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime completedAt;

    @Column(name = "reminder_sent")
    @Builder.Default
    private Boolean reminderSent = false;

    @Column(name = "reminder_sent_at", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime reminderSentAt;

    @Column(name = "days_before_reminder")
    @Builder.Default
    private Integer daysBeforeReminder = 3;

    @Column(name = "is_free_retouch")
    @Builder.Default
    private Boolean isFreeRetouch = true;

    @Column(name = "discount_percentage")
    private Integer discountPercentage;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "rescheduled_from", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime rescheduledFrom;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        // Calculate due date based on service type if not set
        if (dueAt == null && customerCase != null && customerCase.getPrimaryService() != null) {
            Integer retouchDays = customerCase.getPrimaryService().getRetouchDays();
            if (retouchDays != null) {
                dueAt = LocalDateTime.now().plusDays(retouchDays);
            }
        }
    }

    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
        // Auto-set completed timestamp when status changes to DONE
        if (status == RetouchStatus.DONE && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }

    public boolean shouldSendReminder() {
        if (reminderSent || status != RetouchStatus.PLANNED) {
            return false;
        }

        LocalDateTime reminderDate = dueAt.minusDays(daysBeforeReminder);
        return LocalDateTime.now().isAfter(reminderDate);
    }

    public boolean isOverdue() {
        return status == RetouchStatus.PLANNED && LocalDateTime.now().isAfter(dueAt);
    }
}
