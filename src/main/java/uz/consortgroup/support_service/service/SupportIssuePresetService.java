package uz.consortgroup.support_service.service;

import uz.consortgroup.core.api.v1.dto.support.response.IssuePresetResponse;
import uz.consortgroup.support_service.entity.SupportIssuePreset;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupportIssuePresetService {
    Optional<SupportIssuePreset> findSelectedIssueId(UUID selectedIssueId);
    List<IssuePresetResponse> getActivePresetsForCurrentUserRole();
}
