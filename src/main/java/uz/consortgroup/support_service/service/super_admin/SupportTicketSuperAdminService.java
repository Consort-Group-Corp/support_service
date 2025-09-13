package uz.consortgroup.support_service.service.super_admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.consortgroup.core.api.v1.dto.support.enumeration.SupportTicketStatus;
import uz.consortgroup.core.api.v1.dto.support.response.SupportTicketResponse;

import java.util.UUID;

public interface SupportTicketSuperAdminService {
    Page<SupportTicketResponse> listTickets(SupportTicketStatus status, Pageable pageable);
    SupportTicketResponse updateStatus(UUID ticketId, SupportTicketStatus status);
}