package ru.practicum.statsvc.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.statsvc.controller.EventController;
import ru.practicum.statsvc.model.dto.EventDtoIn;
import ru.practicum.statsvc.model.dto.EventDtoOut;
import ru.practicum.statsvc.service.EventService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventControllerTest {
    @MockBean
    private EventService eventService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @Test
    public void shouldSaveEvent() throws Exception {
        EventDtoIn eventDtoIn = new EventDtoIn(
                "test-application",
                "/test",
                "192.168.0.1",
                LocalDateTime.now().minusHours(1)
        );

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(eventDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        Mockito.verify(eventService).saveEvent(any(EventDtoIn.class));
    }

    @Test
    public void shouldNotSaveEventWhenAppIsBlank() throws Exception {
        EventDtoIn eventDtoIn = new EventDtoIn(
                "",
                "/test",
                "192.168.0.1",
                LocalDateTime.now().minusHours(1)
        );

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(eventDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Идентификатор сервиса не может быть пустым"));
    }

    @Test
    public void shouldNotSaveEventWhenUriIsBlank() throws Exception {
        EventDtoIn eventDtoIn = new EventDtoIn(
                "test-application",
                "",
                "192.168.0.1",
                LocalDateTime.now().minusHours(1)
        );

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(eventDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("URI не может быть пустым"));
    }

    @Test
    public void shouldNotSaveEventWhenIpIsBlank() throws Exception {
        EventDtoIn eventDtoIn = new EventDtoIn(
                "test-application",
                "/test",
                "",
                LocalDateTime.now().minusHours(1)
        );

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(eventDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IP-адрес не может быть пустым"));
    }

    @Test
    public void shouldNotSaveEventWhenTimestampIsInFuture() throws Exception {
        EventDtoIn eventDtoIn = new EventDtoIn(
                "test-application",
                "/test",
                "192.168.0.1",
                LocalDateTime.now().plusHours(1)
        );

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(eventDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Дата и время запроса не могут быть в будущем"));
    }

    @Test
    public void shouldGetStats() throws Exception {
        EventDtoOut eventDtoOut = new EventDtoOut(
                "test-application",
                "/test",
                1L
        );

        Mockito
                .when(
                        eventService.getEvents(
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        anyList(),
                        anyBoolean()
                        )
                )
                .thenReturn(List.of(eventDtoOut));

        mvc.perform(get("/stats")
                        .param("start", "2020-10-10 00:00:00")
                        .param("end", "2023-10-10 00:00:00")
                        .param("uris", "/test")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].app").value(eventDtoOut.getApp()))
                .andExpect(jsonPath("$.[0].uri").value(eventDtoOut.getUri()))
                .andExpect(jsonPath("$.[0].hits").value(eventDtoOut.getHits()));
    }

    @Test
    public void shouldNotGetStatsWhenNoStart() throws Exception {
        mvc.perform(get("/stats")
                        .param("end", "2023-10-10 00:00:00")
                        .param("uris", "/test")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void shouldNotGetStatsWhenNoEnd() throws Exception {
        mvc.perform(get("/stats")
                        .param("start", "2020-10-10 00:00:00")
                        .param("uris", "/test")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
