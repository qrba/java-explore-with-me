package ru.practicum.ewm.api.location;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.event.model.Coordinate;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.location.controller.LocationControllerPublic;
import ru.practicum.ewm.location.model.dto.LocationFullDto;
import ru.practicum.ewm.location.model.dto.LocationShortDto;
import ru.practicum.ewm.location.service.LocationService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LocationControllerPublic.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LocationControllerPublicTest {
    @MockBean
    private LocationService locationService;
    @MockBean
    private EventService eventService;
    private final MockMvc mvc;

    @Test
    public void shouldGetLocationById() throws Exception {
        LocationFullDto locationFullDto = new LocationFullDto(
                1,
                "location",
                "d".repeat(20),
                new Coordinate(0,0),
                1
        );
        Mockito
                .when(locationService.getLocationById(anyInt()))
                .thenReturn(locationFullDto);

        mvc.perform(get("/locations/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(locationFullDto.getId()))
                .andExpect(jsonPath("$.name").value(locationFullDto.getName()))
                .andExpect(jsonPath("$.description").value(locationFullDto.getDescription()))
                .andExpect(jsonPath("$.location.lat").value(locationFullDto.getCoordinate().getLat()))
                .andExpect(jsonPath("$.location.lon").value(locationFullDto.getCoordinate().getLon()))
                .andExpect(jsonPath("$.radius").value(locationFullDto.getRadius()));
    }

    @Test
    public void shouldGetLocations() throws Exception {
        LocationShortDto locationShortDto = new LocationShortDto(1, "location");
        Mockito
                .when(locationService.getLocations(any(), anyInt(), anyInt()))
                .thenReturn(List.of(locationShortDto));

        mvc.perform(get("/locations")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(locationShortDto.getId()))
                .andExpect(jsonPath("$.[0].name").value(locationShortDto.getName()));
    }

    @Test
    public void shouldGetEventsInLocation() throws Exception {
        mvc.perform(get("/locations/1/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(eventService).getEventsInLocation(anyInt(), anyInt(), anyInt());
    }
}