package ru.practicum.ewm.stats.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.stats.service.StatsService;
import ru.practicum.ewm.stats.controller.StatsController;
import ru.practicum.ewm.dto.EndpointHitDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsControllerTest {
    @MockBean
    private StatsService statsService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    public void shouldSaveEvent() throws Exception {
        EndpointHitDto endpointHitDto = new EndpointHitDto(
                "test-application",
                "/test",
                "192.168.0.1",
                LocalDateTime.now().minusHours(1).format(formatter)
        );

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHitDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        Mockito.verify(statsService).saveEvent(any(EndpointHitDto.class));
    }

    @Test
    public void shouldNotSaveEventWhenAppIsBlank() throws Exception {
        EndpointHitDto endpointHitDto = new EndpointHitDto(
                "",
                "/test",
                "192.168.0.1",
                LocalDateTime.now().minusHours(1).format(formatter)
        );

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHitDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Идентификатор сервиса не может быть пустым"));
    }

    @Test
    public void shouldNotSaveEventWhenUriIsBlank() throws Exception {
        EndpointHitDto endpointHitDto = new EndpointHitDto(
                "test-application",
                "",
                "192.168.0.1",
                LocalDateTime.now().minusHours(1).format(formatter)
        );

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHitDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("URI не может быть пустым"));
    }

    @Test
    public void shouldNotSaveEventWhenIpIsBlank() throws Exception {
        EndpointHitDto endpointHitDto = new EndpointHitDto(
                "test-application",
                "/test",
                "",
                LocalDateTime.now().minusHours(1).format(formatter)
        );

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHitDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IP-адрес не может быть пустым"));
    }

    @Test
    public void shouldGetStats() throws Exception {
        ViewStatsDto viewStatsDto = new ViewStatsDto(
                "test-application",
                "/test",
                1L
        );

        Mockito
                .when(
                        statsService.getStats(
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        anyList(),
                        anyBoolean()
                        )
                )
                .thenReturn(List.of(viewStatsDto));

        mvc.perform(get("/stats")
                        .param("start", "2020-10-10 00:00:00")
                        .param("end", "2023-10-10 00:00:00")
                        .param("uris", "/test")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].app").value(viewStatsDto.getApp()))
                .andExpect(jsonPath("$.[0].uri").value(viewStatsDto.getUri()))
                .andExpect(jsonPath("$.[0].hits").value(viewStatsDto.getHits()));
    }

    @Test
    public void shouldNotGetStatsWhenNoStart() throws Exception {
        mvc.perform(get("/stats")
                        .param("end", "2023-10-10 00:00:00")
                        .param("uris", "/test")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldNotGetStatsWhenNoEnd() throws Exception {
        mvc.perform(get("/stats")
                        .param("start", "2020-10-10 00:00:00")
                        .param("uris", "/test")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
