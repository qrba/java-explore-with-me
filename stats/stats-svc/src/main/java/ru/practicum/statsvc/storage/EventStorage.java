package ru.practicum.statsvc.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.StatsDto;
import ru.practicum.statsvc.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventStorage extends JpaRepository<Event, Integer> {
    @Query("SELECT new ru.practicum.dto.StatsDto(e.app, e.uri, COUNT(e.ip)) " +
            "FROM Event AS e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "AND (e.uri IN :uris OR :uris = null) " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.dto.StatsDto(e.app, e.uri, COUNT(DISTINCT(e.ip))) " +
            "FROM Event AS e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "AND (e.uri IN :uris OR :uris = null) " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(DISTINCT(e.ip)) DESC")
    List<StatsDto> getStatsWithUniqueIps(LocalDateTime start, LocalDateTime end, List<String> uris);
}