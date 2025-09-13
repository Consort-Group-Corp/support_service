package uz.consortgroup.support_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "support_issue_presets", schema = "support_schema")
public class SupportIssuePreset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "role" , length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "text", length = 255, nullable = false)
    private String text;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
