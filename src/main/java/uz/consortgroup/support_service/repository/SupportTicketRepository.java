package uz.consortgroup.support_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.consortgroup.core.api.v1.dto.support.enumeration.SupportTicketStatus;
import uz.consortgroup.support_service.entity.SupportTicket;

import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {
    Page<SupportTicket> findAllByStatus(SupportTicketStatus status, Pageable pageable);
}
