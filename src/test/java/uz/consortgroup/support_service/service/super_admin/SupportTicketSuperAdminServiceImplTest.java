package uz.consortgroup.support_service.service.super_admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uz.consortgroup.core.api.v1.dto.support.enumeration.SupportTicketStatus;
import uz.consortgroup.core.api.v1.dto.support.response.SupportTicketResponse;
import uz.consortgroup.support_service.entity.SupportTicket;
import uz.consortgroup.support_service.exception.TicketNotFoundException;
import uz.consortgroup.support_service.mapper.SupportMapper;
import uz.consortgroup.support_service.repository.SupportTicketRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportTicketSuperAdminServiceImplTest {

    @Mock
    private SupportTicketRepository repository;

    @Mock
    private SupportMapper mapper;

    @InjectMocks
    private SupportTicketSuperAdminServiceImpl service;

    @Nested
    class ListTickets {

        @Test
        @DisplayName("listTickets(null, pageable) -> repository.findAll(pageable)")
        void list_all_whenStatusNull() {
            Pageable pageable = PageRequest.of(0, 10);

            var e1 = new SupportTicket(); e1.setId(UUID.randomUUID()); e1.setStatus(SupportTicketStatus.NEW);
            var e2 = new SupportTicket(); e2.setId(UUID.randomUUID()); e2.setStatus(SupportTicketStatus.IN_PROGRESS);

            when(repository.findAll(eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(e1, e2), pageable, 2));

            var d1 = mock(SupportTicketResponse.class);
            var d2 = mock(SupportTicketResponse.class);
            when(mapper.toDto(e1)).thenReturn(d1);
            when(mapper.toDto(e2)).thenReturn(d2);

            Page<SupportTicketResponse> out = service.listTickets(null, pageable);

            assertThat(out.getContent()).containsExactly(d1, d2);
            assertThat(out.getNumber()).isEqualTo(0);
            assertThat(out.getSize()).isEqualTo(10);

            verify(repository).findAll(eq(pageable));
            verify(repository, never()).findAllByStatus(any(), any());
            verify(mapper).toDto(e1);
            verify(mapper).toDto(e2);
            verifyNoMoreInteractions(repository, mapper);
        }

        @Test
        @DisplayName("listTickets(status, pageable) -> repository.findAllByStatus(status, pageable)")
        void list_byStatus() {
            Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Order.desc("createdAt")));
            var status = SupportTicketStatus.NEW;

            var entity = new SupportTicket();
            entity.setId(UUID.randomUUID());
            entity.setStatus(status);

            when(repository.findAllByStatus(eq(status), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

            var dto = mock(SupportTicketResponse.class);
            when(mapper.toDto(entity)).thenReturn(dto);

            Page<SupportTicketResponse> out = service.listTickets(status, pageable);

            assertThat(out.getContent()).containsExactly(dto);
            assertThat(out.getNumber()).isEqualTo(1);
            assertThat(out.getSize()).isEqualTo(5);

            verify(repository).findAllByStatus(eq(status), eq(pageable));
            verify(repository, never()).findAll(any(Pageable.class));
            verify(mapper).toDto(entity);
            verifyNoMoreInteractions(repository, mapper);
        }
    }

    @Nested
    class UpdateStatus {

        @Test
        @DisplayName("updateStatus -> OK: статус меняется, сохраняется и маппится")
        void update_ok() {
            UUID id = UUID.randomUUID();
            var newStatus = SupportTicketStatus.CLOSED;

            var existing = new SupportTicket();
            existing.setId(id);
            existing.setStatus(SupportTicketStatus.IN_PROGRESS);

            when(repository.findById(eq(id))).thenReturn(Optional.of(existing));

            var saved = new SupportTicket();
            saved.setId(id);
            saved.setStatus(newStatus);
            when(repository.save(same(existing))).thenReturn(saved);

            var resp = mock(SupportTicketResponse.class);
            when(mapper.toDto(eq(saved))).thenReturn(resp);

            var out = service.updateStatus(id, newStatus);

            assertThat(existing.getStatus()).isEqualTo(newStatus);
            assertThat(out).isSameAs(resp);

            ArgumentCaptor<SupportTicket> captor = ArgumentCaptor.forClass(SupportTicket.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(newStatus);

            verify(mapper).toDto(saved);
            verifyNoMoreInteractions(repository, mapper);
        }

        @Test
        @DisplayName("updateStatus -> not found: кидаем TicketNotFoundException")
        void update_notFound() {
            UUID id = UUID.randomUUID();
            when(repository.findById(eq(id))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateStatus(id, SupportTicketStatus.NEW))
                    .isInstanceOf(TicketNotFoundException.class);

            verify(repository).findById(eq(id));
            verify(repository, never()).save(any());
            verifyNoInteractions(mapper);
        }
    }
}
