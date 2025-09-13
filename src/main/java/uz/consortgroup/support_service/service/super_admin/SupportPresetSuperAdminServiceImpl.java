package uz.consortgroup.support_service.service.super_admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.support.request.CreatePresetRequestDto;
import uz.consortgroup.core.api.v1.dto.support.request.UpdatePresetRequestDto;
import uz.consortgroup.core.api.v1.dto.support.response.PresetResponse;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.entity.SupportIssuePreset;
import uz.consortgroup.support_service.exception.PresetNotFoundExecption;
import uz.consortgroup.support_service.mapper.SupportMapper;
import uz.consortgroup.support_service.repository.SupportIssuePresetRepository;
import uz.consortgroup.support_service.validator.SupportPresetValidator;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportPresetSuperAdminServiceImpl implements SupportPresetSuperAdminService {

    private final SupportIssuePresetRepository repository;
    private final SupportMapper supportMapper;
    private final SupportPresetValidator validator;

    @Override
    @Transactional
    public PresetResponse create(CreatePresetRequestDto req) {
        String text = validator.normalizeTextOrThrow(req.getText());
        validator.ensureUniqueOnCreate(req.getRole(), text);

        SupportIssuePreset preset = SupportIssuePreset.builder()
                .role(req.getRole())
                .text(text)
                .sortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder())
                .active(req.getActive() == null || req.getActive())
                .build();

        preset = repository.save(preset);
        log.info("Preset created: id={}, role={}, text='{}'", preset.getId(), preset.getRole(), preset.getText());
        return supportMapper.toPresetDto(preset);
    }

    @Override
    @Transactional
    public PresetResponse update(UUID id, UpdatePresetRequestDto req) {
        SupportIssuePreset preset = repository.findById(id)
                .orElseThrow(() -> new PresetNotFoundExecption("Preset not found"));

        if (req.getText() != null) {
            String newText = validator.normalizeTextOrThrow(req.getText());
            validator.ensureUniqueOnUpdate(preset, newText);
            preset.setText(newText);
        }
        if (req.getSortOrder() != null) {
            preset.setSortOrder(req.getSortOrder());
        }
        if (req.getActive() != null) {
            preset.setActive(req.getActive());
        }

        preset = repository.save(preset);
        log.info("Preset updated: id={}, active={}, sortOrder={}", preset.getId(), preset.isActive(), preset.getSortOrder());
        return supportMapper.toPresetDto(preset);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new PresetNotFoundExecption("Preset not found");
        }
        repository.deleteById(id);
        log.info("Preset deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresetResponse> list(UserRole role) {
        List<SupportIssuePreset> list = (role == null)
                ? repository.findAllByOrderByRoleAscSortOrderAsc()
                : repository.findAllByRoleOrderBySortOrderAsc(role);

        return list.stream().map(supportMapper::toPresetDto).toList();
    }
}
