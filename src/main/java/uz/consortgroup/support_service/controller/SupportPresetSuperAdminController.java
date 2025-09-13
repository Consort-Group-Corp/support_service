package uz.consortgroup.support_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uz.consortgroup.core.api.v1.dto.support.request.CreatePresetRequestDto;
import uz.consortgroup.core.api.v1.dto.support.request.UpdatePresetRequestDto;
import uz.consortgroup.core.api.v1.dto.support.response.PresetResponse;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.service.super_admin.SupportPresetSuperAdminService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/support/presets/super-admin")
@RequiredArgsConstructor
public class SupportPresetSuperAdminController {

    private final SupportPresetSuperAdminService supportPresetSuperAdminService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PresetResponse create(@Valid @RequestBody CreatePresetRequestDto body) {
        return supportPresetSuperAdminService.create(body);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PresetResponse> list(@RequestParam(required = false) UserRole role) {
        return supportPresetSuperAdminService.list(role);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PresetResponse update(@PathVariable UUID id,
                                 @Valid @RequestBody UpdatePresetRequestDto body) {
        return supportPresetSuperAdminService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        supportPresetSuperAdminService.delete(id);
    }
}
