package ru.practicum.statsvc.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.statsvc.model.Event;
import ru.practicum.statsvc.model.dto.EventDtoIn;
import ru.practicum.statsvc.model.dto.EventDtoOut;
import ru.practicum.statsvc.service.EventServiceImpl;
import ru.practicum.statsvc.storage.EventStorage;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @Mock
    private EventStorage eventStorage;
    @InjectMocks
    private EventServiceImpl eventService;

    private final EventDtoOut eventDtoOut = new EventDtoOut(
            "test-application",
            "/test",
            1L
    );

    @Test
    public void shouldSaveEvent() {
        eventService.saveEvent(
                new EventDtoIn(
                        "test-application",
                        "/test",
                        "192.168.0.1",
                        LocalDateTime.now().minusHours(1)
                )
        );
        Mockito.verify(eventStorage).save(any(Event.class));
    }

    @Test
    public void shouldGetAllStats() {
        Mockito
                .when(eventStorage.getAllStats(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(eventDtoOut));

        List<EventDtoOut> eventDtoOutList = eventService.getEvents(
                LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                Collections.emptyList(),
                false
        );
        EventDtoOut eventDtoOutFromService = eventDtoOutList.get(0);

        assertThat(eventDtoOutList.size(), equalTo(1));
        assertThat(eventDtoOutFromService.getApp(), equalTo(eventDtoOut.getApp()));
        assertThat(eventDtoOutFromService.getUri(), equalTo(eventDtoOut.getUri()));
        assertThat(eventDtoOutFromService.getHits(), equalTo(eventDtoOut.getHits()));
    }

    @Test
    public void shouldGetAllStatsWithUniqueIps() {
        Mockito
                .when(eventStorage.getAllStatsWithUniqueIps(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(eventDtoOut));

        List<EventDtoOut> eventDtoOutList = eventService.getEvents(
                LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                Collections.emptyList(),
                true
        );
        EventDtoOut eventDtoOutFromService = eventDtoOutList.get(0);

        assertThat(eventDtoOutList.size(), equalTo(1));
        assertThat(eventDtoOutFromService.getApp(), equalTo(eventDtoOut.getApp()));
        assertThat(eventDtoOutFromService.getUri(), equalTo(eventDtoOut.getUri()));
        assertThat(eventDtoOutFromService.getHits(), equalTo(eventDtoOut.getHits()));
    }

    @Test
    public void shouldGetAllStatsWithUris() {
        Mockito
                .when(eventStorage.getAllStatsWithUris(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(eventDtoOut));

        List<EventDtoOut> eventDtoOutList = eventService.getEvents(
                LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                List.of("/test"),
                false
        );
        EventDtoOut eventDtoOutFromService = eventDtoOutList.get(0);

        assertThat(eventDtoOutList.size(), equalTo(1));
        assertThat(eventDtoOutFromService.getApp(), equalTo(eventDtoOut.getApp()));
        assertThat(eventDtoOutFromService.getUri(), equalTo(eventDtoOut.getUri()));
        assertThat(eventDtoOutFromService.getHits(), equalTo(eventDtoOut.getHits()));
    }

    @Test
    public void shouldGetAllStatsWithUrisAndUniqueIps() {
        Mockito
                .when(eventStorage.getAllStatsWithUrisAndUniqueIps(
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        anyList())
                )
                .thenReturn(List.of(eventDtoOut));

        List<EventDtoOut> eventDtoOutList = eventService.getEvents(
                LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                List.of("/test"),
                true
        );
        EventDtoOut eventDtoOutFromService = eventDtoOutList.get(0);

        assertThat(eventDtoOutList.size(), equalTo(1));
        assertThat(eventDtoOutFromService.getApp(), equalTo(eventDtoOut.getApp()));
        assertThat(eventDtoOutFromService.getUri(), equalTo(eventDtoOut.getUri()));
        assertThat(eventDtoOutFromService.getHits(), equalTo(eventDtoOut.getHits()));
    }

    @Test
    public void shouldNotGetStatsWhenStartIsAfterEnd() {
        DateTimeException e = Assertions.assertThrows(
                DateTimeException.class,
                () -> eventService.getEvents(
                        LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                        LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                        Collections.emptyList(),
                        true
                )
        );

        assertThat(e.getMessage(),
                equalTo("Нижняя граница временного интервала не может быть больше верхней границы"));
    }
}
