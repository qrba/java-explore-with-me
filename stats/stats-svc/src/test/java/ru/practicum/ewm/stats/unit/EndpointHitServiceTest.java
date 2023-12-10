package ru.practicum.ewm.stats.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.stats.model.EndpointHit;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.stats.service.StatsServiceImpl;
import ru.practicum.ewm.stats.storage.StatsStorage;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

@ExtendWith(MockitoExtension.class)
public class EndpointHitServiceTest {
    @Mock
    private StatsStorage statsStorage;
    @InjectMocks
    private StatsServiceImpl eventService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ViewStatsDto viewStatsDto = new ViewStatsDto(
            "test-application",
            "/test",
            1L
    );

    @Test
    public void shouldSaveEvent() {
        eventService.saveEvent(
                new EndpointHitDto(
                        "test-application",
                        "/test",
                        "192.168.0.1",
                        LocalDateTime.now().minusHours(1).format(formatter)
                )
        );
        Mockito.verify(statsStorage).save(any(EndpointHit.class));
    }

    @Test
    public void shouldGetStats() {
        Mockito
                .when(statsStorage.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(viewStatsDto));

        List<ViewStatsDto> viewStatsDtoList = eventService.getStats(
                LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                Collections.emptyList(),
                false
        );
        ViewStatsDto viewStatsDtoFromService = viewStatsDtoList.get(0);

        assertThat(viewStatsDtoList.size(), equalTo(1));
        assertThat(viewStatsDtoFromService.getApp(), equalTo(viewStatsDto.getApp()));
        assertThat(viewStatsDtoFromService.getUri(), equalTo(viewStatsDto.getUri()));
        assertThat(viewStatsDtoFromService.getHits(), equalTo(viewStatsDto.getHits()));
    }

    @Test
    public void shouldGetStatsWithUniqueIps() {
        Mockito
                .when(statsStorage.getStatsWithUniqueIps(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(viewStatsDto));

        List<ViewStatsDto> viewStatsDtoList = eventService.getStats(
                LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                Collections.emptyList(),
                true
        );
        ViewStatsDto viewStatsDtoFromService = viewStatsDtoList.get(0);

        assertThat(viewStatsDtoList.size(), equalTo(1));
        assertThat(viewStatsDtoFromService.getApp(), equalTo(viewStatsDto.getApp()));
        assertThat(viewStatsDtoFromService.getUri(), equalTo(viewStatsDto.getUri()));
        assertThat(viewStatsDtoFromService.getHits(), equalTo(viewStatsDto.getHits()));
    }

    @Test
    public void shouldGetStatsWithUris() {
        Mockito
                .when(statsStorage.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(viewStatsDto));

        List<ViewStatsDto> viewStatsDtoList = eventService.getStats(
                LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                List.of("/test"),
                false
        );
        ViewStatsDto viewStatsDtoFromService = viewStatsDtoList.get(0);

        assertThat(viewStatsDtoList.size(), equalTo(1));
        assertThat(viewStatsDtoFromService.getApp(), equalTo(viewStatsDto.getApp()));
        assertThat(viewStatsDtoFromService.getUri(), equalTo(viewStatsDto.getUri()));
        assertThat(viewStatsDtoFromService.getHits(), equalTo(viewStatsDto.getHits()));
    }

    @Test
    public void shouldGetStatsWithUrisAndUniqueIps() {
        Mockito
                .when(statsStorage.getStatsWithUniqueIps(
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        anyList())
                )
                .thenReturn(List.of(viewStatsDto));

        List<ViewStatsDto> viewStatsDtoList = eventService.getStats(
                LocalDateTime.of(2020, 10, 10, 0, 0, 0),
                LocalDateTime.of(2023, 10, 10, 0, 0, 0),
                List.of("/test"),
                true
        );
        ViewStatsDto viewStatsDtoFromService = viewStatsDtoList.get(0);

        assertThat(viewStatsDtoList.size(), equalTo(1));
        assertThat(viewStatsDtoFromService.getApp(), equalTo(viewStatsDto.getApp()));
        assertThat(viewStatsDtoFromService.getUri(), equalTo(viewStatsDto.getUri()));
        assertThat(viewStatsDtoFromService.getHits(), equalTo(viewStatsDto.getHits()));
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
