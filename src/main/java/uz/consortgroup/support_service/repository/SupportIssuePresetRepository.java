package uz.consortgroup.support_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.entity.SupportIssuePreset;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportIssuePresetRepository extends JpaRepository<SupportIssuePreset, UUID> {
    boolean existsByRoleAndTextIgnoreCase(UserRole role, String text);
    List<SupportIssuePreset> findAllByRoleAndActiveTrueOrderBySortOrderAsc(UserRole role);
    List<SupportIssuePreset> findAllByRoleOrderBySortOrderAsc(UserRole role);
    List<SupportIssuePreset> findAllByOrderByRoleAscSortOrderAsc();
}
