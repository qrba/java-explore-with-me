package ru.practicum.ewm.stats.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsStorage extends JpaRepository<EndpointHit, Integer> {
    @Query("SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app, e.uri, COUNT(e.ip)) " +
            "FROM EndpointHit AS e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "AND (e.uri IN :uris OR :uris = null) " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.ewm.dto.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT(e.ip))) " +
            "FROM EndpointHit AS e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "AND (e.uri IN :uris OR :uris = null) " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(DISTINCT(e.ip)) DESC")
    List<ViewStatsDto> getStatsWithUniqueIps(LocalDateTime start, LocalDateTime end, List<String> uris);
}