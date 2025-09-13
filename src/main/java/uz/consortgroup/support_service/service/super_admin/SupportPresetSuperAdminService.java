package uz.consortgroup.support_service.service.super_admin;

import uz.consortgroup.core.api.v1.dto.support.request.CreatePresetRequestDto;
import uz.consortgroup.core.api.v1.dto.support.request.UpdatePresetRequestDto;
import uz.consortgroup.core.api.v1.dto.support.response.PresetResponse;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;

import java.util.List;
import java.util.UUID;

public interface SupportPresetSuperAdminService {
    PresetResponse create(CreatePresetRequestDto req);
    PresetResponse update(UUID id, UpdatePresetRequestDto req);
    void delete(UUID id);
    List<PresetResponse> list(UserRole role);
}
