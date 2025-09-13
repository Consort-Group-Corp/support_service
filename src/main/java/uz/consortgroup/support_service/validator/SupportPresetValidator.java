package uz.consortgroup.support_service.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.entity.SupportIssuePreset;
import uz.consortgroup.support_service.repository.SupportIssuePresetRepository;

@Component
@RequiredArgsConstructor
public class SupportPresetValidator {

    private final SupportIssuePresetRepository repository;

    public String normalizeTextOrThrow(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text is required");
        }
        String normalized = text.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Text must not be blank");
        }
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("Text length must be <= 255");
        }
        return normalized;
    }

    public void ensureUniqueOnCreate(UserRole role, String text) {
        if (repository.existsByRoleAndTextIgnoreCase(role, text)) {
            throw new IllegalArgumentException("Preset with same text already exists for this role");
        }
    }

    public void ensureUniqueOnUpdate(SupportIssuePreset existing, String newText) {
        if (!existing.getText().equalsIgnoreCase(newText)
                && repository.existsByRoleAndTextIgnoreCase(existing.getRole(), newText)) {
            throw new IllegalArgumentException("Preset with same text already exists for this role");
        }
    }
}
