package uz.consortgroup.support_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import uz.consortgroup.support_service.service.SupportIssuePresetService;
import uz.consortgroup.support_service.service.super_admin.SupportTicketSuperAdminService;
import uz.consortgroup.support_service.service.SupportTicketService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportTicketService supportTicketService;
    private final SupportIssuePresetService supportIssuePresetService;
    private final SupportTicketSuperAdminService supportTicketSuperAdminService;

    @GetMapping("/presets")
    @ResponseStatus(HttpStatus.OK)
    public List<IssuePresetResponse> getPresetsForCurrentRole() {
        return supportIssuePresetService.getActivePresetsForCurrentUserRole();
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketCreatedResponse createTicket(@Valid @RequestBody CreateTicketRequestDto body) {
        return supportTicketService.createTicket(body);
    }

    @GetMapping("/tickets")
    @ResponseStatus(HttpStatus.OK)
    public Page<SupportTicketResponse> listTickets(@RequestParam(required = false)
                                                       SupportTicketStatus status, Pageable pageable) {
        return supportTicketSuperAdminService.listTickets(status, pageable);
    }

    @PutMapping("/tickets/{ticketId}")
    @ResponseStatus(HttpStatus.OK)
    public SupportTicketResponse updateStatus(@PathVariable UUID ticketId, @RequestBody UpdateTicketStatusRequestDto body) {
        return supportTicketSuperAdminService.updateStatus(ticketId, body.getStatus());
    }
}
