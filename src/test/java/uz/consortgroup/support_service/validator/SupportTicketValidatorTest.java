package uz.consortgroup.support_service.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.entity.SupportIssuePreset;
import uz.consortgroup.support_service.service.SupportIssuePresetService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTicketValidatorTest {

    @Mock
    private SupportIssuePresetService presetService;

    @InjectMocks
    private SupportTicketValidator validator;


    @Test
    @DisplayName("validateRoleAllowed: SUPER_ADMIN -> throws")
    void validateRole_superAdmin_throws() {
        assertThatThrownBy(() -> validator.validateRoleAllowed(UserRole.SUPER_ADMIN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Super Admin cannot create support tickets");
    }

    @Test
    @DisplayName("validateRoleAllowed: non SUPER_ADMIN -> ok")
    void validateRole_nonSuperAdmin_ok() {
        validator.validateRoleAllowed(UserRole.ADMIN);
        validator.validateRoleAllowed(UserRole.MENTOR);
        validator.validateRoleAllowed(UserRole.HR);
        validator.validateRoleAllowed(UserRole.STUDENT);
    }

    @Test
    @DisplayName("validatePresetOrThrow: not found -> throws 'Preset not found'")
    void validatePreset_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(presetService.findSelectedIssueId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validatePresetOrThrow(id, UserRole.MENTOR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Preset not found");

        verify(presetService).findSelectedIssueId(eq(id));
        verifyNoMoreInteractions(presetService);
    }

    @Test
    @DisplayName("validatePresetOrThrow: inactive -> throws 'Preset is not available for this role'")
    void validatePreset_inactive_throws() {
        UUID id = UUID.randomUUID();
        var preset = SupportIssuePreset.builder()
                .id(id)
                .role(UserRole.MENTOR)
                .text("T")
                .sortOrder(1)
                .active(false)
                .build();
        when(presetService.findSelectedIssueId(id)).thenReturn(Optional.of(preset));

        assertThatThrownBy(() -> validator.validatePresetOrThrow(id, UserRole.MENTOR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Preset is not available for this role");

        verify(presetService).findSelectedIssueId(eq(id));
        verifyNoMoreInteractions(presetService);
    }

    @Test
    @DisplayName("validatePresetOrThrow: role mismatch -> throws 'Preset is not available for this role'")
    void validatePreset_roleMismatch_throws() {
        UUID id = UUID.randomUUID();
        var preset = SupportIssuePreset.builder()
                .id(id)
                .role(UserRole.ADMIN)
                .text("T")
                .sortOrder(1)
                .active(true)
                .build();
        when(presetService.findSelectedIssueId(id)).thenReturn(Optional.of(preset));

        assertThatThrownBy(() -> validator.validatePresetOrThrow(id, UserRole.HR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Preset is not available for this role");

        verify(presetService).findSelectedIssueId(eq(id));
        verifyNoMoreInteractions(presetService);
    }

    @Test
    @DisplayName("validatePresetOrThrow: ok (active + same role) -> returns preset")
    void validatePreset_ok() {
        UUID id = UUID.randomUUID();
        var preset = SupportIssuePreset.builder()
                .id(id)
                .role(UserRole.MENTOR)
                .text("T")
                .sortOrder(1)
                .active(true)
                .build();
        when(presetService.findSelectedIssueId(id)).thenReturn(Optional.of(preset));

        var out = validator.validatePresetOrThrow(id, UserRole.MENTOR);

        assertThat(out).isSameAs(preset);
        verify(presetService).findSelectedIssueId(eq(id));
        verifyNoMoreInteractions(presetService);
    }


    @Test
    @DisplayName("normalizeCommentOrThrow: ok -> trimmed")
    void normalizeComment_ok() {
        assertThat(validator.normalizeCommentOrThrow("  Привет  ")).isEqualTo("Привет");
    }

    @Test
    @DisplayName("normalizeCommentOrThrow: null -> throws required")
    void normalizeComment_null_throws() {
        assertThatThrownBy(() -> validator.normalizeCommentOrThrow(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Either selectedIssueId or comment is required");
    }

    @Test
    @DisplayName("normalizeCommentOrThrow: blank -> throws required")
    void normalizeComment_blank_throws() {
        assertThatThrownBy(() -> validator.normalizeCommentOrThrow("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Either selectedIssueId or comment is required");
    }

    @Test
    @DisplayName("normalizeCommentOrThrow: >500 chars -> throws length")
    void normalizeComment_tooLong_throws() {
        String s = "a".repeat(501);
        assertThatThrownBy(() -> validator.normalizeCommentOrThrow(s))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Comment length must be <= 500");
    }


    @Test
    @DisplayName("normalizeOptionalComment: null -> null")
    void normalizeOptional_null_ok() {
        assertThat(validator.normalizeOptionalComment(null)).isNull();
    }

    @Test
    @DisplayName("normalizeOptionalComment: blank -> null")
    void normalizeOptional_blank_ok() {
        assertThat(validator.normalizeOptionalComment("   ")).isNull();
    }

    @Test
    @DisplayName("normalizeOptionalComment: ok -> trimmed")
    void normalizeOptional_ok() {
        assertThat(validator.normalizeOptionalComment("  note  ")).isEqualTo("note");
    }

    @Test
    @DisplayName("normalizeOptionalComment: >500 chars -> throws length")
    void normalizeOptional_tooLong_throws() {
        String s = "a".repeat(501);
        assertThatThrownBy(() -> validator.normalizeOptionalComment(s))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Comment length must be <= 500");
    }
}
