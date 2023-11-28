package ru.practicum.statsvc.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.statsvc.model.Event;
import ru.practicum.statsvc.model.EventDtoOut;

import java.time.LocalDateTime;
import java.util.List;

public interface EventStorage extends JpaRepository<Event, Integer> {
    @Query("SELECT new ru.practicum.statsvc.model.EventDtoOut(e.app, e.uri, COUNT(e.ip)) " +
            "FROM Event AS e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<EventDtoOut> getAllStats(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.statsvc.model.EventDtoOut(e.app, e.uri, COUNT(e.ip)) " +
            "FROM Event AS e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 " +
            "AND e.uri IN ?3 " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<EventDtoOut> getAllStatsForUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.statsvc.model.EventDtoOut(e.app, e.uri, COUNT(DISTINCT(e.ip))) " +
            "FROM Event AS e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(DISTINCT(e.ip)) DESC")
    List<EventDtoOut> getAllStatsForUniqueIps(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.statsvc.model.EventDtoOut(e.app, e.uri, COUNT(DISTINCT(e.ip))) " +
            "FROM Event AS e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 " +
            "AND e.uri IN ?3 " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(DISTINCT(e.ip)) DESC")
    List<EventDtoOut> getAllStatsForUrisAndUniqueIps(LocalDateTime start, LocalDateTime end, List<String> uris);
}