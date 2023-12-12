package ru.practicum.ewm.api.participationrequest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.participationrequest.controller.ParticipationRequestControllerPrivate;
import ru.practicum.ewm.participationrequest.model.ParticipationRequestStatus;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestDto;
import ru.practicum.ewm.participationrequest.service.ParticipationRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ParticipationRequestControllerPrivate.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ParticipationRequestControllerPrivateTest {
    @MockBean
    private ParticipationRequestService requestService;
    private final MockMvc mvc;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ParticipationRequestDto requestDto = new ParticipationRequestDto(
            1,
            LocalDateTime.now().minusMinutes(1),
            1,
            1,
            ParticipationRequestStatus.PENDING
    );

    @Test
    public void shouldAddRequest() throws Exception {
        Mockito
                .when(requestService.addParticipationRequest(anyInt(), anyInt()))
                .thenReturn(requestDto);

        mvc.perform(post("/users/1/requests?eventId=1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(requestDto.getId()))
                .andExpect(jsonPath("$.created").value(requestDto.getCreated().format(formatter)))
                .andExpect(jsonPath("$.event").value(requestDto.getEvent()))
                .andExpect(jsonPath("$.requester").value(requestDto.getRequester()))
                .andExpect(jsonPath("$.status").value(requestDto.getStatus().toString()));
    }

    @Test
    public void shouldGetRequests() throws Exception {
        Mockito
                .when(requestService.getParticipationRequestsByRequester(anyInt()))
                .thenReturn(List.of(requestDto));

        mvc.perform(get("/users/1/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(requestDto.getId()))
                .andExpect(jsonPath("$.[0].created").value(requestDto.getCreated().format(formatter)))
                .andExpect(jsonPath("$.[0].event").value(requestDto.getEvent()))
                .andExpect(jsonPath("$.[0].requester").value(requestDto.getRequester()))
                .andExpect(jsonPath("$.[0].status").value(requestDto.getStatus().toString()));
    }

    @Test
    public void shouldCancelRequest() throws Exception {
        ParticipationRequestDto canceledRequestDto = new ParticipationRequestDto(
                requestDto.getId(),
                requestDto.getCreated(),
                requestDto.getEvent(),
                requestDto.getRequester(),
                ParticipationRequestStatus.CANCELED
        );
        Mockito
                .when(requestService.cancelParticipationRequest(anyInt(), anyInt()))
                .thenReturn(canceledRequestDto);

        mvc.perform(patch("/users/1/requests/1/cancel")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(canceledRequestDto.getId()))
                .andExpect(jsonPath("$.created").value(canceledRequestDto.getCreated().format(formatter)))
                .andExpect(jsonPath("$.event").value(canceledRequestDto.getEvent()))
                .andExpect(jsonPath("$.requester").value(canceledRequestDto.getRequester()))
                .andExpect(jsonPath("$.status").value(canceledRequestDto.getStatus().toString()));
    }
}