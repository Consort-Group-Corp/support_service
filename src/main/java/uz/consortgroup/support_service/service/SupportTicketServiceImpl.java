package uz.consortgroup.support_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.support.enumeration.SupportIssueType;
import uz.consortgroup.core.api.v1.dto.support.enumeration.SupportTicketStatus;
import uz.consortgroup.core.api.v1.dto.support.enumeration.TicketStatus;
import uz.consortgroup.core.api.v1.dto.support.request.CreateTicketRequestDto;
import uz.consortgroup.core.api.v1.dto.support.response.TicketCreatedResponse;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.entity.SupportIssuePreset;
import uz.consortgroup.support_service.entity.SupportTicket;
import uz.consortgroup.support_service.repository.SupportTicketRepository;
import uz.consortgroup.support_service.security.AuthContext;
import uz.consortgroup.support_service.validator.SupportTicketValidator;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketValidator supportTicketValidator;
    private final AuthContext authContext;

    @Override
    @Transactional
    public TicketCreatedResponse createTicket(CreateTicketRequestDto dto) {
        UUID userId = authContext.getCurrentUserId();
        UserRole role = authContext.getCurrentUserRole();

        log.info("Create support ticket request: userId={}, role={}, selectedIssueId={}",
                userId, role.name(), dto.getSelectedIssueId());

        supportTicketValidator.validateRoleAllowed(role);

        SupportTicket ticket = new SupportTicket();
        ticket.setUserId(userId);
        ticket.setRole(role);
        ticket.setStatus(SupportTicketStatus.NEW);

        UUID presetId = dto.getSelectedIssueId();
        if (presetId != null) {
            SupportIssuePreset preset = supportTicketValidator.validatePresetOrThrow(presetId, role);
            ticket.setIssueType(SupportIssueType.PRESET);
            ticket.setSelectedIssue(preset);

            String note = supportTicketValidator.normalizeOptionalComment(dto.getComment());
            ticket.setComment(note);

            log.debug("Ticket mapped as PRESET: presetId={}, commentPresent={}", preset.getId(), note != null);
        } else {
            String normalized = supportTicketValidator.normalizeCommentOrThrow(dto.getComment());
            ticket.setIssueType(SupportIssueType.CUSTOM);
            ticket.setComment(normalized);
            log.debug("Ticket mapped as CUSTOM: commentLength={}", normalized.length());
        }

        supportTicketRepository.save(ticket);

        log.info("Support ticket created: ticketId={}, userId={}, issueType={}, status={}",
                ticket.getId(), userId, ticket.getIssueType().name(), ticket.getStatus().name());

        return TicketCreatedResponse.builder()
                .ticketStatus(TicketStatus.SUCCESS)
                .message("Заявка отправлена")
                .build();
    }
}
