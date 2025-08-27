package com.htttql.crmmodule.service.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.PhotoType;
import com.htttql.crmmodule.core.entity.StaffUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Case photo entity for before/after service photos
 * Critical for tracking service results and quality control
 */
@Entity
@Table(name = "case_photo", schema = SchemaConstants.SERVICE_SCHEMA, indexes = {
        @Index(name = "idx_case_photo_case", columnList = "case_id"),
        @Index(name = "idx_case_photo_type", columnList = "type"),
        @Index(name = "idx_case_photo_taken_by", columnList = "taken_by"),
        @Index(name = "idx_case_photo_taken_at", columnList = "taken_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CasePhoto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "photo_seq")
    @SequenceGenerator(name = "photo_seq", sequenceName = SchemaConstants.SERVICE_SCHEMA
            + ".case_photo_seq", allocationSize = 1)
    @Column(name = "photo_id")
    private Long photoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false, foreignKey = @ForeignKey(name = "fk_photo_case"))
    private CustomerCase customerCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PhotoType type;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "taken_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime takenAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taken_by", nullable = false, foreignKey = @ForeignKey(name = "fk_photo_staff"))
    private StaffUser takenBy;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Additional metadata
    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    // Privacy and compliance
    @Column(name = "consent_for_marketing")
    @Builder.Default
    private Boolean consentForMarketing = false;

    @Column(name = "anonymized")
    @Builder.Default
    private Boolean anonymized = false;

    @Column(name = "deletion_requested_at", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime deletionRequestedAt;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (takenAt == null) {
            takenAt = LocalDateTime.now();
        }
    }

    @PostPersist
    @PostUpdate
    private void updateCasePhotoFlags() {
        // This would typically be handled by a service layer
        // Update the CustomerCase hasBeforePhoto/hasAfterPhoto flags
    }
}
