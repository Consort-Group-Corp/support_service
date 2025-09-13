package uz.consortgroup.support_service.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.entity.SupportIssuePreset;
import uz.consortgroup.support_service.service.SupportIssuePresetService;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupportTicketValidator {
    private final SupportIssuePresetService supportIssuePresetService;

    public void validateRoleAllowed(UserRole role) {
        if (role == UserRole.SUPER_ADMIN) {
            log.warn("SUPER_ADMIN attempted to create a support ticket");
            throw new IllegalArgumentException("Super Admin cannot create support tickets");
        }
    }

    public SupportIssuePreset validatePresetOrThrow(UUID presetId, UserRole role) {
        SupportIssuePreset preset = supportIssuePresetService.findSelectedIssueId(presetId)
                .orElseThrow(() -> {
                    log.warn("Preset not found: presetId={}", presetId);
                    return new IllegalArgumentException("Preset not found");
                });

        if (!preset.isActive() || preset.getRole() != role) {
            log.warn("Preset not available for role: presetId={}, presetRole={}, userRole={}, active={}",
                    presetId, preset.getRole(), role, preset.isActive());
            throw new IllegalArgumentException("Preset is not available for this role");
        }
        return preset;
    }

    public String normalizeCommentOrThrow(String comment) {
        if (comment == null) {
            log.warn("Comment is null for CUSTOM ticket");
            throw new IllegalArgumentException("Either selectedIssueId or comment is required");
        }
        String normalized = comment.trim();
        if (normalized.isBlank()) {
            log.warn("Comment is blank for CUSTOM ticket");
            throw new IllegalArgumentException("Either selectedIssueId or comment is required");
        }
        if (normalized.length() > 500) {
            log.warn("Comment length > 500: length={}", normalized.length());
            throw new IllegalArgumentException("Comment length must be <= 500");
        }
        return normalized;
    }

    public String normalizeOptionalComment(String comment) {
        if (comment == null) return null;
        String normalized = comment.trim();
        if (normalized.isEmpty()) return null;
        if (normalized.length() > 500) {
            log.warn("Comment length > 500 (preset optional): length={}", normalized.length());
            throw new IllegalArgumentException("Comment length must be <= 500");
        }
        return normalized;
    }
}

