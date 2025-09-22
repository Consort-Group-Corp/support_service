package uz.consortgroup.support_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.consortgroup.core.api.v1.dto.support.request.CreatePresetRequestDto;
import uz.consortgroup.core.api.v1.dto.support.request.UpdatePresetRequestDto;
import uz.consortgroup.core.api.v1.dto.support.response.PresetResponse;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.exception.PresetNotFoundExecption;
import uz.consortgroup.support_service.service.super_admin.SupportPresetSuperAdminService;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SupportPresetSuperAdminController.class)
class SupportPresetSuperAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupportPresetSuperAdminService service;


    @Test
    @DisplayName("POST /presets/super-admin -> 201 Created")
    void create_ok() throws Exception {
        var req = CreatePresetRequestDto.builder()
                .role(UserRole.MENTOR)
                .text("Не получается сохранить курс")
                .sortOrder(2)
                .active(true)
                .build();

        var resp = PresetResponse.builder()
                .id(UUID.randomUUID())
                .role(UserRole.MENTOR)
                .text("Не получается сохранить курс")
                .sortOrder(2)
                .active(true)
                .build();

        Mockito.when(service.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/support/presets/super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.role", is("MENTOR")))
                .andExpect(jsonPath("$.text", is("Не получается сохранить курс")))
                .andExpect(jsonPath("$.sortOrder", is(2)))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @DisplayName("POST /presets/super-admin -> 400 (бизнес-валидация: дубликат)")
    void create_duplicate_badRequest() throws Exception {
        var req = CreatePresetRequestDto.builder()
                .role(UserRole.MENTOR)
                .text("Не получается сохранить курс")
                .build();

        Mockito.when(service.create(any()))
                .thenThrow(new IllegalArgumentException("Preset with same text already exists for this role"));

        mockMvc.perform(post("/api/v1/support/presets/super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsStringIgnoringCase("bad")))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("POST /presets/super-admin -> 400 (bean validation: пустые поля)")
    void create_validation_badRequest() throws Exception {
        // Отправляем заведомо невалидный JSON (например, без role/text)
        var raw = """
                {"sortOrder":1,"active":true}
                """;

        mockMvc.perform(post("/api/v1/support/presets/super-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(raw))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsStringIgnoringCase("validation")));
    }


    @Test
    @DisplayName("GET /presets/super-admin -> 200 OK (все роли)")
    void list_all_ok() throws Exception {
        var p1 = PresetResponse.builder()
                .id(UUID.randomUUID()).role(UserRole.HR).text("Как добавить сотрудников").sortOrder(1).active(true).build();
        var p2 = PresetResponse.builder()
                .id(UUID.randomUUID()).role(UserRole.MENTOR).text("Не получается сохранить курс").sortOrder(2).active(true).build();

        Mockito.when(service.list(isNull())).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/v1/support/presets/super-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.role=='HR')]").exists())
                .andExpect(jsonPath("$[?(@.role=='MENTOR')]").exists());
    }

    @Test
    @DisplayName("GET /presets/super-admin?role=MENTOR -> 200 OK (по роли)")
    void list_byRole_ok() throws Exception {
        var p = PresetResponse.builder()
                .id(UUID.randomUUID()).role(UserRole.MENTOR).text("Не можем добавить материалы").sortOrder(1).active(true).build();

        Mockito.when(service.list(eq(UserRole.MENTOR))).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/support/presets/super-admin")
                        .param("role", "MENTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role", is("MENTOR")));
    }


    @Test
    @DisplayName("PUT /presets/super-admin/{id} -> 200 OK")
    void update_ok() throws Exception {
        UUID id = UUID.randomUUID();

        var req = UpdatePresetRequestDto.builder()
                .text("Обновленный текст")
                .sortOrder(5)
                .active(false)
                .build();

        var resp = PresetResponse.builder()
                .id(id).role(UserRole.ADMIN).text("Обновленный текст").sortOrder(5).active(false).build();

        Mockito.when(service.update(eq(id), any())).thenReturn(resp);

        mockMvc.perform(put("/api/v1/support/presets/super-admin/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.text", is("Обновленный текст")))
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    @DisplayName("PUT /presets/super-admin/{id} -> 404 Not Found")
    void update_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        var req = UpdatePresetRequestDto.builder().text("any").build();

        Mockito.when(service.update(eq(id), any()))
                .thenThrow(new PresetNotFoundExecption("Preset not found"));

        mockMvc.perform(put("/api/v1/support/presets/super-admin/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsStringIgnoringCase("not found")));
    }

    @Test
    @DisplayName("PUT /presets/super-admin/{id} -> 400 Bad Request (невалидный UUID)")
    void update_badUuid() throws Exception {
        var req = UpdatePresetRequestDto.builder().text("ok").build();

        mockMvc.perform(put("/api/v1/support/presets/super-admin/not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("DELETE /presets/super-admin/{id} -> 204 No Content")
    void delete_ok() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/support/presets/super-admin/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /presets/super-admin/{id} -> 404 Not Found")
    void delete_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new PresetNotFoundExecption("Preset not found")).when(service).delete(id);

        mockMvc.perform(delete("/api/v1/support/presets/super-admin/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Preset not found")));
    }

    @Test
    @DisplayName("DELETE /presets/super-admin/{id} -> 400 Bad Request (невалидный UUID)")
    void delete_badUuid() throws Exception {
        mockMvc.perform(delete("/api/v1/support/presets/super-admin/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}
