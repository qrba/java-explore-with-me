package ru.practicum.statsvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statsvc.model.Event;
import ru.practicum.dto.EventDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.statsvc.storage.EventStorage;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.statsvc.model.EventMapper.eventFromDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventStorage eventStorage;

    @Override
    @Transactional
    public void saveEvent(EventDto eventDto) {
        Event event = eventStorage.save(eventFromDto(eventDto));
        log.info("Добавлено событие {}", event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (end.isBefore(start))
            throw new DateTimeException("Нижняя граница временного интервала не может быть больше верхней границы");
        log.info("Запрошена статистика по следующим параметрам: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        if (unique) return eventStorage.getStatsWithUniqueIps(start, end, uris);
        else return eventStorage.getStats(start, end, uris);
    }
}