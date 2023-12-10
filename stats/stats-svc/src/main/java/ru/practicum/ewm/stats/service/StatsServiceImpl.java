package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.model.EndpointHit;
import ru.practicum.ewm.stats.model.EndpointHitMapper;
import ru.practicum.ewm.stats.storage.StatsStorage;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsStorage statsStorage;

    @Override
    @Transactional
    public void saveEvent(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = statsStorage.save(EndpointHitMapper.endpointHitFromDto(endpointHitDto));
        log.info("Добавлено событие {}", endpointHit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (end.isBefore(start))
            throw new DateTimeException("Нижняя граница временного интервала не может быть больше верхней границы");
        log.info("Запрошена статистика по следующим параметрам: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        if (unique) return statsStorage.getStatsWithUniqueIps(start, end, uris);
        else return statsStorage.getStats(start, end, uris);
    }
}