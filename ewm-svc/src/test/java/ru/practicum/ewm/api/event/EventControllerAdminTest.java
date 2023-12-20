package ru.practicum.ewm.api.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.event.controller.EventControllerAdmin;
import ru.practicum.ewm.event.model.ActionState;
import ru.practicum.ewm.event.model.Coordinate;
import ru.practicum.ewm.event.model.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.service.EventService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventControllerAdmin.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventControllerAdminTest {
    @MockBean
    private EventService eventService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @Test
    public void shouldGetEventsByAdmin() throws Exception {
        mvc.perform(get("/admin/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(eventService).getEventsByAdmin(
                any(),
                any(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt()
        );
    }

    @Test
    public void shouldUpdateEventByAdmin() throws Exception {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest(
                "a".repeat(21),
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Coordinate(0, 0),
                false,
                0,
                false,
                "title",
                ActionState.PUBLISH_EVENT
        );

        mvc.perform(patch("/admin/events/1")
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(eventService).updateEventByAdmin(anyInt(), any(UpdateEventAdminRequest.class));
    }

    @Test
    public void shouldNotUpdateEventByAdminWhenAnnotationTooShort() throws Exception {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest(
                "a",
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Coordinate(0, 0),
                false,
                0,
                false,
                "title",
                ActionState.PUBLISH_EVENT
        );

        mvc.perform(patch("/admin/events/1")
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotUpdateEventByAdminWhenAnnotationTooLong() throws Exception {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest(
                "a".repeat(2001),
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Coordinate(0, 0),
                false,
                0,
                false,
                "title",
                ActionState.PUBLISH_EVENT
        );

        mvc.perform(patch("/admin/events/1")
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotUpdateEventByAdminWhenDescriptionTooShort() throws Exception {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest(
                "a".repeat(21),
                1,
                "d".repeat(1),
                LocalDateTime.now().plusDays(1),
                new Coordinate(0, 0),
                false,
                0,
                false,
                "title",
                ActionState.PUBLISH_EVENT
        );

        mvc.perform(patch("/admin/events/1")
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotUpdateEventByAdminWhenDescriptionTooLong() throws Exception {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest(
                "a".repeat(21),
                1,
                "d".repeat(7001),
                LocalDateTime.now().plusDays(1),
                new Coordinate(0, 0),
                false,
                0,
                false,
                "title",
                ActionState.PUBLISH_EVENT
        );

        mvc.perform(patch("/admin/events/1")
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotUpdateEventByAdminWhenTitleTooShort() throws Exception {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest(
                "a".repeat(21),
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Coordinate(0, 0),
                false,
                0,
                false,
                "t",
                ActionState.PUBLISH_EVENT
        );

        mvc.perform(patch("/admin/events/1")
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotUpdateEventByAdminWhenTitleTooLong() throws Exception {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest(
                "a".repeat(21),
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Coordinate(0, 0),
                false,
                0,
                false,
                "t".repeat(121),
                ActionState.PUBLISH_EVENT
        );

        mvc.perform(patch("/admin/events/1")
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }
}