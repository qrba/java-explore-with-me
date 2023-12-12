package ru.practicum.ewm.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.storage.EventStorage;
import ru.practicum.ewm.exception.EventNotFoundException;
import ru.practicum.ewm.exception.OperationConditionsFailureException;
import ru.practicum.ewm.exception.ParticipationRequestAlreadyExistsException;
import ru.practicum.ewm.exception.ParticipationRequestNotFoundException;
import ru.practicum.ewm.exception.UserNotFoundException;
import ru.practicum.ewm.participationrequest.model.ParticipationRequest;
import ru.practicum.ewm.participationrequest.model.ParticipationRequestStatus;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestDto;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestStatusUpdateRequest;
import ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestStatusUpdateResult;
import ru.practicum.ewm.participationrequest.service.ParticipationRequestServiceImpl;
import ru.practicum.ewm.participationrequest.storage.ParticipationRequestStorage;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static ru.practicum.ewm.participationrequest.model.dto.ParticipationRequestMapper.participationRequestToDto;

@ExtendWith(MockitoExtension.class)
public class ParticipationRequestServiceTest {
    @Mock
    private ParticipationRequestStorage requestStorage;
    @Mock
    private UserStorage userStorage;
    @Mock
    private EventStorage eventStorage;
    @InjectMocks
    private ParticipationRequestServiceImpl requestService;

    private User initiator;
    private User requester;
    private Event event;
    private ParticipationRequest request;
    private ParticipationRequestStatusUpdateRequest updateRequest;

    @BeforeEach
    public void setUp() {
        initiator = new User(1, "initiator@email.com", "name");
        requester = new User(2, "requester@email.com", "name");
        event = new Event(
                1,
                initiator,
                "a".repeat(21),
                new Category(1, "name"),
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                0.0,
                0.0,
                false,
                10,
                true,
                "title",
                LocalDateTime.now().minusDays(1),
                EventState.PUBLISHED,
                LocalDateTime.now().minusHours(1),
                null
        );
        request = new ParticipationRequest(
                1,
                LocalDateTime.now().minusHours(1),
                event,
                requester,
                ParticipationRequestStatus.PENDING
        );
        updateRequest = new ParticipationRequestStatusUpdateRequest(
                List.of(request.getId()),
                ParticipationRequestStatus.CONFIRMED
        );
    }

    @Test
    public void shouldGetParticipationRequestsByEventInitiator() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(requestStorage.findByEventInitiatorId(anyInt()))
                .thenReturn(List.of(request));

        List<ParticipationRequestDto> requests =
                requestService.getParticipationRequestsByEventInitiator(initiator.getId(), event.getId());

        assertThat(requests.size(), equalTo(1));

        ParticipationRequestDto requestDto = participationRequestToDto(request);
        ParticipationRequestDto requestDtoFromService = requests.get(0);

