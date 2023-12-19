package ru.practicum.ewm.api.place;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.place.controller.PlaceControllerAdmin;
import ru.practicum.ewm.place.model.dto.NewPlaceDto;
import ru.practicum.ewm.place.model.dto.PlaceFullDto;
import ru.practicum.ewm.place.model.dto.PlaceUpdateRequest;
import ru.practicum.ewm.place.service.PlaceService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PlaceControllerAdmin.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PlaceControllerAdminTest {
    @MockBean
    private PlaceService placeService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    private final PlaceFullDto placeFullDto = new PlaceFullDto(
            1,
            "place",
            "d".repeat(20),
            new Location(0,0),
            1
    );

    @Test
    public void shouldAddPlace() throws Exception {
        NewPlaceDto newPlaceDto = new NewPlaceDto(
                placeFullDto.getName(),
                placeFullDto.getDescription(),
                placeFullDto.getLocation(),
                placeFullDto.getRadius()
        );
        Mockito
                .when(placeService.addPlace(any(NewPlaceDto.class)))
                .thenReturn(placeFullDto);

        mvc.perform(post("/admin/places")
                        .content(mapper.writeValueAsString(newPlaceDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(placeFullDto.getId()))
                .andExpect(jsonPath("$.name").value(placeFullDto.getName()))
                .andExpect(jsonPath("$.description").value(placeFullDto.getDescription()))
                .andExpect(jsonPath("$.location.lat").value(placeFullDto.getLocation().getLat()))
                .andExpect(jsonPath("$.location.lon").value(placeFullDto.getLocation().getLon()))
                .andExpect(jsonPath("$.radius").value(placeFullDto.getRadius()));
    }

    @Test
    public void shouldNotAddPlaceWhenBlankName() throws Exception {
        NewPlaceDto newPlaceDto = new NewPlaceDto(
                "",
                placeFullDto.getDescription(),
                placeFullDto.getLocation(),
                placeFullDto.getRadius()
        );

        mvc.perform(post("/admin/places")
                        .content(mapper.writeValueAsString(newPlaceDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddPlaceWhenNameTooLong() throws Exception {
        NewPlaceDto newPlaceDto = new NewPlaceDto(
                "Place names longer than 50 characters are prohibited",
                placeFullDto.getDescription(),
                placeFullDto.getLocation(),
                placeFullDto.getRadius()
        );

        mvc.perform(post("/admin/places")
                        .content(mapper.writeValueAsString(newPlaceDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddPlaceWhenBlankDescription() throws Exception {
        NewPlaceDto newPlaceDto = new NewPlaceDto(
                "place",
                "",
                placeFullDto.getLocation(),
                placeFullDto.getRadius()
        );

        mvc.perform(post("/admin/places")
                        .content(mapper.writeValueAsString(newPlaceDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddPlaceWhenDescriptionTooShort() throws Exception {
        NewPlaceDto newPlaceDto = new NewPlaceDto(
                "place",
                "d",
                placeFullDto.getLocation(),
                placeFullDto.getRadius()
        );

        mvc.perform(post("/admin/places")
                        .content(mapper.writeValueAsString(newPlaceDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddPlaceWhenDescriptionTooLong() throws Exception {
        NewPlaceDto newPlaceDto = new NewPlaceDto(
                "place",
                "d".repeat(2001),
                placeFullDto.getLocation(),
                placeFullDto.getRadius()
        );

        mvc.perform(post("/admin/places")
                        .content(mapper.writeValueAsString(newPlaceDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddPlaceWhenLocationNull() throws Exception {
        NewPlaceDto newPlaceDto = new NewPlaceDto(
                "place",
                placeFullDto.getDescription(),
                null,
                placeFullDto.getRadius()
        );

        mvc.perform(post("/admin/places")
                        .content(mapper.writeValueAsString(newPlaceDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddPlaceWhenZeroRadius() throws Exception {
        NewPlaceDto newPlaceDto = new NewPlaceDto(
                "place",
                placeFullDto.getDescription(),
                placeFullDto.getLocation(),
                0
        );

        mvc.perform(post("/admin/places")
                        .content(mapper.writeValueAsString(newPlaceDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldDeletePlace() throws Exception {
        mvc.perform(delete("/admin/places/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Mockito.verify(placeService).deletePlace(anyInt());
    }

    @Test
    public void shouldUpdatePlace() throws Exception {
        PlaceUpdateRequest request = new PlaceUpdateRequest(
                placeFullDto.getId(),
                placeFullDto.getName(),
                placeFullDto.getDescription(),
                placeFullDto.getLocation(),
                placeFullDto.getRadius()
        );
        Mockito
                .when(placeService.updatePlace(any(PlaceUpdateRequest.class)))
                .thenReturn(placeFullDto);

        mvc.perform(patch("/admin/places/" + placeFullDto.getId())
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(placeFullDto.getId()))
                .andExpect(jsonPath("$.name").value(placeFullDto.getName()))
                .andExpect(jsonPath("$.description").value(placeFullDto.getDescription()))
                .andExpect(jsonPath("$.location.lat").value(placeFullDto.getLocation().getLat()))
                .andExpect(jsonPath("$.location.lon").value(placeFullDto.getLocation().getLon()))
                .andExpect(jsonPath("$.radius").value(placeFullDto.getRadius()));
    }
}