package ru.practicum.ewm.api.event;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.event.controller.EventControllerPublic;
import ru.practicum.ewm.event.model.SortType;
import ru.practicum.ewm.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventControllerPublic.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventControllerPublicTest {
    @MockBean
    private EventService eventService;
    private final MockMvc mvc;

    @Test
    public void shouldGetEventById() throws Exception {
        mvc.perform(get("/events/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(eventService).getEventById(anyInt(), any(HttpServletRequest.class));
    }

    @Test
    public void shouldGetEvents() throws Exception {
        mvc.perform(get("/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(eventService).getEvents(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyBoolean(),
                any(SortType.class),
                anyInt(),
                anyInt(),
                any(HttpServletRequest.class)
        );
    }
}