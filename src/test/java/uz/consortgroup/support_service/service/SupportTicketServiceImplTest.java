package uz.consortgroup.support_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceImplTest {

    @Mock
    private SupportTicketRepository repository;

    @Mock
    private SupportTicketValidator validator;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private SupportTicketServiceImpl service;

    @Nested
    class CreateTicket {

        @Test
        @DisplayName("createTicket: PRESET + note -> saved with PRESET and trimmed comment")
        void preset_withNote_ok() {
            UUID userId = UUID.randomUUID();
            UserRole role = UserRole.MENTOR;
            UUID presetId = UUID.randomUUID();

            when(authContext.getCurrentUserId()).thenReturn(userId);
            when(authContext.getCurrentUserRole()).thenReturn(role);

            var dto = CreateTicketRequestDto.builder()
                    .selectedIssueId(presetId)
                    .comment("  extra info  ")
                    .build();

            var preset = SupportIssuePreset.builder()
                    .id(presetId)
                    .role(role)
                    .text("Some preset")
                    .sortOrder(1)
                    .active(true)
                    .build();

            when(validator.validatePresetOrThrow(presetId, role)).thenReturn(preset);
            when(validator.normalizeOptionalComment("  extra info  ")).thenReturn("extra info");

            when(repository.save(any(SupportTicket.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TicketCreatedResponse resp = service.createTicket(dto);

            assertThat(resp.getTicketStatus()).isEqualTo(TicketStatus.SUCCESS);
            assertThat(resp.getMessage()).isNotBlank();

            ArgumentCaptor<SupportTicket> captor = ArgumentCaptor.forClass(SupportTicket.class);
            verify(repository).save(captor.capture());
            SupportTicket saved = captor.getValue();

            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getRole()).isEqualTo(role);
            assertThat(saved.getStatus()).isEqualTo(SupportTicketStatus.NEW);
            assertThat(saved.getIssueType()).isEqualTo(SupportIssueType.PRESET);
            assertThat(saved.getSelectedIssue()).isEqualTo(preset);
            assertThat(saved.getComment()).isEqualTo("extra info");

            verify(validator).validateRoleAllowed(role);
            verify(validator).validatePresetOrThrow(presetId, role);
            verify(validator).normalizeOptionalComment("  extra info  ");
            verifyNoMoreInteractions(validator);
        }

        @Test
        @DisplayName("createTicket: PRESET without note -> saved with PRESET and null comment")
        void preset_withoutNote_ok() {
            UUID userId = UUID.randomUUID();
            UserRole role = UserRole.HR;
            UUID presetId = UUID.randomUUID();

            when(authContext.getCurrentUserId()).thenReturn(userId);
            when(authContext.getCurrentUserRole()).thenReturn(role);

            var dto = CreateTicketRequestDto.builder()
                    .selectedIssueId(presetId)
                    .comment(null)
                    .build();

            var preset = SupportIssuePreset.builder()
                    .id(presetId)
                    .role(role)
                    .text("Preset HR")
                    .sortOrder(1)
                    .active(true)
                    .build();

            when(validator.validatePresetOrThrow(presetId, role)).thenReturn(preset);
            when(validator.normalizeOptionalComment(null)).thenReturn(null);

            when(repository.save(any(SupportTicket.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TicketCreatedResponse resp = service.createTicket(dto);

            assertThat(resp.getTicketStatus()).isEqualTo(TicketStatus.SUCCESS);

            ArgumentCaptor<SupportTicket> captor = ArgumentCaptor.forClass(SupportTicket.class);
            verify(repository).save(captor.capture());
            SupportTicket saved = captor.getValue();

            assertThat(saved.getIssueType()).isEqualTo(SupportIssueType.PRESET);
            assertThat(saved.getSelectedIssue()).isEqualTo(preset);
            assertThat(saved.getComment()).isNull();

            verify(validator).validateRoleAllowed(role);
            verify(validator).validatePresetOrThrow(presetId, role);
            verify(validator).normalizeOptionalComment(null);
            verifyNoMoreInteractions(validator);
        }

        @Test
        @DisplayName("createTicket: CUSTOM comment -> saved with CUSTOM and normalized comment")
        void custom_ok() {
            UUID userId = UUID.randomUUID();
            UserRole role = UserRole.ADMIN;

            when(authContext.getCurrentUserId()).thenReturn(userId);
            when(authContext.getCurrentUserRole()).thenReturn(role);

            var dto = CreateTicketRequestDto.builder()
                    .comment("  help me  ")
                    .build();

            when(validator.normalizeCommentOrThrow("  help me  ")).thenReturn("help me");
            when(repository.save(any(SupportTicket.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            TicketCreatedResponse resp = service.createTicket(dto);

            assertThat(resp.getTicketStatus()).isEqualTo(TicketStatus.SUCCESS);

            ArgumentCaptor<SupportTicket> captor = ArgumentCaptor.forClass(SupportTicket.class);
            verify(repository).save(captor.capture());
            SupportTicket saved = captor.getValue();

            assertThat(saved.getIssueType()).isEqualTo(SupportIssueType.CUSTOM);
            assertThat(saved.getSelectedIssue()).isNull();
            assertThat(saved.getComment()).isEqualTo("help me");
            assertThat(saved.getRole()).isEqualTo(role);
            assertThat(saved.getUserId()).isEqualTo(userId);

            verify(validator).validateRoleAllowed(role);
            verify(validator).normalizeCommentOrThrow("  help me  ");
            verifyNoMoreInteractions(validator);
        }

        @Test
        @DisplayName("createTicket: SUPER_ADMIN -> throws (forbidden)")
        void superAdmin_forbidden() {
            UUID userId = UUID.randomUUID();
            UserRole role = UserRole.SUPER_ADMIN;

            when(authContext.getCurrentUserId()).thenReturn(userId);
            when(authContext.getCurrentUserRole()).thenReturn(role);

            doThrow(new IllegalArgumentException("Super Admin cannot create support tickets"))
                    .when(validator).validateRoleAllowed(role);

            var dto = CreateTicketRequestDto.builder()
                    .comment("whatever")
                    .build();

            assertThatThrownBy(() -> service.createTicket(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Super Admin");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("createTicket: PRESET invalid for role -> throws and not saved")
        void preset_invalid_throws() {
            UUID userId = UUID.randomUUID();
            UserRole role = UserRole.MENTOR;
            UUID presetId = UUID.randomUUID();

            when(authContext.getCurrentUserId()).thenReturn(userId);
            when(authContext.getCurrentUserRole()).thenReturn(role);

            var dto = CreateTicketRequestDto.builder()
                    .selectedIssueId(presetId)
                    .comment("x")
                    .build();

            when(validator.validatePresetOrThrow(presetId, role))
                    .thenThrow(new IllegalArgumentException("Preset is not available for this role"));

            assertThatThrownBy(() -> service.createTicket(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Preset");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("createTicket: CUSTOM invalid comment -> throws and not saved")
        void custom_invalidComment_throws() {
            UUID userId = UUID.randomUUID();
            UserRole role = UserRole.MENTOR;

            when(authContext.getCurrentUserId()).thenReturn(userId);
            when(authContext.getCurrentUserRole()).thenReturn(role);

            var dto = CreateTicketRequestDto.builder()
                    .comment("  ")
                    .build();

            when(validator.normalizeCommentOrThrow("  "))
                    .thenThrow(new IllegalArgumentException("Either selectedIssueId or comment is required"));

            assertThatThrownBy(() -> service.createTicket(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Either selectedIssueId or comment");

            verify(repository, never()).save(any());
        }
    }
}
