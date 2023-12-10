package ru.practicum.ewm.participationrequest.model.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ParticipationRequestStatusUpdateResult {
    private final List<ParticipationRequestDto> confirmedRequests;
    private final List<ParticipationRequestDto> rejectedRequests;
}