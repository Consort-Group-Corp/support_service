package uz.consortgroup.support_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.core.api.v1.dto.support.response.PresetResponse;
import uz.consortgroup.core.api.v1.dto.support.response.SupportTicketResponse;
import uz.consortgroup.support_service.entity.SupportIssuePreset;
import uz.consortgroup.support_service.entity.SupportTicket;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupportMapper {
    SupportTicketResponse toDto(SupportTicket supportTicket);
    PresetResponse toPresetDto(SupportIssuePreset supportTicket);
}
