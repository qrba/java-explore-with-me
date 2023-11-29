package ru.practicum.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EventDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsClient {
    private final RestTemplate restTemplate;

    private final String resource = "http://localhost:9090";

    public ResponseEntity<Object> postEvent(EventDto eventDto) {
        try {
            return restTemplate.postForEntity(resource + "/hit", eventDto, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder path = new StringBuilder(resource + "/stats?");
        path.append("&start=").append(start.format(formatter));
        path.append("&end=").append(end.format(formatter));
        if (uris != null) for (String uri : uris) {
            path.append("&uris=").append(uri);
        }
        path.append("&unique=").append(unique);

        try {
            return restTemplate.getForEntity(path.toString(), Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }
}