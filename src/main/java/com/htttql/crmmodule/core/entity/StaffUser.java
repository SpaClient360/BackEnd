package com.htttql.crmmodule.core.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.StaffStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff_user", schema = SchemaConstants.CORE_SCHEMA, indexes = {
        @Index(name = "idx_staff_phone", columnList = "phone", unique = true),
        @Index(name = "idx_staff_email", columnList = "email", unique = true),
        @Index(name = "idx_staff_role", columnList = "role_id"),
        @Index(name = "idx_staff_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "staff_seq")
    @SequenceGenerator(name = "staff_seq", sequenceName = SchemaConstants.CORE_SCHEMA
            + ".staff_user_seq", allocationSize = 1)
    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "phone", nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StaffStatus status = StaffStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_staff_role"))
    private Role role;

    @Column(name = "last_login_at", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime lastLoginAt;

    @Transient
    private String password;

    @PrePersist
    @PreUpdate
    private void normalizeData() {
        if (phone != null) {
            phone = phone.replaceAll("[^0-9]", "");
        }
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }
}
