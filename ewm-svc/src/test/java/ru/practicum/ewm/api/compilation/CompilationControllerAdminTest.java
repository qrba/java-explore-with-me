package ru.practicum.ewm.api.compilation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.compilation.controller.CompilationControllerAdmin;
import ru.practicum.ewm.compilation.model.dto.CompilationDto;
import ru.practicum.ewm.compilation.model.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.service.CompilationService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CompilationControllerAdmin.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CompilationControllerAdminTest {
    @MockBean
    private CompilationService compilationService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    private final CompilationDto compilationDto = new CompilationDto(
            1,
            false,
            "title",
            Collections.emptyList()
    );

    @Test
    public void shouldAddCompilation() throws Exception {
        NewCompilationDto newCompilationDto =
                new NewCompilationDto(false, "title", Collections.emptyList());
        Mockito
                .when(compilationService.addCompilation(any(NewCompilationDto.class)))
                .thenReturn(compilationDto);

        mvc.perform(post("/admin/compilations")
                        .content(mapper.writeValueAsString(newCompilationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(compilationDto.getId()))
                .andExpect(jsonPath("$.pinned").value(compilationDto.getPinned()))
                .andExpect(jsonPath("$.title").value(compilationDto.getTitle()));
    }

    @Test
    public void shouldNotAddCompilationWhenBlankName() throws Exception {
        NewCompilationDto newCompilationDto =
                new NewCompilationDto(false, "", Collections.emptyList());

        mvc.perform(post("/admin/compilations")
                        .content(mapper.writeValueAsString(newCompilationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddCompilationWhenNameTooLong() throws Exception {
        NewCompilationDto newCompilationDto = new NewCompilationDto(
                false,
                "Compilation names longer than 50 characters are prohibited",
                Collections.emptyList()
        );

        mvc.perform(post("/admin/compilations")
                        .content(mapper.writeValueAsString(newCompilationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldDeleteCompilation() throws Exception {
        mvc.perform(delete("/admin/compilations/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Mockito.verify(compilationService).deleteCompilation(anyInt());
    }

    @Test
    public void shouldUpdateCompilation() throws Exception {
        UpdateCompilationRequest updateCompilationRequest =
                new UpdateCompilationRequest(false, "title", Collections.emptyList());
        Mockito
                .when(compilationService.updateCompilation(any(UpdateCompilationRequest.class), anyInt()))
                .thenReturn(compilationDto);

        mvc.perform(patch("/admin/compilations/1")
                        .content(mapper.writeValueAsString(updateCompilationRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compilationDto.getId()))
                .andExpect(jsonPath("$.pinned").value(compilationDto.getPinned()))
                .andExpect(jsonPath("$.title").value(compilationDto.getTitle()));
    }
}