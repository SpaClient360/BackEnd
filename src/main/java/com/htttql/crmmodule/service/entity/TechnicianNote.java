package com.htttql.crmmodule.service.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.core.entity.StaffUser;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.Map;

/**
 * Technician note entity for technical observations during service
 * Records complications, aftercare instructions, and technical details
 */
@Entity
@Table(name = "technician_note", schema = SchemaConstants.SERVICE_SCHEMA, indexes = {
        @Index(name = "idx_tech_note_case", columnList = "case_id"),
        @Index(name = "idx_tech_note_technician", columnList = "technician_id"),
        @Index(name = "idx_tech_note_created", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tech_note_seq")
    @SequenceGenerator(name = "tech_note_seq", sequenceName = SchemaConstants.SERVICE_SCHEMA
            + ".technician_note_seq", allocationSize = 1)
    @Column(name = "tech_note_id")
    private Long techNoteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tech_note_case"))
    private CustomerCase customerCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tech_note_technician"))
    private StaffUser technician;

    @Column(name = "note", columnDefinition = "TEXT", nullable = false)
    private String note;

    @Type(JsonType.class)
    @Column(name = "complications", columnDefinition = "jsonb")
    private Map<String, Object> complications;

    @Type(JsonType.class)
    @Column(name = "aftercare", columnDefinition = "jsonb")
    private Map<String, Object> aftercare;

    // Additional technical details
    @Column(name = "technique_used", length = 200)
    private String techniqueUsed;

    @Column(name = "products_used", columnDefinition = "TEXT")
    private String productsUsed;

    @Column(name = "color_code", length = 50)
    private String colorCode;

    @Column(name = "is_retouch_needed")
    @Builder.Default
    private Boolean isRetouchNeeded = false;

    @Column(name = "retouch_notes", columnDefinition = "TEXT")
    private String retouchNotes;

    @Column(name = "client_feedback", columnDefinition = "TEXT")
    private String clientFeedback;

    @Column(name = "satisfaction_rating")
    private Integer satisfactionRating;

    @PrePersist
    @PreUpdate
    private void validateNote() {
        if (satisfactionRating != null && (satisfactionRating < 1 || satisfactionRating > 5)) {
            throw new IllegalArgumentException("Satisfaction rating must be between 1 and 5");
        }
    }
}
