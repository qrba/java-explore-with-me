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
import ru.practicum.ewm.event.controller.EventControllerPrivate;
import ru.practicum.ewm.event.model.ActionState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.dto.NewEventDto;
import ru.practicum.ewm.event.model.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.participationrequest.model.ParticipationRequestStatus;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestStatusUpdateRequest;
import ru.practicum.ewm.participationrequest.service.ParticipationRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventControllerPrivate.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventControllerPrivateTest {
    @MockBean
    private EventService eventService;
    @MockBean
    private ParticipationRequestService requestService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @Test
    public void shouldAddEvent() throws Exception {
        NewEventDto eventDto = new NewEventDto(
                "a".repeat(21),
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "title"
        );

        mvc.perform(post("/users/1/events")
                        .content(mapper.writeValueAsString(eventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Mockito.verify(eventService).addEvent(anyInt(), any(NewEventDto.class));
    }

    @Test
    public void shouldNotAddEventWhenAnnotationTooShort() throws Exception {
        NewEventDto eventDto = new NewEventDto(
                "a",
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "title"
        );

        mvc.perform(post("/users/1/events")
                        .content(mapper.writeValueAsString(eventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddEventWhenAnnotationTooLong() throws Exception {
        NewEventDto eventDto = new NewEventDto(
                "a".repeat(2001),
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "title"
        );

        mvc.perform(post("/users/1/events")
                        .content(mapper.writeValueAsString(eventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddEventWhenDescriptionTooShort() throws Exception {
        NewEventDto eventDto = new NewEventDto(
                "a".repeat(21),
                1,
                "d",
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "title"
        );

        mvc.perform(post("/users/1/events")
                        .content(mapper.writeValueAsString(eventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddEventWhenDescriptionTooLong() throws Exception {
        NewEventDto eventDto = new NewEventDto(
                "a".repeat(21),
                1,
                "d".repeat(7001),
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "title"
        );

        mvc.perform(post("/users/1/events")
                        .content(mapper.writeValueAsString(eventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddEventWhenTitleTooShort() throws Exception {
        NewEventDto eventDto = new NewEventDto(
                "a".repeat(21),
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "t"
        );

        mvc.perform(post("/users/1/events")
                        .content(mapper.writeValueAsString(eventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddEventWhenTitleTooLong() throws Exception {
        NewEventDto eventDto = new NewEventDto(
                "a".repeat(21),
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "t".repeat(121)
        );

        mvc.perform(post("/users/1/events")
                        .content(mapper.writeValueAsString(eventDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldUpdateEventByInitiator() throws Exception {
        UpdateEventUserRequest userRequest = new UpdateEventUserRequest(
                "a".repeat(21),
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "title",
                ActionState.SEND_TO_REVIEW
        );

        mvc.perform(patch("/users/1/events/1")
                        .content(mapper.writeValueAsString(userRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(eventService).updateEventByInitiator(anyInt(), anyInt(), any(UpdateEventUserRequest.class));
    }

    @Test
    public void shouldNotUpdateEventByInitiatorWhenAnnotationTooShort() throws Exception {
        UpdateEventUserRequest userRequest = new UpdateEventUserRequest(
                "a",
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "title",
                ActionState.SEND_TO_REVIEW
        );

        mvc.perform(patch("/users/1/events/1")
                        .content(mapper.writeValueAsString(userRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotUpdateEventByInitiatorWhenAnnotationTooLong() throws Exception {
        UpdateEventUserRequest userRequest = new UpdateEventUserRequest(
                "a".repeat(2001),
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "title",
                ActionState.SEND_TO_REVIEW
        );

        mvc.perform(patch("/users/1/events/1")
                        .content(mapper.writeValueAsString(userRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotUpdateEventByInitiatorWhenDescriptionTooShort() throws Exception {
        UpdateEventUserRequest userRequest = new UpdateEventUserRequest(
                "a".repeat(21),
                1,
                "d",
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "title",
                ActionState.SEND_TO_REVIEW
        );

        mvc.perform(patch("/users/1/events/1")
                        .content(mapper.writeValueAsString(userRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotUpdateEventByInitiatorWhenDescriptionTooLong() throws Exception {
        UpdateEventUserRequest userRequest = new UpdateEventUserRequest(
                "a".repeat(21),
                1,
                "d".repeat(7001),
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "title",
                ActionState.SEND_TO_REVIEW
        );

        mvc.perform(patch("/users/1/events/1")
                        .content(mapper.writeValueAsString(userRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotUpdateEventByInitiatorWhenTitleTooShort() throws Exception {
        UpdateEventUserRequest userRequest = new UpdateEventUserRequest(
                "a".repeat(21),
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "t",
                ActionState.SEND_TO_REVIEW
        );

        mvc.perform(patch("/users/1/events/1")
                        .content(mapper.writeValueAsString(userRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotUpdateEventByInitiatorWhenTitleTooLong() throws Exception {
        UpdateEventUserRequest userRequest = new UpdateEventUserRequest(
                "a".repeat(21),
                1,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                new Location(0, 0),
                false,
                0,
                false,
                "t".repeat(121),
                ActionState.SEND_TO_REVIEW
        );

        mvc.perform(patch("/users/1/events/1")
                        .content(mapper.writeValueAsString(userRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldGetEventsByInitiator() throws Exception {
        mvc.perform(get("/users/1/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(eventService).getEventsByInitiatorId(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void shouldGetEventsByIdAndInitiator() throws Exception {
        mvc.perform(get("/users/1/events/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(eventService).getEventByIdAndInitiatorId(anyInt(), anyInt());
    }

    @Test
    public void shouldGetRequestsByInitiator() throws Exception {
        mvc.perform(get("/users/1/events/1/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(requestService).getParticipationRequestsByEventInitiator(anyInt(), anyInt());
    }

    @Test
    public void shouldUpdateRequestsByInitiator() throws Exception {
        ParticipationRequestStatusUpdateRequest updateRequest = new ParticipationRequestStatusUpdateRequest(
                Collections.emptyList(),
                ParticipationRequestStatus.CONFIRMED
        );

        mvc.perform(patch("/users/1/events/1/requests")
                        .content(mapper.writeValueAsString(updateRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(requestService)
                .updateParticipationRequests(anyInt(), anyInt(), any(ParticipationRequestStatusUpdateRequest.class));
    }
}