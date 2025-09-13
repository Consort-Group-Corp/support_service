package uz.consortgroup.support_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.core.api.v1.dto.support.response.IssuePresetResponse;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.entity.SupportIssuePreset;
import uz.consortgroup.support_service.repository.SupportIssuePresetRepository;
import uz.consortgroup.support_service.security.AuthContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class SupportIssuePresetServiceImpl implements SupportIssuePresetService {

    private final SupportIssuePresetRepository supportIssuePresetRepository;
    private final AuthContext authContext;

    @Override
    public Optional<SupportIssuePreset> findSelectedIssueId(UUID selectedIssueId) {
        log.info("Find selected issue id: {}", selectedIssueId);
        return supportIssuePresetRepository.findById(selectedIssueId);
    }

    @Override
    public List<IssuePresetResponse> getActivePresetsForCurrentUserRole() {
        UserRole role = authContext.getCurrentUserRole();

        List<SupportIssuePreset> list =
                supportIssuePresetRepository.findAllByRoleAndActiveTrueOrderBySortOrderAsc(role);

        log.info("Loaded {} presets for role={}", list.size(), role);
        return list.stream()
                .map(p -> new IssuePresetResponse(p.getId(), p.getText()))
                .toList();
    }
}
