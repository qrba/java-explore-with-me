package ru.practicum.ewm.participationrequest.service;

import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestDto;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestStatusUpdateRequest;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestStatusUpdateResult;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getParticipationRequestsByEventInitiator(Integer userId, Integer eventId);

    ParticipationRequestStatusUpdateResult updateParticipationRequests(
            Integer userId,
            Integer eventId,
            ParticipationRequestStatusUpdateRequest updateRequest
    );

    List<ParticipationRequestDto> getParticipationRequestsByRequester(Integer requesterId);

    ParticipationRequestDto addParticipationRequest(Integer userId, Integer eventId);

    ParticipationRequestDto cancelParticipationRequest(Integer userId, Integer requestId);
}