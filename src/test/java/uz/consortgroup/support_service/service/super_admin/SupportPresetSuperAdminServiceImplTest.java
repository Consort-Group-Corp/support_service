package uz.consortgroup.support_service.service.super_admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.support.request.CreatePresetRequestDto;
import uz.consortgroup.core.api.v1.dto.support.request.UpdatePresetRequestDto;
import uz.consortgroup.core.api.v1.dto.support.response.PresetResponse;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.entity.SupportIssuePreset;
import uz.consortgroup.support_service.exception.PresetNotFoundExecption;
import uz.consortgroup.support_service.mapper.SupportMapper;
import uz.consortgroup.support_service.repository.SupportIssuePresetRepository;
import uz.consortgroup.support_service.validator.SupportPresetValidator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportPresetSuperAdminServiceImplTest {

    @Mock
    private SupportIssuePresetRepository repository;

    @Mock
    private SupportMapper mapper;

    @Mock
    private SupportPresetValidator validator;

    @InjectMocks
    private SupportPresetSuperAdminServiceImpl service;

    @Nested
    class CreateTests {
        @Test
        @DisplayName("create: ok")
        void create_ok() {
            var req = CreatePresetRequestDto.builder()
                    .role(UserRole.MENTOR)
                    .text("  Text  ")
                    .sortOrder(5)
                    .active(true)
                    .build();

            when(validator.normalizeTextOrThrow("  Text  ")).thenReturn("Text");
            doNothing().when(validator).ensureUniqueOnCreate(UserRole.MENTOR, "Text");

            var saved = SupportIssuePreset.builder()
                    .id(UUID.randomUUID())
                    .role(UserRole.MENTOR)
                    .text("Text")
                    .sortOrder(5)
                    .active(true)
                    .build();

            when(repository.save(any(SupportIssuePreset.class))).thenReturn(saved);

            PresetResponse resp = mock(PresetResponse.class);
            when(mapper.toPresetDto(saved)).thenReturn(resp);

            var result = service.create(req);

            assertThat(result).isSameAs(resp);

            ArgumentCaptor<SupportIssuePreset> captor = ArgumentCaptor.forClass(SupportIssuePreset.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(UserRole.MENTOR);
            assertThat(captor.getValue().getText()).isEqualTo("Text");
            assertThat(captor.getValue().getSortOrder()).isEqualTo(5);
            assertThat(captor.getValue().isActive()).isTrue();

            verify(validator).ensureUniqueOnCreate(UserRole.MENTOR, "Text");
            verify(mapper).toPresetDto(saved);
        }

        @Test
        @DisplayName("create: default sortOrder=0 and active=true")
        void create_defaults() {
            var req = CreatePresetRequestDto.builder()
                    .role(UserRole.HR)
                    .text("Preset")
                    .build();

            when(validator.normalizeTextOrThrow("Preset")).thenReturn("Preset");
            doNothing().when(validator).ensureUniqueOnCreate(UserRole.HR, "Preset");

            var saved = SupportIssuePreset.builder()
                    .id(UUID.randomUUID())
                    .role(UserRole.HR)
                    .text("Preset")
                    .sortOrder(0)
                    .active(true)
                    .build();

            when(repository.save(any(SupportIssuePreset.class))).thenReturn(saved);
            PresetResponse resp = mock(PresetResponse.class);
            when(mapper.toPresetDto(saved)).thenReturn(resp);

            var result = service.create(req);

            assertThat(result).isSameAs(resp);
            ArgumentCaptor<SupportIssuePreset> captor = ArgumentCaptor.forClass(SupportIssuePreset.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getSortOrder()).isEqualTo(0);
            assertThat(captor.getValue().isActive()).isTrue();
        }

        @Test
        @DisplayName("create: invalid text -> IllegalArgumentException")
        void create_invalidText() {
            var req = CreatePresetRequestDto.builder()
                    .role(UserRole.ADMIN)
                    .text("   ")
                    .build();

            when(validator.normalizeTextOrThrow("   ")).thenThrow(new IllegalArgumentException("Text must not be blank"));

            assertThatThrownBy(() -> service.create(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("create: duplicate -> IllegalArgumentException")
        void create_duplicate() {
            var req = CreatePresetRequestDto.builder()
                    .role(UserRole.ADMIN)
                    .text("Duplicate")
                    .build();

            when(validator.normalizeTextOrThrow("Duplicate")).thenReturn("Duplicate");
            doThrow(new IllegalArgumentException("already exists"))
                    .when(validator).ensureUniqueOnCreate(UserRole.ADMIN, "Duplicate");

            assertThatThrownBy(() -> service.create(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    class UpdateTests {
        @Test
        @DisplayName("update: ok with text/sort/active")
        void update_ok_all() {
            UUID id = UUID.randomUUID();

            var existing = SupportIssuePreset.builder()
                    .id(id)
                    .role(UserRole.MENTOR)
                    .text("Old")
                    .sortOrder(1)
                    .active(true)
                    .build();

            when(repository.findById(id)).thenReturn(Optional.of(existing));

            var req = UpdatePresetRequestDto.builder()
                    .text(" New  ")
                    .sortOrder(10)
                    .active(false)
                    .build();

            when(validator.normalizeTextOrThrow(" New  ")).thenReturn("New");
            doNothing().when(validator).ensureUniqueOnUpdate(existing, "New");

            var saved = SupportIssuePreset.builder()
                    .id(id)
                    .role(UserRole.MENTOR)
                    .text("New")
                    .sortOrder(10)
                    .active(false)
                    .build();

            when(repository.save(existing)).thenReturn(saved);
            PresetResponse resp = mock(PresetResponse.class);
            when(mapper.toPresetDto(saved)).thenReturn(resp);

            var result = service.update(id, req);

            assertThat(result).isSameAs(resp);
            assertThat(existing.getText()).isEqualTo("New");
            assertThat(existing.getSortOrder()).isEqualTo(10);
            assertThat(existing.isActive()).isFalse();

            verify(validator).ensureUniqueOnUpdate(existing, "New");
        }

        @Test
        @DisplayName("update: only sort and active")
        void update_onlySortActive() {
            UUID id = UUID.randomUUID();

            var existing = SupportIssuePreset.builder()
                    .id(id)
                    .role(UserRole.HR)
                    .text("Same")
                    .sortOrder(1)
                    .active(true)
                    .build();

            when(repository.findById(id)).thenReturn(Optional.of(existing));

            var req = UpdatePresetRequestDto.builder()
                    .sortOrder(7)
                    .active(false)
                    .build();

            var saved = SupportIssuePreset.builder()
                    .id(id)
                    .role(UserRole.HR)
                    .text("Same")
                    .sortOrder(7)
                    .active(false)
                    .build();

            when(repository.save(existing)).thenReturn(saved);
            PresetResponse resp = mock(PresetResponse.class);
            when(mapper.toPresetDto(saved)).thenReturn(resp);

            var result = service.update(id, req);

            assertThat(result).isSameAs(resp);
            assertThat(existing.getText()).isEqualTo("Same");
            assertThat(existing.getSortOrder()).isEqualTo(7);
            assertThat(existing.isActive()).isFalse();

            verify(validator, never()).normalizeTextOrThrow(anyString());
            verify(validator, never()).ensureUniqueOnUpdate(any(), anyString());
        }

        @Test
        @DisplayName("update: not found -> PresetNotFoundExecption")
        void update_notFound() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            var req = UpdatePresetRequestDto.builder().text("X").build();

            assertThatThrownBy(() -> service.update(id, req))
                    .isInstanceOf(PresetNotFoundExecption.class);

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("update: duplicate text -> IllegalArgumentException")
        void update_duplicateText() {
            UUID id = UUID.randomUUID();

            var existing = SupportIssuePreset.builder()
                    .id(id)
                    .role(UserRole.ADMIN)
                    .text("Old")
                    .sortOrder(0)
                    .active(true)
                    .build();

            when(repository.findById(id)).thenReturn(Optional.of(existing));

            var req = UpdatePresetRequestDto.builder().text("New").build();

            when(validator.normalizeTextOrThrow("New")).thenReturn("New");
            doThrow(new IllegalArgumentException("already exists"))
                    .when(validator).ensureUniqueOnUpdate(existing, "New");

            assertThatThrownBy(() -> service.update(id, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    class DeleteTests {
        @Test
        @DisplayName("delete: ok")
        void delete_ok() {
            UUID id = UUID.randomUUID();
            when(repository.existsById(id)).thenReturn(true);

            service.delete(id);

            verify(repository).deleteById(id);
        }

        @Test
        @DisplayName("delete: not found -> PresetNotFoundExecption")
        void delete_notFound() {
            UUID id = UUID.randomUUID();
            when(repository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(id))
                    .isInstanceOf(PresetNotFoundExecption.class);

            verify(repository, never()).deleteById(any());
        }
    }

    @Nested
    class ListTests {
        @Test
        @DisplayName("list: all roles (role=null)")
        void list_all() {
            var e1 = SupportIssuePreset.builder().id(UUID.randomUUID()).role(UserRole.MENTOR).text("A").sortOrder(1).active(true).build();
            var e2 = SupportIssuePreset.builder().id(UUID.randomUUID()).role(UserRole.HR).text("B").sortOrder(2).active(true).build();

            when(repository.findAllByOrderByRoleAscSortOrderAsc()).thenReturn(List.of(e1, e2));

            PresetResponse r1 = mock(PresetResponse.class);
            PresetResponse r2 = mock(PresetResponse.class);
            when(mapper.toPresetDto(e1)).thenReturn(r1);
            when(mapper.toPresetDto(e2)).thenReturn(r2);

            var out = service.list(null);

            assertThat(out).containsExactly(r1, r2);
        }

        @Test
        @DisplayName("list: by role")
        void list_byRole() {
            var e1 = SupportIssuePreset.builder().id(UUID.randomUUID()).role(UserRole.MENTOR).text("A").sortOrder(1).active(true).build();

            when(repository.findAllByRoleOrderBySortOrderAsc(UserRole.MENTOR)).thenReturn(List.of(e1));

            PresetResponse r1 = mock(PresetResponse.class);
            when(mapper.toPresetDto(e1)).thenReturn(r1);

            var out = service.list(UserRole.MENTOR);

            assertThat(out).containsExactly(r1);
        }
    }
}
