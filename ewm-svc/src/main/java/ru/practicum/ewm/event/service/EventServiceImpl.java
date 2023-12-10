package ru.practicum.ewm.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.storage.CategoryStorage;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.event.model.ActionState;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.model.SortType;
import ru.practicum.ewm.event.model.dto.EventFullDto;
import ru.practicum.ewm.event.model.dto.EventShortDto;
import ru.practicum.ewm.event.model.dto.NewEventDto;
import ru.practicum.ewm.event.model.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.model.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.storage.EventStorage;
import ru.practicum.ewm.exception.CategoryNotFoundException;
import ru.practicum.ewm.exception.EventDateException;
import ru.practicum.ewm.exception.EventNotFoundException;
import ru.practicum.ewm.exception.OperationConditionsFailureException;
import ru.practicum.ewm.exception.StatsServiceException;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.participationrequest.model.ParticipationRequestStatus;
import ru.practicum.ewm.participationrequest.storage.ParticipationRequestStorage;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserStorage;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewm.event.model.dto.EventMapper.eventFromNewEventDto;
import static ru.practicum.ewm.event.model.dto.EventMapper.eventToFullDto;
import static ru.practicum.ewm.event.model.dto.EventMapper.eventToShortDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventStorage eventStorage;
    private final CategoryStorage categoryStorage;
    private final UserStorage userStorage;
    private final ParticipationRequestStorage requestStorage;
    private final StatsClient statsClient;

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Integer eventId, HttpServletRequest request) {
        Optional<Event> eventOptional = eventStorage.findByIdAndState(eventId, EventState.PUBLISHED);
        if (eventOptional.isEmpty()) throw new EventNotFoundException("Событие с id=" + eventId + " не найдено");
        Event event = eventOptional.get();
        log.info("Запрошено событие {}", event);
        long hits = getHitsForSingleEvent(event);
        int confirmedRequests = requestStorage.countByStatusAndEventId(ParticipationRequestStatus.CONFIRMED, eventId);
        saveHit(request);
        return eventToFullDto(event, confirmedRequests, hits);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(
            String text,
            List<Integer> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            SortType sortType,
            Integer from,
            Integer size,
            HttpServletRequest request
    ) {
        if (rangeStart == null && rangeEnd == null)
            rangeStart = LocalDateTime.now();
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd))
            throw new EventDateException("Указан некорректный диапазон дат");
        Comparator<EventShortDto> comparator;
        if (sortType.equals(SortType.EVENT_DATE))
            comparator = Comparator.comparing(EventShortDto::getEventDate);
        else comparator = Comparator.comparing(EventShortDto::getViews).reversed();
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        log.info("Запрошен публичный список событий");
        List<Event> events = eventStorage.findEventsPublic(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                pageable
        );
        if (events.isEmpty()) return Collections.emptyList();
        Map<Integer, Long> hits = getHits(events);
        saveHit(request);
        return events.stream()
                .map(
                        event -> {
                            int id = event.getId();
                            return eventToShortDto(
                                    event,
                                    requestStorage.countByStatusAndEventId(
                                            ParticipationRequestStatus.CONFIRMED,
                                            id
                                    ),
                                    hits.getOrDefault(id, 0L)
                            );
                        }
                )
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Integer userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2)))
            throw new EventDateException(
                    "Время начала события не может быть раньше, чем через два часа от текущего момента"
            );
        Optional<User> userOptional = userStorage.findById(userId);
        if (userOptional.isEmpty()) throw new UserNotFoundException("Пользователь с id=" + userId + " не найден");
        User user = userOptional.get();
        int catId = newEventDto.getCategory();
        Optional<Category> categoryOptional = categoryStorage.findById(catId);
        if (categoryOptional.isEmpty()) throw new CategoryNotFoundException("Категория с id=" + catId + " не найдена");
        Category category = categoryOptional.get();
        Event eventFromDto = eventFromNewEventDto(newEventDto, user, category);
        if (eventFromDto.getPaid() == null) eventFromDto.setPaid(false);
        if (eventFromDto.getParticipantLimit() == null) eventFromDto.setParticipantLimit(0);
        if (eventFromDto.getRequestModeration() == null) eventFromDto.setRequestModeration(true);
        Event event = eventStorage.save(eventFromDto);
        log.info("Добавлено событие {}", event);
        return eventToFullDto(event, 0, 0L);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByInitiator(
            Integer initiatorId,
            Integer eventId,
            UpdateEventUserRequest updateRequest
    ) {
        if (!userStorage.existsById(initiatorId))
            throw new UserNotFoundException("Пользователь с id=" + initiatorId + " не найден");
        Optional<Event> eventOptional = eventStorage.findByIdAndInitiatorId(eventId, initiatorId);
        if (eventOptional.isEmpty())
            throw new EventNotFoundException(
                    "Событие с id=" + eventId + ", созданное пользователем с id=" + initiatorId + ", не найдено"
            );
        Event event = eventOptional.get();
        if (event.getState().equals(EventState.PUBLISHED))
            throw new OperationConditionsFailureException("Изменение опубликованного события невозможно");
        updateEvent(
                event,
                updateRequest.getAnnotation(),
                updateRequest.getCategory(),
                updateRequest.getDescription(),
                updateRequest.getLocation(),
                updateRequest.getPaid(),
                updateRequest.getParticipantLimit(),
                updateRequest.getRequestModeration(),
                updateRequest.getTitle()
        );
        LocalDateTime eventDate = updateRequest.getEventDate();
        if (eventDate != null) {
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2)))
                throw new EventDateException(
                        "Время начала события не может быть раньше, чем через два часа от текущего момента"
                );
            event.setEventDate(eventDate);
        }
        ActionState state = updateRequest.getStateAction();
        if (state != null) {
            switch (state) {
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                default:
                    throw new OperationConditionsFailureException(
                            "Данная операция недоступна для пользователя"
                    );
            }
        }
        log.info("Инициатором обновлено событие {}", event);
        return eventToFullDto(eventStorage.save(event), 0, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByInitiatorId(Integer initiatorId, Integer from, Integer size) {
        if (!userStorage.existsById(initiatorId))
            throw new UserNotFoundException("Пользователь с id=" + initiatorId + " не найден");
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate"));
        log.info("Запрошен список событий, добавленных пользователем с id=" + initiatorId);
        List<Event> events = eventStorage.findByInitiatorId(initiatorId, pageable);
        if (events.isEmpty()) return Collections.emptyList();
        Map<Integer, Long> hits = getHits(events);
        return events.stream()
                .map(
                        event -> {
                            if (event.getState().equals(EventState.PUBLISHED)) {
                                int id = event.getId();
                                return eventToShortDto(
                                        event,
                                        requestStorage.countByStatusAndEventId(
                                                ParticipationRequestStatus.CONFIRMED,
                                                id
                                        ),
                                        hits.getOrDefault(id, 0L)
                                );
                            } else {
                                return eventToShortDto(event, 0, 0L);
                            }
                        }
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByIdAndInitiatorId(Integer initiatorId, Integer eventId) {
        if (!userStorage.existsById(initiatorId))
            throw new UserNotFoundException("Пользователь с id=" + initiatorId + " не найден");
        Optional<Event> eventOptional = eventStorage.findByIdAndInitiatorId(eventId, initiatorId);
        if (eventOptional.isEmpty())
            throw new EventNotFoundException(
                    "Событие с id=" + eventId + ", созданное пользователем с id=" + initiatorId + ", не найдено"
            );
        Event event = eventOptional.get();
        log.info("Инициатором запрошено событие {}", event);
        long hits = getHitsForSingleEvent(event);
        int confirmedRequests = (event.getState().equals(EventState.PUBLISHED)) ?
                requestStorage.countByStatusAndEventId(ParticipationRequestStatus.CONFIRMED, eventId) : 0;
        return eventToFullDto(event, confirmedRequests, hits);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsByAdmin(
            List<Integer> users,
            List<EventState> states,
            List<Integer> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size
    ) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate"));
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd))
            throw new OperationConditionsFailureException("Указан некорректный диапазон дат");
        log.info("Запрошен список событий для администратора");
        List<Event> events = eventStorage.findEventsAdmin(
                users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                pageable
        );
        if (events.isEmpty()) return Collections.emptyList();
        Map<Integer, Long> hits = getHits(events);
        return events.stream()
                .map(
                        event -> {
                            if (event.getState().equals(EventState.PUBLISHED)) {
                                int id = event.getId();
                                return eventToFullDto(
                                        event,
                                        requestStorage.countByStatusAndEventId(
                                                ParticipationRequestStatus.CONFIRMED,
                                                id
                                        ),
                                        hits.getOrDefault(id, 0L)
                                );
                            } else {
                                return eventToFullDto(event, 0, 0L);
                            }
                        }
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Integer eventId, UpdateEventAdminRequest updateRequest) {
        Optional<Event> eventOptional = eventStorage.findById(eventId);
        if (eventOptional.isEmpty())
            throw new EventNotFoundException(
                    "Событие с id=" + eventId + " не найдено"
            );
        Event event = eventOptional.get();
        EventState eventState = event.getState();
        if (eventState.equals(EventState.PUBLISHED) || eventState.equals(EventState.CANCELED))
            throw new OperationConditionsFailureException("Изменение события с id=" + eventId + " невозможно");
        updateEvent(
                event,
                updateRequest.getAnnotation(),
                updateRequest.getCategory(),
                updateRequest.getDescription(),
                updateRequest.getLocation(),
                updateRequest.getPaid(),
                updateRequest.getParticipantLimit(),
                updateRequest.getRequestModeration(),
                updateRequest.getTitle()
        );
        LocalDateTime eventDate = updateRequest.getEventDate();
        if (eventDate != null) {
            if (eventDate.isBefore(LocalDateTime.now().plusHours(1)))
                throw new EventDateException(
                        "Время начала события не может быть раньше, чем через час от текущего момента"
                );
            event.setEventDate(eventDate);
        }
        ActionState state = updateRequest.getStateAction();
        if (state != null) {
            switch (state) {
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                default:
                    throw new OperationConditionsFailureException(
                            "Данная операция недоступна для администратора"
                    );
            }
        }
        log.info("Администратором обновлено событие {}", event);
        return eventToFullDto(eventStorage.save(event), 0, 0L);
    }

    private void updateEvent(
            Event event,
            String annotation,
            Integer catId,
            String description,
            Location location,
            Boolean paid,
            Integer participantLimit,
            Boolean requestModeration,
            String title
    ) {
        if (annotation != null) event.setAnnotation(annotation);
        if (catId != null) {
            Optional<Category> categoryOptional = categoryStorage.findById(catId);
            if (categoryOptional.isEmpty())
                throw new CategoryNotFoundException("Категория с id=" + catId + " не найдена");
            Category category = categoryOptional.get();
            event.setCategory(category);
        }
        if (description != null) event.setDescription(description);
        if (location != null) {
            event.setLat(location.getLat());
            event.setLon(location.getLon());
        }
        if (paid != null) event.setPaid(paid);
        if (participantLimit != null) event.setParticipantLimit(participantLimit);
        if (requestModeration != null) event.setRequestModeration(requestModeration);
        if (title != null) event.setTitle(title);
    }

    private void saveHit(HttpServletRequest request) {
        EndpointHitDto hit = new EndpointHitDto(
                "ewm-svc",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        ResponseEntity<Object> response = statsClient.addHit(hit);
        if (!response.getStatusCode().is2xxSuccessful())
            throw new StatsServiceException("Возникла проблема при сохранении записи в сервис статистики");
    }

    private Map<Integer, Long> getHits(List<Event> events) {
        Optional<LocalDateTime> earliestPublishedOptional = events.stream()
                .filter(event -> event.getState().equals(EventState.PUBLISHED))
                .map(Event::getPublishedOn)
                .min(LocalDateTime::compareTo);
        if (earliestPublishedOptional.isEmpty()) return new HashMap<>();
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());
        ResponseEntity<Object> response = statsClient.getStats(
                earliestPublishedOptional.get(),
                LocalDateTime.now(),
                uris,
                true
        );
        ObjectMapper mapper = new ObjectMapper();
        List<ViewStatsDto> statsDto = mapper.convertValue(response.getBody(), new TypeReference<>() {});
        Map<Integer, Long> hits = new HashMap<>();
        for (ViewStatsDto stat : statsDto)
            hits.put(Integer.parseInt(stat.getUri().substring(8)), stat.getHits());
        return hits;
    }

    private Long getHitsForSingleEvent(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) return 0L;
        ResponseEntity<Object> response = statsClient.getStats(
                event.getPublishedOn(),
                LocalDateTime.now(),
                List.of(String.format("/events/%s", event.getId())),
                true
        );
        ObjectMapper mapper = new ObjectMapper();
        List<ViewStatsDto> statsDto = mapper.convertValue(response.getBody(), new TypeReference<>() {});
        return (statsDto.isEmpty()) ? 0L : statsDto.get(0).getHits();
    }
}