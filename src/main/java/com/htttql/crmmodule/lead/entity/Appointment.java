package com.htttql.crmmodule.lead.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.AppointmentStatus;
import com.htttql.crmmodule.core.entity.Customer;
import com.htttql.crmmodule.core.entity.StaffUser;
import com.htttql.crmmodule.service.entity.SpaService;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Appointment entity for booking spa services
 * Tracks appointments from leads or existing customers
 */
@Entity
@Table(name = "appointment", schema = SchemaConstants.LEAD_SCHEMA, indexes = {
        @Index(name = "idx_appt_lead", columnList = "lead_id"),
        @Index(name = "idx_appt_customer", columnList = "customer_id"),
        @Index(name = "idx_appt_technician", columnList = "technician_id"),
        @Index(name = "idx_appt_start", columnList = "start_at"),
        @Index(name = "idx_appt_status", columnList = "status"),
        @Index(name = "idx_appt_tech_time", columnList = "technician_id, start_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "appt_seq")
    @SequenceGenerator(name = "appt_seq", sequenceName = SchemaConstants.LEAD_SCHEMA
            + ".appointment_seq", allocationSize = 1)
    @Column(name = "appt_id")
    private Long apptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", foreignKey = @ForeignKey(name = "fk_appt_lead"))
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name = "fk_appt_customer"))
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", foreignKey = @ForeignKey(name = "fk_appt_service"))
    private SpaService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false, foreignKey = @ForeignKey(name = "fk_appt_technician"))
    private StaffUser technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receptionist_id", nullable = false, foreignKey = @ForeignKey(name = "fk_appt_receptionist"))
    private StaffUser receptionist;

    @Column(name = "start_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Additional fields for tracking
    @Column(name = "reminder_sent")
    @Builder.Default
    private Boolean reminderSent = false;

    @Column(name = "confirmed_at", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_reason", columnDefinition = "TEXT")
    private String cancelledReason;

    @PrePersist
    @PreUpdate
    private void validateAppointment() {
        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Either lead or customer must be present
        if (lead == null && customer == null) {
            throw new IllegalArgumentException("Either lead or customer must be specified");
        }
    }
}