        assertThat(requestDto, equalTo(requestDtoFromService));
    }

    @Test
    public void shouldNotGetParticipationRequestsByEventInitiatorWhenUserNotFound() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(false);

        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> requestService.getParticipationRequestsByEventInitiator(initiator.getId(), event.getId())
        );

        assertThat(
                e.getMessage(),
                equalTo("Пользователь с id=1 не найден")
        );
    }

    @Test
    public void shouldUpdateParticipationRequests() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(requestStorage.countByStatusAndEventId(any(ParticipationRequestStatus.class), anyInt()))
                .thenReturn(0);
        Mockito
                .when(
                        requestStorage.findByIdInAndStatusAndEventId(
                                anyList(),
                                any(ParticipationRequestStatus.class),
                                anyInt()
                        )
                )
                .thenReturn(List.of(request));

        ParticipationRequestStatusUpdateResult result =
                requestService.updateParticipationRequests(initiator.getId(), event.getId(), updateRequest);

        assertThat(result.getRejectedRequests().size(), equalTo(0));
        assertThat(result.getConfirmedRequests().size(), equalTo(1));

        ParticipationRequestDto requestDto = participationRequestToDto(request);
        ParticipationRequestDto requestDtoFromService = result.getConfirmedRequests().get(0);

        assertThat(requestDto, equalTo(requestDtoFromService));
    }

    @Test
    public void shouldNotUpdateParticipationRequestsWhenUserNotFound() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(false);

        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> requestService.updateParticipationRequests(initiator.getId(), event.getId(), updateRequest)
        );

        assertThat(
                e.getMessage(),
                equalTo("Пользователь с id=1 не найден")
        );
    }

    @Test
    public void shouldNotUpdateParticipationRequestsWhenEventNotFound() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.empty());

        EventNotFoundException e = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> requestService.updateParticipationRequests(initiator.getId(), event.getId(), updateRequest)
        );

        assertThat(e.getMessage(), equalTo("Событие с id=1 не найдено"));
    }

    @Test
    public void shouldNotUpdateParticipationRequestsWhenNotInitiator() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> requestService.updateParticipationRequests(requester.getId(), event.getId(), updateRequest)
        );

        assertThat(e.getMessage(), equalTo("Пользователь с id=2 не является инициатором события с id=1"));
    }

    @Test
    public void shouldNotUpdateParticipationRequestsWhenRequireModerationFalse() {
        event.setRequestModeration(false);
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> requestService.updateParticipationRequests(initiator.getId(), event.getId(), updateRequest)
        );

        assertThat(e.getMessage(), equalTo("Для события с id=1 одобрение заявок не требуется"));
    }

    @Test
    public void shouldNotUpdateParticipationRequestsWhenLimitReached() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(requestStorage.countByStatusAndEventId(any(ParticipationRequestStatus.class), anyInt()))
                .thenReturn(10);

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> requestService.updateParticipationRequests(initiator.getId(), event.getId(), updateRequest)
        );

        assertThat(e.getMessage(), equalTo("Достигнут лимит одобренных заявок для события с id=1"));
    }

    @Test
    public void shouldNotUpdateParticipationRequestsWhenIncorrectStatus() {
        ParticipationRequestStatusUpdateRequest incorrectUpdateRequest = new ParticipationRequestStatusUpdateRequest(
                List.of(request.getId()),
                ParticipationRequestStatus.CANCELED
        );
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(requestStorage.countByStatusAndEventId(any(ParticipationRequestStatus.class), anyInt()))
                .thenReturn(0);

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> requestService.updateParticipationRequests(
                        initiator.getId(),
                        event.getId(),
                        incorrectUpdateRequest
                )
        );

        assertThat(
                e.getMessage(),
                equalTo("Некорректный статус в заявке на изменение статусов запросов на участие")
        );
    }

    @Test
    public void shouldGetParticipationRequestsByRequester() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(true);
        Mockito
                .when(requestStorage.findByRequesterId(anyInt()))
                .thenReturn(List.of(request));

        List<ParticipationRequestDto> requests = requestService.getParticipationRequestsByRequester(requester.getId());

        assertThat(requests.size(), equalTo(1));

        ParticipationRequestDto requestDto = participationRequestToDto(request);
        ParticipationRequestDto requestDtoFromService = requests.get(0);

        assertThat(requestDto, equalTo(requestDtoFromService));
    }

    @Test
    public void shouldNotGetParticipationRequestsByRequesterWhenUserNotFound() {
        Mockito
                .when(userStorage.existsById(anyInt()))
                .thenReturn(false);

        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> requestService.getParticipationRequestsByRequester(requester.getId())
        );

        assertThat(
                e.getMessage(),
                equalTo("Пользователь с id=2 не найден")
        );
    }

    @Test
    public void shouldAddParticipationRequest() {
        Mockito
                .when(requestStorage.existsByRequesterIdAndEventId(anyInt(), anyInt()))
                .thenReturn(false);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(userStorage.findById(anyInt()))
                .thenReturn(Optional.of(requester));
        Mockito
                .when(requestStorage.countByStatusAndEventId(any(ParticipationRequestStatus.class), anyInt()))
                .thenReturn(0);
        Mockito
                .when(requestStorage.save(any(ParticipationRequest.class)))
                .thenReturn(request);

        ParticipationRequestDto requestDto = participationRequestToDto(request);
        ParticipationRequestDto requestDtoFromService =
                requestService.addParticipationRequest(requester.getId(), event.getId());

        assertThat(requestDto, equalTo(requestDtoFromService));
    }

    @Test
    public void shouldNotAddParticipationRequestWhenRequestExists() {
        Mockito
                .when(requestStorage.existsByRequesterIdAndEventId(anyInt(), anyInt()))
                .thenReturn(true);

        ParticipationRequestAlreadyExistsException e = Assertions.assertThrows(
                ParticipationRequestAlreadyExistsException.class,
                () -> requestService.addParticipationRequest(requester.getId(), event.getId())
        );

        assertThat(
                e.getMessage(),
                equalTo("Запрос на участие пользователя с id=2 в событии с id=1 уже существует")
        );
    }

    @Test
    public void shouldNotAddParticipationRequestWhenEventNotFound() {
        Mockito
                .when(requestStorage.existsByRequesterIdAndEventId(anyInt(), anyInt()))
                .thenReturn(false);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.empty());

        EventNotFoundException e = Assertions.assertThrows(
                EventNotFoundException.class,
                () -> requestService.addParticipationRequest(requester.getId(), event.getId())
        );

        assertThat(e.getMessage(), equalTo("Событие с id=1 не найдено"));
    }

    @Test
    public void shouldNotAddParticipationRequestWhenUserNotFound() {
        Mockito
                .when(requestStorage.existsByRequesterIdAndEventId(anyInt(), anyInt()))
                .thenReturn(false);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(userStorage.findById(anyInt()))
                .thenReturn(Optional.empty());

        UserNotFoundException e = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> requestService.addParticipationRequest(requester.getId(), event.getId())
        );

        assertThat(
                e.getMessage(),
                equalTo("Пользователь с id=2 не найден")
        );
    }

    @Test
    public void shouldNotAddParticipationRequestWhenInitiator() {
        Mockito
                .when(requestStorage.existsByRequesterIdAndEventId(anyInt(), anyInt()))
                .thenReturn(false);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(userStorage.findById(anyInt()))
                .thenReturn(Optional.of(initiator));

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> requestService.addParticipationRequest(initiator.getId(), event.getId())
        );

        assertThat(
                e.getMessage(),
                equalTo("Пользователь с id=1 отправил запрос на участие в собственном событии с id=1")
        );
    }

    @Test
    public void shouldNotAddParticipationRequestWhenEventNotPublished() {
        event.setState(EventState.PENDING);
        Mockito
                .when(requestStorage.existsByRequesterIdAndEventId(anyInt(), anyInt()))
                .thenReturn(false);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(userStorage.findById(anyInt()))
                .thenReturn(Optional.of(requester));

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> requestService.addParticipationRequest(requester.getId(), event.getId())
        );

        assertThat(
                e.getMessage(),
                equalTo("Пользователь с id=2 отправил запрос на участие в неопубликованном событии с id=1")
        );
    }

    @Test
    public void shouldNotAddParticipationRequestWhenLimitReached() {
        Mockito
                .when(requestStorage.existsByRequesterIdAndEventId(anyInt(), anyInt()))
                .thenReturn(false);
        Mockito
                .when(eventStorage.findById(anyInt()))
                .thenReturn(Optional.of(event));
        Mockito
                .when(userStorage.findById(anyInt()))
                .thenReturn(Optional.of(requester));
        Mockito
                .when(requestStorage.countByStatusAndEventId(any(ParticipationRequestStatus.class), anyInt()))
                .thenReturn(10);

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> requestService.addParticipationRequest(requester.getId(), event.getId())
        );

        assertThat(
                e.getMessage(),
                equalTo("Пользователь с id=2 отправил запрос на участие " +
                        "в событии с id=1 с достигнутым лимитом запросов")
        );
    }

    @Test
    public void shouldCancelParticipationRequest() {
        Mockito
                .when(requestStorage.findById(anyInt()))
                .thenReturn(Optional.of(request));
        Mockito
                .when(requestStorage.save(any(ParticipationRequest.class)))
                .then(returnsFirstArg());

        ParticipationRequestDto requestDto =
                requestService.cancelParticipationRequest(requester.getId(), request.getId());

        assertThat(requestDto.getStatus(), equalTo(ParticipationRequestStatus.CANCELED));
    }

    @Test
    public void shouldNotCancelParticipationRequestWhenRequestNotFound() {
        Mockito
                .when(requestStorage.findById(anyInt()))
                .thenReturn(Optional.empty());

        ParticipationRequestNotFoundException e = Assertions.assertThrows(
                ParticipationRequestNotFoundException.class,
                () -> requestService.cancelParticipationRequest(requester.getId(), request.getId())
        );

        assertThat(
                e.getMessage(),
                equalTo("Запрос на участие с id=1 не найден")
        );
    }

    @Test
    public void shouldNotCancelParticipationRequestWhenNotRequester() {
        Mockito
                .when(requestStorage.findById(anyInt()))
                .thenReturn(Optional.of(request));

        OperationConditionsFailureException e = Assertions.assertThrows(
                OperationConditionsFailureException.class,
                () -> requestService.cancelParticipationRequest(initiator.getId(), request.getId())
        );

        assertThat(
                e.getMessage(),
                equalTo("Запрос на участие с id=1 не принадлежит пользователю с id=1")
        );
    }
}