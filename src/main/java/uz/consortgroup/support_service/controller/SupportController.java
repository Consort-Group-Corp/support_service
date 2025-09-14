package uz.consortgroup.support_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.support.enumeration.SupportTicketStatus;
import uz.consortgroup.core.api.v1.dto.support.request.CreateTicketRequestDto;
import uz.consortgroup.core.api.v1.dto.support.request.UpdateTicketStatusRequestDto;
import uz.consortgroup.core.api.v1.dto.support.response.IssuePresetResponse;
import uz.consortgroup.core.api.v1.dto.support.response.SupportTicketResponse;
import uz.consortgroup.core.api.v1.dto.support.response.TicketCreatedResponse;
import uz.consortgroup.support_service.handler.ErrorResponse;
import uz.consortgroup.support_service.service.SupportIssuePresetService;
import uz.consortgroup.support_service.service.SupportTicketService;
import uz.consortgroup.support_service.service.super_admin.SupportTicketSuperAdminService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
@Tag(name = "Support", description = "API для работы с поддержкой")
@SecurityRequirement(name = "bearerAuth")
public class SupportController {

    private final SupportTicketService supportTicketService;
    private final SupportIssuePresetService supportIssuePresetService;
    private final SupportTicketSuperAdminService supportTicketSuperAdminService;

    @GetMapping("/presets")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Получить пресеты для текущей роли",
            description = "Возвращает активные предустановленные варианты проблем в зависимости от роли текущего пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = IssuePresetResponse.class)))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public List<IssuePresetResponse> getPresetsForCurrentRole() {
        return supportIssuePresetService.getActivePresetsForCurrentUserRole();
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создать тикет в поддержку",
            description = "Создаёт заявку в поддержку. Можно указать ID пресета или собственный комментарий (до 500 символов). " +
                    "При наличии пресета комментарий опционален (пояснение).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateTicketRequestDto.class),
                            examples = {
                                    @ExampleObject(name = "Через пресет",
                                            value = """
                                                    {
                                                      "selectedIssueId": "f25f659c-53bc-4260-a977-07470d97e8e2",
                                                      "comment": "Доп. детали: ошибка возникает при нажатии «Сохранить»"
                                                    }
                                                    """),
                                    @ExampleObject(name = "Свободный комментарий (без пресета)",
                                            value = """
                                                    {
                                                      "comment": "Не получается сохранить курс. Ошибка при нажатии на кнопку «Сохранить»."
                                                    }
                                                    """)
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Создано",
                            content = @Content(schema = @Schema(implementation = TicketCreatedResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "ticketStatus": "SUCCESS",
                                              "message": "Заявка отправлена"
                                            }
                                            """))),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public TicketCreatedResponse createTicket(@Valid @RequestBody CreateTicketRequestDto body) {
        return supportTicketService.createTicket(body);
    }

    @GetMapping("/tickets")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Список тикетов (для Супер-Админа)",
            description = "Возвращает страницу тикетов. Доступно фильтрование по статусу. Требует роль SUPER_ADMIN.",
            parameters = {
                    @Parameter(name = "status", description = "Фильтр по статусу",
                            schema = @Schema(implementation = SupportTicketStatus.class))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = SupportTicketResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public Page<SupportTicketResponse> listTickets(
            @RequestParam(required = false) SupportTicketStatus status,
            @ParameterObject Pageable pageable
    ) {
        return supportTicketSuperAdminService.listTickets(status, pageable);
    }

    @PutMapping("/tickets/{ticketId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Обновить статус тикета (для Супер-Админа)",
            description = "Меняет статус указанного тикета и возвращает обновлённую модель",
            parameters = {
                    @Parameter(name = "ticketId", description = "ID тикета", required = true,
                            schema = @Schema(format = "uuid"))
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateTicketStatusRequestDto.class),
                            examples = @ExampleObject(value = """
                                { "status": "IN_PROGRESS" }
                                """)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = SupportTicketResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
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
    public SupportTicketResponse updateStatus(@PathVariable UUID ticketId,
                                              @RequestBody UpdateTicketStatusRequestDto body) {
        return supportTicketSuperAdminService.updateStatus(ticketId, body.getStatus());
    }
}
