package ru.practicum.statsvc.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.StatsDto;
import ru.practicum.statsvc.model.Event;
import ru.practicum.dto.EventDto;
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

    private final StatsDto statsDto = new StatsDto(
            "test-application",
            "/test",
            1L
    );

    @Test
    public void shouldSaveEvent() {
        eventService.saveEvent(
                new EventDto(
                        "test-application",
                        "/test",
                        "192.168.0.1",
                        LocalDateTime.now().minusHours(1)
                )
        );
        Mockito.verify(eventStorage).save(any(Event.class));
    }

    @Test
    public void shouldGetStats() {
        Mockito
                .when(eventStorage.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(statsDto));

        List<StatsDto> statsDtoList = eventService.getStats(
                LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                Collections.emptyList(),
                false
        );
        StatsDto statsDtoFromService = statsDtoList.get(0);

        assertThat(statsDtoList.size(), equalTo(1));
        assertThat(statsDtoFromService.getApp(), equalTo(statsDto.getApp()));
        assertThat(statsDtoFromService.getUri(), equalTo(statsDto.getUri()));
        assertThat(statsDtoFromService.getHits(), equalTo(statsDto.getHits()));
    }

    @Test
    public void shouldGetStatsWithUniqueIps() {
        Mockito
                .when(eventStorage.getStatsWithUniqueIps(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(statsDto));

        List<StatsDto> statsDtoList = eventService.getStats(
                LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                Collections.emptyList(),
                true
        );
        StatsDto statsDtoFromService = statsDtoList.get(0);

        assertThat(statsDtoList.size(), equalTo(1));
        assertThat(statsDtoFromService.getApp(), equalTo(statsDto.getApp()));
        assertThat(statsDtoFromService.getUri(), equalTo(statsDto.getUri()));
        assertThat(statsDtoFromService.getHits(), equalTo(statsDto.getHits()));
    }

    @Test
    public void shouldGetStatsWithUris() {
        Mockito
                .when(eventStorage.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(statsDto));

        List<StatsDto> statsDtoList = eventService.getStats(
                LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                List.of("/test"),
                false
        );
        StatsDto statsDtoFromService = statsDtoList.get(0);

        assertThat(statsDtoList.size(), equalTo(1));
        assertThat(statsDtoFromService.getApp(), equalTo(statsDto.getApp()));
        assertThat(statsDtoFromService.getUri(), equalTo(statsDto.getUri()));
        assertThat(statsDtoFromService.getHits(), equalTo(statsDto.getHits()));
    }

    @Test
    public void shouldGetStatsWithUrisAndUniqueIps() {
        Mockito
                .when(eventStorage.getStatsWithUniqueIps(
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        anyList())
                )
                .thenReturn(List.of(statsDto));

        List<StatsDto> statsDtoList = eventService.getStats(
                LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                List.of("/test"),
                true
        );
        StatsDto statsDtoFromService = statsDtoList.get(0);

        assertThat(statsDtoList.size(), equalTo(1));
        assertThat(statsDtoFromService.getApp(), equalTo(statsDto.getApp()));
        assertThat(statsDtoFromService.getUri(), equalTo(statsDto.getUri()));
        assertThat(statsDtoFromService.getHits(), equalTo(statsDto.getHits()));
    }

    @Test
    public void shouldNotGetStatsWhenStartIsAfterEnd() {
        DateTimeException e = Assertions.assertThrows(
                DateTimeException.class,
                () -> eventService.getStats(
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
