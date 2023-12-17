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
import ru.practicum.ewm.category.storage.CategoryRepository;
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
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.exception.CategoryNotFoundException;
import ru.practicum.ewm.exception.EventDateException;
import ru.practicum.ewm.exception.EventNotFoundException;
import ru.practicum.ewm.exception.OperationConditionsFailureException;
import ru.practicum.ewm.exception.StatsServiceException;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.participationrequest.model.ParticipationRequestStatus;
import ru.practicum.ewm.participationrequest.storage.ParticipationRequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.event.model.dto.EventMapper.eventFromNewEventDto;
import static ru.practicum.ewm.event.model.dto.EventMapper.eventToFullDto;
import static ru.practicum.ewm.event.model.dto.EventMapper.eventToShortDto;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository requestStorage;
    private final StatsClient statsClient;

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Integer eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException("Событие с id=" + eventId + " не найдено"));
        log.info("Запрошено событие {}", event);
        saveHit(request);
        return eventToFullDto(event, getConfirmed(event), getViews(event));
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
        saveHit(request);
        return eventRepository.findEventsPublic(
                        text,
                        categories,
                        paid,
                        rangeStart,
                        rangeEnd,
                        onlyAvailable,
                        pageable
                ).stream()
                .map(
                        event -> eventToShortDto(
                                event,
                                getConfirmed(event),
                                getViews(event)
                        )
                )
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto addEvent(Integer userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2)))
            throw new EventDateException(
                    "Время начала события не может быть раньше, чем через два часа от текущего момента"
            );
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id=" + userId + " не найден"));
        int catId = newEventDto.getCategory();
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException("Категория с id=" + catId + " не найдена"));
        Event eventFromDto = eventFromNewEventDto(newEventDto, user, category);
        Event event = eventRepository.save(eventFromDto);
        log.info("Добавлено событие {}", event);
        return eventToFullDto(event, 0, 0L);
    }

    @Override
    public EventFullDto updateEventByInitiator(
            Integer initiatorId,
            Integer eventId,
            UpdateEventUserRequest updateRequest
    ) {
        if (!userRepository.existsById(initiatorId))
            throw new UserNotFoundException("Пользователь с id=" + initiatorId + " не найден");
        Event event = eventRepository.findByIdAndInitiatorId(eventId, initiatorId)
                .orElseThrow(
                        () -> new EventNotFoundException(
                                "Событие с id=" + eventId + ", созданное пользователем с id=" +
                                        initiatorId + ", не найдено"
                        )
                );
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
        return eventToFullDto(eventRepository.save(event), 0, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByInitiatorId(Integer initiatorId, Integer from, Integer size) {
        if (!userRepository.existsById(initiatorId))
            throw new UserNotFoundException("Пользователь с id=" + initiatorId + " не найден");
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate"));
        log.info("Запрошен список событий, добавленных пользователем с id=" + initiatorId);
        return eventRepository.findByInitiatorId(initiatorId, pageable).stream()
                .map(
                        event -> eventToShortDto(
                                event,
                                getConfirmed(event),
                                getViews(event)
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByIdAndInitiatorId(Integer initiatorId, Integer eventId) {
        if (!userRepository.existsById(initiatorId))
            throw new UserNotFoundException("Пользователь с id=" + initiatorId + " не найден");
        Event event = eventRepository.findByIdAndInitiatorId(eventId, initiatorId)
                .orElseThrow(
                        () -> new EventNotFoundException(
                                "Событие с id=" + eventId + ", созданное пользователем с id=" +
                                        initiatorId + ", не найдено"
                        )
                );
        log.info("Инициатором запрошено событие {}", event);
        return eventToFullDto(event, getConfirmed(event), getViews(event));
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
        return eventRepository.findEventsAdmin(
                        users,
                        states,
                        categories,
                        rangeStart,
                        rangeEnd,
                        pageable
                ).stream()
                .map(
                        event -> eventToFullDto(
                                event,
                                getConfirmed(event),
                                getViews(event)
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventByAdmin(Integer eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие с id=" + eventId + " не найдено"));
        EventState eventState = event.getState();
        if (!eventState.equals(EventState.PENDING))
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
        return eventToFullDto(eventRepository.save(event), 0, 0L);
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
            Category category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new CategoryNotFoundException("Категория с id=" + catId + " не найдена"));
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

    private long getViews(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) return 0;
        ResponseEntity<Object> response = statsClient.getStats(
                event.getPublishedOn(),
                LocalDateTime.now(),
                List.of(String.format("/events/%s", event.getId())),
                true
        );
        ObjectMapper mapper = new ObjectMapper();
        List<ViewStatsDto> statsDto = mapper.convertValue(response.getBody(), new TypeReference<>() {});
        return (statsDto.isEmpty()) ? 0 : statsDto.get(0).getHits();
    }

    private int getConfirmed(Event event) {
        return (event.getState().equals(EventState.PUBLISHED)) ?
                requestStorage.countByStatusAndEventId(ParticipationRequestStatus.CONFIRMED, event.getId()) : 0;
    }
}