package ru.practicum.ewm.participationrequest.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.participationrequest.model.ParticipationRequest;
import ru.practicum.ewm.participationrequest.model.ParticipationRequestStatus;

import java.util.List;

public interface ParticipationRequestStorage extends JpaRepository<ParticipationRequest, Integer> {
    List<ParticipationRequest> findByRequesterId(Integer requesterId);

    List<ParticipationRequest> findByEventInitiatorId(Integer initiatorId);

    List<ParticipationRequest> findByIdInAndStatusAndEventId(
            List<Integer> ids,
            ParticipationRequestStatus status,
            Integer eventId
    );

    Integer countByStatusAndEventId(ParticipationRequestStatus status, Integer eventId);

    Boolean existsByRequesterIdAndEventId(Integer requesterId, Integer eventId);
}