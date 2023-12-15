package ru.practicum.ewm.participationrequest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.exception.EventNotFoundException;
import ru.practicum.ewm.exception.OperationConditionsFailureException;
import ru.practicum.ewm.exception.ParticipationRequestAlreadyExistsException;
import ru.practicum.ewm.exception.ParticipationRequestNotFoundException;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.participationrequest.model.ParticipationRequest;
import ru.practicum.ewm.participationrequest.model.ParticipationRequestStatus;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestDto;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestMapper;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestStatusUpdateRequest;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestStatusUpdateResult;
import ru.practicum.ewm.participationrequest.storage.ParticipationRequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestMapper.participationRequestToDto;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository requestStorage;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getParticipationRequestsByEventInitiator(
            Integer initiatorId,
            Integer eventId
    ) {
        if (!userRepository.existsById(initiatorId))
            throw new UserNotFoundException("Пользователь с id=" + initiatorId + " не найден");
        if (!eventRepository.existsById(eventId))
            throw new EventNotFoundException("Событие с id=" + eventId + " не найдено");
        log.info("Инициатором с id=" + initiatorId + " события с id=" + eventId + " запрошены заявки на участие");
        return requestStorage.findByEventIdAndEventInitiatorId(eventId, initiatorId).stream()
                .map(ParticipationRequestMapper::participationRequestToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestStatusUpdateResult updateParticipationRequests(
            Integer initiatorId,
            Integer eventId,
            ParticipationRequestStatusUpdateRequest updateRequest
    ) {
        if (!userRepository.existsById(initiatorId))
            throw new UserNotFoundException("Пользователь с id=" + initiatorId + " не найден");
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие с id=" + eventId + " не найдено"));
        if (!event.getInitiator().getId().equals(initiatorId))
            throw new OperationConditionsFailureException(
                    "Пользователь с id=" + initiatorId +
                            " не является инициатором события с id=" + eventId
            );
        if (!event.isRequestModeration()) throw new OperationConditionsFailureException(
                "Для события с id=" + eventId + " одобрение заявок не требуется"
        );
        int limit = event.getParticipantLimit();
        int vacant = limit - requestStorage.countByStatusAndEventId(ParticipationRequestStatus.CONFIRMED, eventId);
        if (limit > 0 && vacant == 0) throw new OperationConditionsFailureException(
                "Достигнут лимит одобренных заявок для события с id=" + eventId
        );
        List<Integer> ids = updateRequest.getRequestIds();
        List<ParticipationRequest> requests = requestStorage.findByIdInAndStatusAndEventId(
                ids,
                ParticipationRequestStatus.PENDING,
                eventId
        );
        ParticipationRequestStatus status = updateRequest.getStatus();
        if (status.equals(ParticipationRequestStatus.REJECTED)) {
            for (ParticipationRequest request : requests)
                request.setStatus(ParticipationRequestStatus.REJECTED);
        } else if (status.equals(ParticipationRequestStatus.CONFIRMED)) {
            int size = requests.size();
            for (int i = 0; (i < vacant && i < size); i++)
                requests.get(i).setStatus(ParticipationRequestStatus.CONFIRMED);
            if (size > vacant)
                for (int i = vacant; i < size; i++)
                    requests.get(i).setStatus(ParticipationRequestStatus.REJECTED);
        } else throw new OperationConditionsFailureException(
                "Некорректный статус в заявке на изменение статусов запросов на участие"
        );
        requestStorage.saveAll(requests);
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        for (ParticipationRequest request : requests) {
            if (request.getStatus().equals(ParticipationRequestStatus.CONFIRMED)) {
                confirmedRequests.add(participationRequestToDto(request));
            } else {
                rejectedRequests.add(participationRequestToDto(request));
            }
        }
        log.info(
                "Инициатором с id=" + initiatorId + " события с id=" + eventId +
                        " обновлены заявки на участие:" + requests
        );
        return new ParticipationRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getParticipationRequestsByRequester(Integer requesterId) {
        if (!userRepository.existsById(requesterId))
            throw new UserNotFoundException("Пользователь с id=" + requesterId + " не найден");
        log.info("Пользователем с id=" + requesterId + " запрошены его заявки на участие");
        return requestStorage.findByRequesterId(requesterId).stream()
                .map(ParticipationRequestMapper::participationRequestToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto addParticipationRequest(Integer requesterId, Integer eventId) {
        if (requestStorage.existsByRequesterIdAndEventId(requesterId, eventId))
            throw new ParticipationRequestAlreadyExistsException(
                    "Запрос на участие пользователя с id=" + requesterId +
                            " в событии с id=" + eventId + " уже существует"
            );
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие с id=" + eventId + " не найдено"));
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id=" + requesterId + " не найден"));
        if (event.getInitiator().getId().equals(requesterId))
            throw new OperationConditionsFailureException(
                    "Пользователь с id=" + requesterId +
                            " отправил запрос на участие в собственном событии с id=" + eventId
            );
        if (!event.getState().equals(EventState.PUBLISHED))
            throw new OperationConditionsFailureException(
                    "Пользователь с id=" + requesterId +
                            " отправил запрос на участие в неопубликованном событии с id=" + eventId
            );
        int limit = event.getParticipantLimit();
        boolean hasReachedLimit =
                (limit > 0) &&
                        requestStorage
                                .countByStatusAndEventId(ParticipationRequestStatus.CONFIRMED, eventId)
                                .equals(limit);
        if (hasReachedLimit)
            throw new OperationConditionsFailureException(
                    "Пользователь с id=" + requesterId +
                            " отправил запрос на участие в событии с id=" + eventId + " с достигнутым лимитом запросов"
            );
        ParticipationRequestStatus status = (event.isRequestModeration() && limit > 0)
                ? ParticipationRequestStatus.PENDING : ParticipationRequestStatus.CONFIRMED;
        ParticipationRequest request = requestStorage.save(
                new ParticipationRequest(
                        null,
                        LocalDateTime.now(),
                        event,
                        user,
                        status
                )
        );
        log.info("Добавлен запрос на участие: " + request);
        return participationRequestToDto(request);
    }

    @Override
    public ParticipationRequestDto cancelParticipationRequest(Integer userId, Integer requestId) {
        ParticipationRequest request = requestStorage.findById(requestId)
                .orElseThrow(
                        () -> new ParticipationRequestNotFoundException(
                                "Запрос на участие с id=" + requestId + " не найден"
                        )
                );
        if (!request.getRequester().getId().equals(userId))
            throw new OperationConditionsFailureException(
                    "Запрос на участие с id=" + requestId + " не принадлежит пользователю с id=" + userId
            );
        request.setStatus(ParticipationRequestStatus.CANCELED);
        log.info("Пользователь с id=" + userId + " отменил заявку на участие с id=" + requestId);
        return participationRequestToDto(requestStorage.save(request));
    }
}