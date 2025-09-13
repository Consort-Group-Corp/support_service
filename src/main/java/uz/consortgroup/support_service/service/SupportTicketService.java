package uz.consortgroup.support_service.service;

import uz.consortgroup.core.api.v1.dto.support.request.CreateTicketRequestDto;
import uz.consortgroup.core.api.v1.dto.support.response.TicketCreatedResponse;

public interface SupportTicketService {
    TicketCreatedResponse createTicket(CreateTicketRequestDto createTicketRequestDto);
}
