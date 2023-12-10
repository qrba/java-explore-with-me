package ru.practicum.ewm.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatsClient {
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${stats-service.url}")
    private String resource;

    public ResponseEntity<Object> addHit(EndpointHitDto endpointHitDto) {
        try {
            return restTemplate.postForEntity(resource + "/hit", endpointHitDto, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder path = new StringBuilder(resource + "/stats?");
        if (start != null) path.append("&start=").append(start.format(formatter));
        if (end != null) path.append("&end=").append(end.format(formatter));
        if (uris != null) for (String uri : uris) {
            path.append("&uris=").append(uri);
        }
        if ((unique != null)) path.append("&unique=").append(unique);
        try {
            return restTemplate.getForEntity(path.toString(), Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }
}