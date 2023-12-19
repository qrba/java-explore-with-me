package ru.practicum.ewm.api.place;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.place.controller.PlaceControllerPublic;
import ru.practicum.ewm.place.model.dto.PlaceFullDto;
import ru.practicum.ewm.place.model.dto.PlaceShortDto;
import ru.practicum.ewm.place.service.PlaceService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PlaceControllerPublic.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PlaceControllerPublicTest {
    @MockBean
    private PlaceService placeService;
    @MockBean
    private EventService eventService;
    private final MockMvc mvc;

    @Test
    public void shouldGetCategoryById() throws Exception {
        PlaceFullDto placeFullDto = new PlaceFullDto(
                1,
                "place",
                "d".repeat(20),
                new Location(0,0),
                1
        );
        Mockito
                .when(placeService.getPlaceById(anyInt()))
                .thenReturn(placeFullDto);

        mvc.perform(get("/places/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(placeFullDto.getId()))
                .andExpect(jsonPath("$.name").value(placeFullDto.getName()))
                .andExpect(jsonPath("$.description").value(placeFullDto.getDescription()))
                .andExpect(jsonPath("$.location.lat").value(placeFullDto.getLocation().getLat()))
                .andExpect(jsonPath("$.location.lon").value(placeFullDto.getLocation().getLon()))
                .andExpect(jsonPath("$.radius").value(placeFullDto.getRadius()));
    }

    @Test
    public void shouldGetCategories() throws Exception {
        PlaceShortDto placeShortDto = new PlaceShortDto(1, "place");
        Mockito
                .when(placeService.getPlaces(any(), anyInt(), anyInt()))
                .thenReturn(List.of(placeShortDto));

        mvc.perform(get("/places")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(placeShortDto.getId()))
                .andExpect(jsonPath("$.[0].name").value(placeShortDto.getName()));
    }

    @Test
    public void shouldGetEventsInPlace() throws Exception {
        mvc.perform(get("/places/1/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(eventService).getEventsInPlace(anyInt(), anyInt(), anyInt());
    }
}