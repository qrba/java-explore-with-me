package ru.practicum.ewm.participationrequest.model.dto;

import ru.practicum.ewm.participationrequest.model.ParticipationRequest;

public class ParticipationRequestMapper {
    public static ParticipationRequestDto participationRequestToDto(ParticipationRequest participationRequest) {
        return new ParticipationRequestDto(
                participationRequest.getId(),
                participationRequest.getCreated(),
                participationRequest.getEvent().getId(),
                participationRequest.getRequester().getId(),
                participationRequest.getStatus()
        );
    }
}