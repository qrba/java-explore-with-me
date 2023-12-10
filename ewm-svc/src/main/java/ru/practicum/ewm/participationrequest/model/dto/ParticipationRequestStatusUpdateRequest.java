package ru.practicum.ewm.participationrequest.model.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.participationrequest.model.ParticipationRequestStatus;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ParticipationRequestStatusUpdateRequest {
    private final List<Integer> requestIds;
    private final ParticipationRequestStatus status;
}