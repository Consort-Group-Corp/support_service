package uz.consortgroup.support_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import uz.consortgroup.core.api.v1.dto.support.enumeration.SupportIssueType;
import uz.consortgroup.core.api.v1.dto.support.enumeration.SupportTicketStatus;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "support_tickets", schema = "support_schema")
public class SupportTicket {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", length = 50, nullable = false)
    private SupportIssueType issueType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_issue_id")
    private SupportIssuePreset selectedIssue;

    @Column(length = 500)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private SupportTicketStatus status;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
