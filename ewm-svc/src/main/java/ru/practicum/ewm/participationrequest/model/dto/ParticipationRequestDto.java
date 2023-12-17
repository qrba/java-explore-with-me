package ru.practicum.ewm.participationrequest.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.participationrequest.model.ParticipationRequestStatus;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class ParticipationRequestDto {
    private final Integer id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime created;
    private final Integer event;
    private final Integer requester;
    private final ParticipationRequestStatus status;
}