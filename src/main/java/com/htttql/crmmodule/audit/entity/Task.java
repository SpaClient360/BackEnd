package com.htttql.crmmodule.audit.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.TaskStatus;
import com.htttql.crmmodule.common.enums.TaskType;
import com.htttql.crmmodule.core.entity.StaffUser;
import com.htttql.crmmodule.lead.entity.Lead;
import com.htttql.crmmodule.service.entity.CustomerCase;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Task entity for tracking follow-ups and reminders
 * Includes calls, follow-ups, and retouch reminders
 */
@Entity
@Table(name = "task", schema = SchemaConstants.AUDIT_SCHEMA, indexes = {
        @Index(name = "idx_task_assignee", columnList = "assignee_id"),
        @Index(name = "idx_task_lead", columnList = "related_lead_id"),
        @Index(name = "idx_task_case", columnList = "related_case_id"),
        @Index(name = "idx_task_status", columnList = "status"),
        @Index(name = "idx_task_type", columnList = "type"),
        @Index(name = "idx_task_due", columnList = "due_at"),
        @Index(name = "idx_task_assignee_status", columnList = "assignee_id, status, due_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_seq")
    @SequenceGenerator(name = "task_seq", sequenceName = SchemaConstants.AUDIT_SCHEMA + ".task_seq", allocationSize = 1)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TaskType type;

    @Column(name = "due_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime dueAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_task_assignee"))
    private StaffUser assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_lead_id", foreignKey = @ForeignKey(name = "fk_task_lead"))
    private Lead relatedLead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_case_id", foreignKey = @ForeignKey(name = "fk_task_case"))
    private CustomerCase relatedCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.OPEN;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Additional task details
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 3; // 1=High, 2=Medium, 3=Low

    @Column(name = "completed_at", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by", foreignKey = @ForeignKey(name = "fk_task_completed_by"))
    private StaffUser completedBy;

    @Column(name = "reminder_sent")
    @Builder.Default
    private Boolean reminderSent = false;

    @Column(name = "reminder_sent_at", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime reminderSentAt;

    @Column(name = "is_recurring")
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "recurrence_pattern", length = 50)
    private String recurrencePattern;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_name", length = 200)
    private String contactName;

    @PrePersist
    @PreUpdate
    private void validateTask() {
        // Either lead or case must be related (but not necessarily)
        if (priority != null && (priority < 1 || priority > 3)) {
            throw new IllegalArgumentException("Priority must be between 1 and 3");
        }

        // Auto-set completed timestamp when status changes to DONE
        if (status == TaskStatus.DONE && completedAt == null) {
            completedAt = LocalDateTime.now();
        }

        // Extract contact info from related entities if not set
        if (contactPhone == null) {
            if (relatedLead != null) {
                contactPhone = relatedLead.getPhone();
                contactName = relatedLead.getFullName();
            } else if (relatedCase != null && relatedCase.getCustomer() != null) {
                contactPhone = relatedCase.getCustomer().getPhone();
                contactName = relatedCase.getCustomer().getFullName();
            }
        }
    }

    public boolean isOverdue() {
        return status == TaskStatus.OPEN && LocalDateTime.now().isAfter(dueAt);
    }
}
