package uz.consortgroup.support_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.support.response.IssuePresetResponse;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.entity.SupportIssuePreset;
import uz.consortgroup.support_service.repository.SupportIssuePresetRepository;
import uz.consortgroup.support_service.security.AuthContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportIssuePresetServiceImplTest {

    @Mock
    private SupportIssuePresetRepository repository;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private SupportIssuePresetServiceImpl service;

    @Nested
    class FindSelectedIssueId {

        @Test
        @DisplayName("findSelectedIssueId -> returns Optional with entity when present")
        void returns_present() {
            UUID id = UUID.randomUUID();
            var preset = SupportIssuePreset.builder()
                    .id(id)
                    .role(UserRole.MENTOR)
                    .text("Не получается сохранить курс")
                    .sortOrder(1)
                    .active(true)
                    .build();

            when(repository.findById(eq(id))).thenReturn(Optional.of(preset));

            Optional<SupportIssuePreset> out = service.findSelectedIssueId(id);

            assertThat(out).isPresent();
            assertThat(out.get().getId()).isEqualTo(id);

            verify(repository).findById(eq(id));
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(authContext);
        }

        @Test
        @DisplayName("findSelectedIssueId -> returns Optional.empty when not found")
        void returns_empty() {
            UUID id = UUID.randomUUID();
            when(repository.findById(eq(id))).thenReturn(Optional.empty());

            Optional<SupportIssuePreset> out = service.findSelectedIssueId(id);

            assertThat(out).isEmpty();

            verify(repository).findById(eq(id));
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(authContext);
        }
    }

    @Nested
    class GetActivePresetsForCurrentUserRole {

        @Test
        @DisplayName("getActivePresetsForCurrentUserRole -> maps list for role from AuthContext")
        void returns_mapped_list() {
            UserRole role = UserRole.MENTOR;
            when(authContext.getCurrentUserRole()).thenReturn(role);

            var p1 = SupportIssuePreset.builder()
                    .id(UUID.randomUUID())
                    .role(role)
                    .text("Не можем добавить материалы")
                    .sortOrder(1)
                    .active(true)
                    .build();
            var p2 = SupportIssuePreset.builder()
                    .id(UUID.randomUUID())
                    .role(role)
                    .text("Не получается сохранить курс")
                    .sortOrder(2)
                    .active(true)
                    .build();

            when(repository.findAllByRoleAndActiveTrueOrderBySortOrderAsc(eq(role)))
                    .thenReturn(List.of(p1, p2));

            List<IssuePresetResponse> out = service.getActivePresetsForCurrentUserRole();

            assertThat(out).hasSize(2);
            assertThat(out.get(0).getId()).isEqualTo(p1.getId());
            assertThat(out.get(0).getText()).isEqualTo(p1.getText());
            assertThat(out.get(1).getId()).isEqualTo(p2.getId());
            assertThat(out.get(1).getText()).isEqualTo(p2.getText());

            verify(authContext).getCurrentUserRole();
            verify(repository).findAllByRoleAndActiveTrueOrderBySortOrderAsc(eq(role));
            verifyNoMoreInteractions(repository, authContext);
        }

        @Test
        @DisplayName("getActivePresetsForCurrentUserRole -> returns empty list when repo returns empty")
        void returns_empty_list() {
            UserRole role = UserRole.HR;
            when(authContext.getCurrentUserRole()).thenReturn(role);
            when(repository.findAllByRoleAndActiveTrueOrderBySortOrderAsc(eq(role)))
                    .thenReturn(List.of());

            List<IssuePresetResponse> out = service.getActivePresetsForCurrentUserRole();

            assertThat(out).isEmpty();

            verify(authContext).getCurrentUserRole();
            verify(repository).findAllByRoleAndActiveTrueOrderBySortOrderAsc(eq(role));
            verifyNoMoreInteractions(repository, authContext);
        }
    }
}
