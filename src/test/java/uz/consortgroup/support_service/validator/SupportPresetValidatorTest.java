package uz.consortgroup.support_service.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.entity.SupportIssuePreset;
import uz.consortgroup.support_service.repository.SupportIssuePresetRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportPresetValidatorTest {

    @Mock
    private SupportIssuePresetRepository repository;

    @InjectMocks
    private SupportPresetValidator validator;

    @Nested
    class NormalizeTextOrThrow {

        @Test
        @DisplayName("ok -> trims and returns")
        void ok() {
            String out = validator.normalizeTextOrThrow("  Привет  ");
            assertThat(out).isEqualTo("Привет");
        }

        @Test
        @DisplayName("null -> throws 'Text is required'")
        void nullText() {
            assertThatThrownBy(() -> validator.normalizeTextOrThrow(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Text is required");
        }

        @Test
        @DisplayName("blank -> throws 'Text must not be blank'")
        void blank() {
            assertThatThrownBy(() -> validator.normalizeTextOrThrow("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Text must not be blank");
        }

        @Test
        @DisplayName(">255 -> throws 'Text length must be <= 255'")
        void tooLong() {
            String longStr = "a".repeat(256);
            assertThatThrownBy(() -> validator.normalizeTextOrThrow(longStr))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Text length must be <= 255");
        }
    }

    @Nested
    class EnsureUniqueOnCreate {

        @Test
        @DisplayName("unique -> OK (no exception)")
        void unique_ok() {
            when(repository.existsByRoleAndTextIgnoreCase(UserRole.MENTOR, "abc")).thenReturn(false);
            validator.ensureUniqueOnCreate(UserRole.MENTOR, "abc");
            verify(repository).existsByRoleAndTextIgnoreCase(UserRole.MENTOR, "abc");
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("duplicate -> throws")
        void duplicate_throw() {
            when(repository.existsByRoleAndTextIgnoreCase(UserRole.MENTOR, "abc")).thenReturn(true);

            assertThatThrownBy(() -> validator.ensureUniqueOnCreate(UserRole.MENTOR, "abc"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Preset with same text already exists for this role");

            verify(repository).existsByRoleAndTextIgnoreCase(UserRole.MENTOR, "abc");
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    class EnsureUniqueOnUpdate {

        @Test
        @DisplayName("same text (case-insensitive) -> does NOT query repo, OK")
        void sameText_ok() {
            var existing = SupportIssuePreset.builder()
                    .role(UserRole.ADMIN)
                    .text("One Text")
                    .build();

            validator.ensureUniqueOnUpdate(existing, "one text");

            verify(repository, never()).existsByRoleAndTextIgnoreCase(any(), any());
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("changed text and unique -> queries repo, OK")
        void changed_unique_ok() {
            var existing = SupportIssuePreset.builder()
                    .role(UserRole.HR)
                    .text("Old")
                    .build();

            when(repository.existsByRoleAndTextIgnoreCase(UserRole.HR, "New")).thenReturn(false);

            validator.ensureUniqueOnUpdate(existing, "New");

            verify(repository).existsByRoleAndTextIgnoreCase(UserRole.HR, "New");
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("changed text but duplicate -> throws")
        void changed_duplicate_throw() {
            var existing = SupportIssuePreset.builder()
                    .role(UserRole.MENTOR)
                    .text("Old")
                    .build();

            when(repository.existsByRoleAndTextIgnoreCase(UserRole.MENTOR, "New")).thenReturn(true);

            assertThatThrownBy(() -> validator.ensureUniqueOnUpdate(existing, "New"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Preset with same text already exists for this role");

            verify(repository).existsByRoleAndTextIgnoreCase(UserRole.MENTOR, "New");
            verifyNoMoreInteractions(repository);
        }
    }
}
