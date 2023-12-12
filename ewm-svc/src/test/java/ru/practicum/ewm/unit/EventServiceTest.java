package ru.practicum.ewm.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.storage.CategoryStorage;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.dto.EndpointHitDto;
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
import ru.practicum.ewm.event.service.EventServiceImpl;
import ru.practicum.ewm.event.storage.EventStorage;
import ru.practicum.ewm.exception.CategoryNotFoundException;
import ru.practicum.ewm.exception.EventDateException;
import ru.practicum.ewm.exception.EventNotFoundException;
import ru.practicum.ewm.exception.OperationConditionsFailureException;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.participationrequest.model.ParticipationRequestStatus;
import ru.practicum.ewm.participationrequest.storage.ParticipationRequestStorage;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static ru.practicum.ewm.event.model.dto.EventMapper.eventToFullDto;
import static ru.practicum.ewm.event.model.dto.EventMapper.eventToShortDto;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @Mock
    private EventStorage eventStorage;
    @Mock
    private CategoryStorage categoryStorage;
    @Mock
    private UserStorage userStorage;
    @Mock
    private ParticipationRequestStorage requestStorage;
    @Mock
    private StatsClient statsClient;
    @InjectMocks
    private EventServiceImpl eventService;

    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "events/1");
    private final User user = new User(1, "userm@email.com", "name");
    private final Category category = new Category(1, "name");
    private Event event;
    private NewEventDto newEventDto;
    private UpdateEventUserRequest updateUserRequest;
    private UpdateEventAdminRequest updateAdminRequest;

    @BeforeEach
    public void setEvent() {
        event = new Event(
                1,
                user,
                "a".repeat(21),
                category,
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                0.0,
                0.0,
                false,
                0,
                false,
                "title",
                LocalDateTime.now().minusDays(1),
                EventState.PUBLISHED,
                LocalDateTime.now().minusHours(1),
                null
        );
        newEventDto = new NewEventDto(
                event.getAnnotation(),
                event.getCategory().getId(),
                event.getDescription(),
                event.getEventDate(),
                new Location(event.getLat(), event.getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getTitle()
        );
        updateUserRequest = new UpdateEventUserRequest(
                event.getAnnotation(),
                event.getCategory().getId(),
                event.getDescription(),
                event.getEventDate(),
                new Location(event.getLat(), event.getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getTitle(),
                ActionState.SEND_TO_REVIEW
        );
        updateAdminRequest = new UpdateEventAdminRequest(
                event.getAnnotation(),
                event.getCategory().getId(),
                event.getDescription(),
                event.getEventDate(),
                new Location(event.getLat(), event.getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getTitle(),
                ActionState.PUBLISH_EVENT
        );
    }

    @Test
    public void shouldGetEventById() {
        request.setRemoteAddr("127.0.0.1");
        Mockito
                .when(eventStorage.findByIdAndState(anyInt(), any(EventState.class)))
                .thenReturn(Optional.of(event));
        Mockito
                .when(requestStorage.countByStatusAndEventId(any(ParticipationRequestStatus.class), anyInt()))
                .thenReturn(0);
        Mockito
                .when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().body(Collections.emptyList()));
        Mockito
                .when(statsClient.addHit(any(EndpointHitDto.class)))
                .thenReturn(ResponseEntity.ok().build());
        EventFullDto eventFullDtoFromService = eventService.getEventById(1, request);
        EventFullDto eventFullDto = eventToFullDto(event, 0, 0);

        assertThat(eventFullDto, equalTo(eventFullDtoFromService));
    }

    @Test
    public void shouldNotGetEventByIdWhenEventNotFound() {
        Mockito
                .when(eventStorage.findByIdAndState(anyInt(), any(EventState.class)))
                .thenReturn(Optional.empty());

        EventNotFoundException e = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventService.getEventById(1, request)
        );

        assertThat(e.getMessage(), equalTo("Событие с id=1 не найдено"));
    }

    @Test
    public void shouldGetEvents() {
        request.setRemoteAddr("127.0.0.1");
        Mockito
                .when(eventStorage.findEventsPublic(anyString(), anyList(), anyBoolean(), any(LocalDateTime.class),
                        any(LocalDateTime.class), anyBoolean(), any(Pageable.class)))
                .thenReturn(List.of(event));
        Mockito
                .when(requestStorage.countByStatusAndEventId(any(ParticipationRequestStatus.class), anyInt()))
                .thenReturn(0);
        Mockito
                .when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().body(Collections.emptyList()));
        Mockito
                .when(statsClient.addHit(any(EndpointHitDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        List<EventShortDto> events =
                eventService.getEvents("text", List.of(1), false, LocalDateTime.now(),
                        LocalDateTime.now().plusDays(1), true, SortType.VIEWS, 0, 10, request);

        assertThat(events.size(), equalTo(1));

        EventShortDto eventShortDto = eventToShortDto(event, 0, 0);
        EventShortDto eventShortDtoFromService = events.get(0);

        assertThat(eventShortDto, equalTo(eventShortDtoFromService));
    }

    @Test
    public void shouldNotGetEventsWhenIncorrectDateInterval() {
        EventDateException e = Assertions.assertThrows(
                EventDateException.class,
                () -> eventService.getEvents("text", List.of(1), false, LocalDateTime.now(),
                        LocalDateTime.now().minusDays(1), true, SortType.VIEWS, 0, 10, request)
        );

        assertThat(e.getMessage(), equalTo("Указан некорректный диапазон дат"));
    }

    @Test
    public void shouldAddEvent() {
        Mockito
                .when(userStorage.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.of(category));
        Mockito
                .when(eventStorage.save(any(Event.class)))
                .thenReturn(event);

        EventFullDto eventFullDtoFromService = eventService.addEvent(1, newEventDto);
        EventFullDto eventFullDto = eventToFullDto(event, 0, 0);

        assertThat(eventFullDto, equalTo(eventFullDtoFromService));
    }

    @Test
    public void shouldNotAddEventWhenIncorrectEventDate() {
        NewEventDto newIncorrectEventDto = new NewEventDto(
                event.getAnnotation(),
                event.getCategory().getId(),
                event.getDescription(),
                LocalDateTime.now(),
                new Location(event.getLat(), event.getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getTitle()
        );

        EventDateException e = Assertions.assertThrows(
                EventDateException.class,
                () -> eventService.addEvent(1, newIncorrectEventDto)
        );

        assertThat(
                e.getMessage(),
                equalTo("Время начала события не может быть раньше, чем через два часа от текущего момента")
        );
    }

    @Test
    public void shouldNotAddEventWhenUserNotFound() {
        Mockito
                .when(userStorage.findById(anyInt()))
                .thenReturn(Optional.empty());
        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> eventService.addEvent(1, newEventDto)
        );

        assertThat(
                e.getMessage(),
                equalTo("Пользователь с id=1 не найден")
        );
    }

    @Test
    public void shouldNotAddEventWhenCategoryNotFound() {
        Mockito
                .when(userStorage.findById(anyInt()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.empty());
        CategoryNotFoundException e = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> eventService.addEvent(1, newEventDto)
        );

        assertThat(
                e.getMessage(),
                equalTo("Категория с id=1 не найдена")
        );
    }

    @Test
    public void shouldUpdateEventByInitiator() {
        event.setState(EventState.PENDING);
        event.setPublishedOn(null);
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findByIdAndInitiatorId(anyInt(), anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.of(category));
        Mockito
                .when(eventStorage.save(any(Event.class)))
                .then(returnsFirstArg());

        EventFullDto eventFullDtoFromService = eventService.updateEventByInitiator(1, 1, updateUserRequest);
        EventFullDto eventFullDto = eventToFullDto(event, 0, 0);

        assertThat(eventFullDto, equalTo(eventFullDtoFromService));
    }

    @Test
    public void shouldNotUpdateEventByInitiatorWhenUserNotFound() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(false);

        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> eventService.updateEventByInitiator(1, 1, updateUserRequest)
        );

        assertThat(
                e.getMessage(),
                equalTo("Пользователь с id=1 не найден")
        );
    }

    @Test
    public void shouldNotUpdateEventByInitiatorWhenEventNotFound() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findByIdAndInitiatorId(anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        EventNotFoundException e = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventService.updateEventByInitiator(1, 1, updateUserRequest)
        );

        assertThat(e.getMessage(), equalTo("Событие с id=1, созданное пользователем с id=1, не найдено"));
    }

    @Test
    public void shouldNotUpdateEventByInitiatorWhenEventPublished() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findByIdAndInitiatorId(anyInt(), anyInt()))
                .thenReturn(Optional.of(event));

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> eventService.updateEventByInitiator(1, 1, updateUserRequest)
        );

        assertThat(e.getMessage(), equalTo("Изменение опубликованного события невозможно"));
    }

    @Test
    public void shouldNotUpdateEventByInitiatorWhenIncorrectEventDate() {
        updateUserRequest = new UpdateEventUserRequest(
                event.getAnnotation(),
                event.getCategory().getId(),
                event.getDescription(),
                LocalDateTime.now().minusDays(1),
                new Location(event.getLat(), event.getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getTitle(),
                ActionState.SEND_TO_REVIEW
        );
        event.setState(EventState.PENDING);
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findByIdAndInitiatorId(anyInt(), anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.of(category));

        EventDateException e = Assertions.assertThrows(
                EventDateException.class,
                () -> eventService.updateEventByInitiator(1, 1, updateUserRequest)
        );

        assertThat(
                e.getMessage(),
                equalTo("Время начала события не может быть раньше, чем через два часа от текущего момента")
        );
    }

    @Test
    public void shouldNotUpdateEventByInitiatorWhenIncorrectAction() {
        updateUserRequest = new UpdateEventUserRequest(
                event.getAnnotation(),
                event.getCategory().getId(),
                event.getDescription(),
                event.getEventDate(),
                new Location(event.getLat(), event.getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getTitle(),
                ActionState.PUBLISH_EVENT
        );
        event.setState(EventState.PENDING);
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findByIdAndInitiatorId(anyInt(), anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.of(category));

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> eventService.updateEventByInitiator(1, 1, updateUserRequest)
        );

        assertThat(e.getMessage(), equalTo("Данная операция недоступна для пользователя"));

    }

    @Test
    public void shouldNotUpdateEventByInitiatorWhenCategoryNotFound() {
        event.setState(EventState.PENDING);
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findByIdAndInitiatorId(anyInt(), anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.empty());

        CategoryNotFoundException e = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> eventService.updateEventByInitiator(1, 1, updateUserRequest)
        );

        assertThat(
                e.getMessage(),
                equalTo("Категория с id=1 не найдена")
        );
    }

    @Test
    public void shouldGetEventsByInitiatorId() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findByInitiatorId(anyInt(), any(Pageable.class)))
                .thenReturn(List.of(event));
        Mockito
                .when(requestStorage.countByStatusAndEventId(any(ParticipationRequestStatus.class), anyInt()))
                .thenReturn(0);
        Mockito
                .when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().body(Collections.emptyList()));

        List<EventShortDto> events = eventService.getEventsByInitiatorId(1, 0, 10);

        assertThat(events.size(), equalTo(1));

        EventShortDto eventShortDto = eventToShortDto(event, 0, 0);
        EventShortDto eventShortDtoFromService = events.get(0);

        assertThat(eventShortDto, equalTo(eventShortDtoFromService));
    }

    @Test
    public void shouldNotGetEventsByInitiatorIdWhenUserNotFound() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(false);

        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> eventService.getEventsByInitiatorId(1, 0, 10)
        );

        assertThat(
                e.getMessage(),
                equalTo("Пользователь с id=1 не найден")
        );
    }

    @Test
    public void shouldGetEventByIdAndInitiatorId() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findByIdAndInitiatorId(anyInt(), anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(requestStorage.countByStatusAndEventId(any(ParticipationRequestStatus.class), anyInt()))
                .thenReturn(0);
        Mockito
                .when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().body(Collections.emptyList()));

        EventFullDto eventFullDtoFromService = eventService.getEventByIdAndInitiatorId(1, 1);
        EventFullDto eventFullDto = eventToFullDto(event, 0, 0);

        assertThat(eventFullDto, equalTo(eventFullDtoFromService));
    }

    @Test
    public void shouldNotGetEventByIdAndInitiatorIdWhenUserNotFound() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(false);

        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> eventService.getEventByIdAndInitiatorId(1, 1)
        );

        assertThat(
                e.getMessage(),
                equalTo("Пользователь с id=1 не найден")
        );
    }

    @Test
    public void shouldNotGetEventByIdAndInitiatorIdWhenEventNotFound() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findByIdAndInitiatorId(anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        EventNotFoundException e = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventService.getEventByIdAndInitiatorId(1, 1)
        );

        assertThat(e.getMessage(), equalTo("Событие с id=1, созданное пользователем с id=1, не найдено"));
    }

    @Test
    public void shouldGetEventsByAdmin() {
        Mockito
                .when(eventStorage.findEventsAdmin(anyList(), anyList(), anyList(),
                        any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(event));
        Mockito
                .when(requestStorage.countByStatusAndEventId(any(ParticipationRequestStatus.class), anyInt()))
                .thenReturn(0);
        Mockito
                .when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().body(Collections.emptyList()));

        List<EventFullDto> events =
                eventService.getEventsByAdmin(List.of(1), List.of(EventState.PUBLISHED), List.of(1),
                        LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), 0, 10);

        assertThat(events.size(), equalTo(1));

        EventFullDto eventFullDto = eventToFullDto(event, 0, 0);
        EventFullDto eventFullDtoFromService = events.get(0);

        assertThat(eventFullDto, equalTo(eventFullDtoFromService));
    }

    @Test
    public void shouldNotGetEventsByAdminWhenIncorrectDateInterval() {
        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> eventService.getEventsByAdmin(null, null, null,
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1), 0, 10)
        );

        assertThat(
                e.getMessage(),
                equalTo("Указан некорректный диапазон дат")
        );
    }

    @Test
    public void shouldUpdateEventByAdmin() {
        event.setState(EventState.PENDING);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.of(category));
        Mockito
                .when(eventStorage.save(any(Event.class)))
                .then(returnsFirstArg());

        EventFullDto eventFullDtoFromService = eventService.updateEventByAdmin(1, updateAdminRequest);
        EventFullDto eventFullDto = eventToFullDto(event, 0, 0);

        assertThat(eventFullDto, equalTo(eventFullDtoFromService));
    }

    @Test
    public void shouldNotUpdateEventByAdminWhenEventNotFound() {
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.empty());

        EventNotFoundException e = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> eventService.updateEventByAdmin(1, updateAdminRequest)
        );

        assertThat(
                e.getMessage(),
                equalTo("Событие с id=1 не найдено")
        );
    }

    @Test
    public void shouldNotUpdateEventByAdminWhenStateNotPending() {
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> eventService.updateEventByAdmin(1, updateAdminRequest)
        );

        assertThat(
                e.getMessage(),
                equalTo("Изменение события с id=1 невозможно")
        );
    }

    @Test
    public void shouldNotUpdateEventByAdminWhenIncorrectEventDate() {
        event.setState(EventState.PENDING);
        updateAdminRequest = new UpdateEventAdminRequest(
                event.getAnnotation(),
                event.getCategory().getId(),
                event.getDescription(),
                LocalDateTime.now(),
                new Location(event.getLat(), event.getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getTitle(),
                ActionState.PUBLISH_EVENT
        );
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.of(category));

        EventDateException e = Assertions.assertThrows(
                EventDateException.class,
                () -> eventService.updateEventByAdmin(1, updateAdminRequest)
        );

        assertThat(
                e.getMessage(),
                equalTo("Время начала события не может быть раньше, чем через час от текущего момента")
        );
    }

    @Test
    public void shouldNotUpdateEventByAdminWhenIncorrectAction() {
        event.setState(EventState.PENDING);
        updateAdminRequest = new UpdateEventAdminRequest(
                event.getAnnotation(),
                event.getCategory().getId(),
                event.getDescription(),
                event.getEventDate(),
                new Location(event.getLat(), event.getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getTitle(),
                ActionState.SEND_TO_REVIEW
        );
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.of(category));

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> eventService.updateEventByAdmin(1, updateAdminRequest)
        );

        assertThat(
                e.getMessage(),
                equalTo("Данная операция недоступна для администратора")
        );
    }

    @Test
    public void shouldNotUpdateEventByAdminWhenCategoryNotFound() {
        event.setState(EventState.PENDING);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(categoryStorage.findById(anyInt()))
                .thenReturn(Optional.empty());

        CategoryNotFoundException e = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> eventService.updateEventByAdmin(1, updateAdminRequest)
        );

        assertThat(
                e.getMessage(),
                equalTo("Категория с id=1 не найдена")
        );
    }
}