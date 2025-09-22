package uz.consortgroup.support_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.consortgroup.core.api.v1.dto.support.enumeration.SupportTicketStatus;
import uz.consortgroup.core.api.v1.dto.support.enumeration.TicketStatus;
import uz.consortgroup.core.api.v1.dto.support.request.CreateTicketRequestDto;
import uz.consortgroup.core.api.v1.dto.support.request.UpdateTicketStatusRequestDto;
import uz.consortgroup.core.api.v1.dto.support.response.IssuePresetResponse;
import uz.consortgroup.core.api.v1.dto.support.response.SupportTicketResponse;
import uz.consortgroup.core.api.v1.dto.support.response.TicketCreatedResponse;
import uz.consortgroup.support_service.exception.TicketNotFoundException;
import uz.consortgroup.support_service.handler.GlobalExceptionHandler;
import uz.consortgroup.support_service.service.SupportIssuePresetService;
import uz.consortgroup.support_service.service.SupportTicketService;
import uz.consortgroup.support_service.service.super_admin.SupportTicketSuperAdminService;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SupportController.class)
class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupportTicketService supportTicketService;

    @MockitoBean
    private SupportIssuePresetService supportIssuePresetService;

    @MockitoBean
    private SupportTicketSuperAdminService supportTicketSuperAdminService;

    @Test
    @DisplayName("GET /presets -> 200 OK и список пресетов")
    void getPresets_ok() throws Exception {
        var p1 = new IssuePresetResponse(UUID.randomUUID(), "Не получается сохранить курс");
        var p2 = new IssuePresetResponse(UUID.randomUUID(), "Не можем добавить материалы");

        given(supportIssuePresetService.getActivePresetsForCurrentUserRole())
                .willReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/v1/support/presets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(p1.getId().toString()))
                .andExpect(jsonPath("$[0].text").value(p1.getText()));
    }

    @Test
    @DisplayName("POST /tickets (комментарий) -> 201 Created")
    void createTicket_comment_ok() throws Exception {
        var req = CreateTicketRequestDto.builder()
                .comment("Кнопка «Сохранить» не реагирует")
                .build();

        var resp = TicketCreatedResponse.builder()
                .ticketStatus(TicketStatus.SUCCESS)
                .message("Заявка отправлена")
                .build();

        given(supportTicketService.createTicket(any(CreateTicketRequestDto.class)))
                .willReturn(resp);

        mockMvc.perform(post("/api/v1/support/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Заявка отправлена"));

        verify(supportTicketService).createTicket(any(CreateTicketRequestDto.class));
    }

    @Test
    @DisplayName("POST /tickets (пресет) -> 201 Created")
    void createTicket_preset_ok() throws Exception {
        var req = CreateTicketRequestDto.builder()
                .selectedIssueId(UUID.randomUUID())
                .build();

        var resp = TicketCreatedResponse.builder()
                .ticketStatus(TicketStatus.SUCCESS)
                .message("Заявка отправлена")
                .build();

        given(supportTicketService.createTicket(any(CreateTicketRequestDto.class)))
                .willReturn(resp);

        mockMvc.perform(post("/api/v1/support/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketStatus").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /tickets -> 400 Bad Request (валидация: ни пресета, ни комментария)")
    void createTicket_validation_fail() throws Exception {
        var req = CreateTicketRequestDto.builder().build();

        mockMvc.perform(post("/api/v1/support/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.message",
                        containsString("Either selectedIssueId or comment must be provided")));
    }

    @Test
    @DisplayName("GET /tickets -> 200 OK (пустая страница)")
    void listTickets_ok() throws Exception {
        Page<SupportTicketResponse> page = new PageImpl<>(
                List.of(), PageRequest.of(0, 20), 0);

        given(supportTicketSuperAdminService.listTickets(isNull(), any()))
                .willReturn(page);

        mockMvc.perform(get("/api/v1/support/tickets?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        verify(supportTicketSuperAdminService).listTickets(isNull(), any());
    }

    @Test
    @DisplayName("GET /tickets -> 500 Internal Server Error (исключение из сервиса)")
    void listTickets_fail500() throws Exception {
        given(supportTicketSuperAdminService.listTickets(any(), any()))
                .willThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/v1/support/tickets"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }

    @Test
    @DisplayName("PUT /tickets/{id} -> 200 OK")
    void updateStatus_ok() throws Exception {
        UUID id = UUID.randomUUID();
        var body = new UpdateTicketStatusRequestDto(SupportTicketStatus.IN_PROGRESS);

        given(supportTicketSuperAdminService.updateStatus(eq(id), eq(SupportTicketStatus.IN_PROGRESS)))
                .willReturn(new SupportTicketResponse(null, null, null, null,
                        null, null, SupportTicketStatus.IN_PROGRESS, null, null));

        mockMvc.perform(put("/api/v1/support/tickets/{ticketId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(supportTicketSuperAdminService)
                .updateStatus(eq(id), eq(SupportTicketStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("PUT /tickets/{id} -> 404 Not Found (тикет не найден)")
    void updateStatus_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        var body = new UpdateTicketStatusRequestDto(SupportTicketStatus.CLOSED);

        given(supportTicketSuperAdminService.updateStatus(eq(id), eq(SupportTicketStatus.CLOSED)))
                .willThrow(new TicketNotFoundException("Ticket with id %s not found".formatted(id)));

        mockMvc.perform(put("/api/v1/support/tickets/{ticketId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ticket not found"));
    }

    @Test
    @DisplayName("PUT /tickets/{id} -> 400 Bad Request (невалидный UUID)")
    void updateStatus_badUuid() throws Exception {
        var body = new UpdateTicketStatusRequestDto(SupportTicketStatus.NEW);

        mockMvc.perform(put("/api/v1/support/tickets/not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
