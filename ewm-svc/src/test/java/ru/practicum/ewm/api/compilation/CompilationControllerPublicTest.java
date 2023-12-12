package ru.practicum.ewm.api.compilation;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.compilation.controller.CompilationControllerPublic;
import ru.practicum.ewm.compilation.model.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CompilationControllerPublic.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CompilationControllerPublicTest {
    @MockBean
    private CompilationService compilationService;
    private final MockMvc mvc;

    private final CompilationDto compilationDto = new CompilationDto(
            1,
            false,
            "title",
            Collections.emptyList()
    );

    @Test
    public void shouldGetCompilationById() throws Exception {
        Mockito
                .when(compilationService.getCompilationById(anyInt()))
                .thenReturn(compilationDto);

        mvc.perform(get("/compilations/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compilationDto.getId()))
                .andExpect(jsonPath("$.pinned").value(compilationDto.getPinned()))
                .andExpect(jsonPath("$.title").value(compilationDto.getTitle()));
    }

    @Test
    public void shouldGetCompilations() throws Exception {
        Mockito
                .when(compilationService.getCompilations(any(), anyInt(), anyInt()))
                .thenReturn(List.of(compilationDto));

        mvc.perform(get("/compilations")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(compilationDto.getId()))
                .andExpect(jsonPath("$.[0].pinned").value(compilationDto.getPinned()))
                .andExpect(jsonPath("$.[0].title").value(compilationDto.getTitle()));
    }
}