package uz.consortgroup.support_service.service.super_admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.support.enumeration.SupportTicketStatus;
import uz.consortgroup.core.api.v1.dto.support.response.SupportTicketResponse;
import uz.consortgroup.support_service.entity.SupportTicket;
import uz.consortgroup.support_service.exception.TicketNotFoundException;
import uz.consortgroup.support_service.mapper.SupportMapper;
import uz.consortgroup.support_service.repository.SupportTicketRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupportTicketSuperAdminServiceImpl implements SupportTicketSuperAdminService {

    private final SupportTicketRepository supportTicketRepository;
    private final SupportMapper supportMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> listTickets(SupportTicketStatus status, Pageable pageable) {
        log.info("List tickets: status={}, pageable={}", status, pageable);
        Page<SupportTicket> page = (status == null) ? supportTicketRepository.findAll(pageable)
                : supportTicketRepository.findAllByStatus(status, pageable);

        return page.map(supportMapper::toDto);
    }

    @Override
    @Transactional
    public SupportTicketResponse updateStatus(UUID ticketId, SupportTicketStatus status) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(String.format("Ticket with id %s not found", ticketId)));

        ticket.setStatus(status);
        log.info("Ticket status updated: ticketId={}, newStatus={}", ticketId, status.name());
        SupportTicket saved = supportTicketRepository.save(ticket);
        return supportMapper.toDto(saved);

    }
}