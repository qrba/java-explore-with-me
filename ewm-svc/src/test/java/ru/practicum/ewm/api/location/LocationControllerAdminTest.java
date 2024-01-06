package ru.practicum.ewm.api.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.event.model.Coordinate;
import ru.practicum.ewm.location.controller.LocationControllerAdmin;
import ru.practicum.ewm.location.model.dto.NewLocationDto;
import ru.practicum.ewm.location.model.dto.LocationFullDto;
import ru.practicum.ewm.location.model.dto.LocationUpdateRequest;
import ru.practicum.ewm.location.service.LocationService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LocationControllerAdmin.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LocationControllerAdminTest {
    @MockBean
    private LocationService locationService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    private final LocationFullDto locationFullDto = new LocationFullDto(
            1,
            "location",
            "d".repeat(20),
            new Coordinate(0,0),
            1
    );

    @Test
    public void shouldAddLocation() throws Exception {
        NewLocationDto newLocationDto = new NewLocationDto(
                locationFullDto.getName(),
                locationFullDto.getDescription(),
                locationFullDto.getCoordinate(),
                locationFullDto.getRadius()
        );
        Mockito
                .when(locationService.addLocation(any(NewLocationDto.class)))
                .thenReturn(locationFullDto);

        mvc.perform(post("/admin/locations")
                        .content(mapper.writeValueAsString(newLocationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(locationFullDto.getId()))
                .andExpect(jsonPath("$.name").value(locationFullDto.getName()))
                .andExpect(jsonPath("$.description").value(locationFullDto.getDescription()))
                .andExpect(jsonPath("$.location.lat").value(locationFullDto.getCoordinate().getLat()))
                .andExpect(jsonPath("$.location.lon").value(locationFullDto.getCoordinate().getLon()))
                .andExpect(jsonPath("$.radius").value(locationFullDto.getRadius()));
    }

    @Test
    public void shouldNotAddLocationWhenBlankName() throws Exception {
        NewLocationDto newLocationDto = new NewLocationDto(
                "",
                locationFullDto.getDescription(),
                locationFullDto.getCoordinate(),
                locationFullDto.getRadius()
        );

        mvc.perform(post("/admin/locations")
                        .content(mapper.writeValueAsString(newLocationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddLocationWhenNameTooLong() throws Exception {
        NewLocationDto newLocationDto = new NewLocationDto(
                "Location names longer than 50 characters are prohibited",
                locationFullDto.getDescription(),
                locationFullDto.getCoordinate(),
                locationFullDto.getRadius()
        );

        mvc.perform(post("/admin/locations")
                        .content(mapper.writeValueAsString(newLocationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddLocationWhenBlankDescription() throws Exception {
        NewLocationDto newLocationDto = new NewLocationDto(
                "location",
                "",
                locationFullDto.getCoordinate(),
                locationFullDto.getRadius()
        );

        mvc.perform(post("/admin/locations")
                        .content(mapper.writeValueAsString(newLocationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddLocationWhenDescriptionTooShort() throws Exception {
        NewLocationDto newLocationDto = new NewLocationDto(
                "location",
                "d",
                locationFullDto.getCoordinate(),
                locationFullDto.getRadius()
        );

        mvc.perform(post("/admin/locations")
                        .content(mapper.writeValueAsString(newLocationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddLocationWhenDescriptionTooLong() throws Exception {
        NewLocationDto newLocationDto = new NewLocationDto(
                "location",
                "d".repeat(2001),
                locationFullDto.getCoordinate(),
                locationFullDto.getRadius()
        );

        mvc.perform(post("/admin/locations")
                        .content(mapper.writeValueAsString(newLocationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddLocationWhenLocationNull() throws Exception {
        NewLocationDto newLocationDto = new NewLocationDto(
                "location",
                locationFullDto.getDescription(),
                null,
                locationFullDto.getRadius()
        );

        mvc.perform(post("/admin/locations")
                        .content(mapper.writeValueAsString(newLocationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldNotAddLocationWhenZeroRadius() throws Exception {
        NewLocationDto newLocationDto = new NewLocationDto(
                "location",
                locationFullDto.getDescription(),
                locationFullDto.getCoordinate(),
                0
        );

        mvc.perform(post("/admin/locations")
                        .content(mapper.writeValueAsString(newLocationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."));
    }

    @Test
    public void shouldDeleteLocation() throws Exception {
        mvc.perform(delete("/admin/locations/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Mockito.verify(locationService).deleteLocation(anyInt());
    }

    @Test
    public void shouldUpdateLocation() throws Exception {
        LocationUpdateRequest request = new LocationUpdateRequest(
                locationFullDto.getId(),
                locationFullDto.getName(),
                locationFullDto.getDescription(),
                locationFullDto.getCoordinate(),
                locationFullDto.getRadius()
        );
        Mockito
                .when(locationService.updateLocation(any(LocationUpdateRequest.class)))
                .thenReturn(locationFullDto);

        mvc.perform(patch("/admin/locations/" + locationFullDto.getId())
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(locationFullDto.getId()))
                .andExpect(jsonPath("$.name").value(locationFullDto.getName()))
                .andExpect(jsonPath("$.description").value(locationFullDto.getDescription()))
                .andExpect(jsonPath("$.location.lat").value(locationFullDto.getCoordinate().getLat()))
                .andExpect(jsonPath("$.location.lon").value(locationFullDto.getCoordinate().getLon()))
                .andExpect(jsonPath("$.radius").value(locationFullDto.getRadius()));
    }
}