package ru.practicum.statsvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statsvc.model.Event;
import ru.practicum.statsvc.model.EventDtoIn;
import ru.practicum.statsvc.model.EventDtoOut;
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
    public void saveEvent(EventDtoIn eventDtoIn) {
        Event event = eventStorage.save(eventFromDto(eventDtoIn));
        log.info("Добавлено событие {}", event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDtoOut> getEvents(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (end.isBefore(start))
            throw new DateTimeException("Нижняя граница временного интервала не может быть больше верхней границы.");
        log.info("Запрошена статистика по следующим параметрам: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        if (uris == null || uris.isEmpty()) {
            if (unique) return eventStorage.getAllStatsForUniqueIps(start, end);
            return eventStorage.getAllStats(start, end);
        } else {
            if (unique) return eventStorage.getAllStatsForUrisAndUniqueIps(start, end, uris);
            return eventStorage.getAllStatsForUris(start, end, uris);
        }
    }
}