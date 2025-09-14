package uz.consortgroup.support_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uz.consortgroup.core.api.v1.dto.support.request.CreatePresetRequestDto;
import uz.consortgroup.core.api.v1.dto.support.request.UpdatePresetRequestDto;
import uz.consortgroup.core.api.v1.dto.support.response.PresetResponse;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.handler.ErrorResponse;
import uz.consortgroup.support_service.service.super_admin.SupportPresetSuperAdminService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/support/presets/super-admin")
@RequiredArgsConstructor
@Tag(name = "Support Preset Super Admin", description = "Управление пресетами тикетов (только SUPER_ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class SupportPresetSuperAdminController {

    private final SupportPresetSuperAdminService supportPresetSuperAdminService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создать пресет",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreatePresetRequestDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "role": "MENTOR",
                                      "text": "Не получается сохранить курс",
                                      "sortOrder": 2,
                                      "active": true
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Создано",
                            content = @Content(schema = @Schema(implementation = PresetResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Conflict (дубликат текста для роли)",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public PresetResponse create(@Valid @RequestBody CreatePresetRequestDto body) {
        return supportPresetSuperAdminService.create(body);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Список пресетов",
            description = "Опциональная фильтрация по роли",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PresetResponse.class)))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public List<PresetResponse> list(@RequestParam(required = false) UserRole role) {
        return supportPresetSuperAdminService.list(role);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Обновить пресет",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID пресета", required = true,
                            schema = @Schema(format = "uuid"))
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdatePresetRequestDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "text": "Не удаётся сохранить курс",
                                      "sortOrder": 3,
                                      "active": true
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = PresetResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Conflict (дубликат текста для роли)",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public PresetResponse update(@PathVariable UUID id,
                                 @Valid @RequestBody UpdatePresetRequestDto body) {
        return supportPresetSuperAdminService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удалить пресет",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID пресета", required = true,
                            schema = @Schema(format = "uuid"))
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public void delete(@PathVariable UUID id) {
        supportPresetSuperAdminService.delete(id);
    }
}
