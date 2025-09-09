package com.htttql.crmmodule.core.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "role", schema = SchemaConstants.CORE_SCHEMA, indexes = {
        @Index(name = "idx_role_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
    @SequenceGenerator(name = "role_seq", sequenceName = SchemaConstants.CORE_SCHEMA + ".role_seq", allocationSize = 1)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<StaffUser> staffUsers = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void validateCode() {
        if (code != null) {
            code = code.toUpperCase();
        }
    }
}
