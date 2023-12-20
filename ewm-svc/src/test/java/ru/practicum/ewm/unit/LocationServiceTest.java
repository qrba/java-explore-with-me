package ru.practicum.ewm.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.event.model.Coordinate;
import ru.practicum.ewm.exception.LocationAlreadyExistsException;
import ru.practicum.ewm.exception.LocationNotFoundException;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.model.dto.NewLocationDto;
import ru.practicum.ewm.location.model.dto.LocationFullDto;
import ru.practicum.ewm.location.model.dto.LocationShortDto;
import ru.practicum.ewm.location.model.dto.LocationUpdateRequest;
import ru.practicum.ewm.location.service.LocationServiceImpl;
import ru.practicum.ewm.location.storage.LocationRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static ru.practicum.ewm.location.model.dto.LocationMapper.locationToFullDto;
import static ru.practicum.ewm.location.model.dto.LocationMapper.locationToShortDto;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {
    @Mock
    private LocationRepository locationRepository;
    @InjectMocks
    private LocationServiceImpl locationService;

    private final Location location = new Location(
            1,
            "location",
            "d".repeat(20),
            0,
            0,
            1
    );

    LocationUpdateRequest request = new LocationUpdateRequest(
            1,
            location.getName(),
            location.getDescription(),
            new Coordinate(location.getLat(), location.getLon()),
            location.getRadius()
    );

    @Test
    public void shouldAddLocation() {
        NewLocationDto newLocationDto = new NewLocationDto(
                location.getName(),
                location.getDescription(),
                new Coordinate(location.getLat(), location.getLon()),
                location.getRadius()
        );

        Mockito
                .when(locationRepository.save(any(Location.class)))
                .thenReturn(location);

        LocationFullDto locationFullDtoFromService = locationService.addLocation(newLocationDto);
        LocationFullDto locationFullDto = locationToFullDto(location);

        assertThat(locationFullDto, equalTo(locationFullDtoFromService));
    }

    @Test
    public void shouldNotAddLocationWhenNameNotUnique() {
        NewLocationDto newLocationDto = new NewLocationDto(
                location.getName(),
                location.getDescription(),
                new Coordinate(location.getLat(), location.getLon()),
                location.getRadius()
        );

        Mockito
                .when(locationRepository.save(any(Location.class)))
                .thenThrow(DataIntegrityViolationException.class);

        LocationAlreadyExistsException e = Assertions.assertThrows(
                LocationAlreadyExistsException.class,
                () -> locationService.addLocation(newLocationDto)
        );

        assertThat(e.getMessage(), equalTo("Локация с названием 'location' уже существует"));
    }

    @Test
    public void shouldDeleteLocation() {
        Mockito
                .when(locationRepository.existsById(anyInt()))
                .thenReturn(true);
        locationService.deleteLocation(1);

        Mockito.verify(locationRepository).deleteById(anyInt());
    }

    @Test
    public void shouldNotDeleteLocationWhenLocationNotFound() {
        LocationNotFoundException e = Assertions.assertThrows(
                LocationNotFoundException.class,
                () -> locationService.deleteLocation(1)
        );

        assertThat(e.getMessage(), equalTo("Локация с id=1 не найдена"));
    }

    @Test
    public void shouldUpdateLocation() {
        Mockito
                .when(locationRepository.findById(anyInt()))
                .thenReturn(Optional.of(location));
        Mockito
                .when(locationRepository.existsByNameAndIdNot(anyString(), anyInt()))
                .thenReturn(false);
        Mockito
                .when(locationRepository.save(any(Location.class)))
                .then(returnsFirstArg());

        LocationFullDto locationFullDtoFromService = locationService.updateLocation(request);
        LocationFullDto locationFullDto = locationToFullDto(location);

        assertThat(locationFullDto, equalTo(locationFullDtoFromService));
    }

    @Test
    public void shouldNotUpdateLocationWhenLocationNotFound() {
        Mockito
                .when(locationRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        LocationNotFoundException e = Assertions.assertThrows(
                LocationNotFoundException.class,
                () -> locationService.updateLocation(request)
        );

        assertThat(e.getMessage(), equalTo("Локация с id=1 не найдена"));
    }

    @Test
    public void shouldNotUpdateLocationWhenNameNotUnique() {
        Mockito
                .when(locationRepository.findById(anyInt()))
                .thenReturn(Optional.of(location));
        Mockito
                .when(locationRepository.existsByNameAndIdNot(anyString(), anyInt()))
                .thenReturn(true);

        LocationAlreadyExistsException e = Assertions.assertThrows(
                LocationAlreadyExistsException.class,
                () -> locationService.updateLocation(request)
        );

        assertThat(e.getMessage(), equalTo("Локация с названием 'location' уже существует"));
    }

    @Test
    public void shouldGetLocationById() {
        Mockito
                .when(locationRepository.findById(anyInt()))
                .thenReturn(Optional.of(location));

        LocationFullDto locationFullDtoFromService = locationService.getLocationById(1);
        LocationFullDto locationFullDto = locationToFullDto(location);

        assertThat(locationFullDto, equalTo(locationFullDtoFromService));
    }

    @Test
    public void shouldNotGetLocationByIdWhenLocationNotFound() {
        Mockito
                .when(locationRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        LocationNotFoundException e = Assertions.assertThrows(
                LocationNotFoundException.class,
                () -> locationService.updateLocation(request)
        );

        assertThat(e.getMessage(), equalTo("Локация с id=1 не найдена"));
    }

    @Test
    public void shouldGetLocation() {
        Mockito
                .when(locationRepository.findLocations(anyString(), any(Pageable.class)))
                .thenReturn(List.of(location));

        List<LocationShortDto> locations = locationService.getLocations("location", 0, 10);

        assertThat(1, equalTo(locations.size()));

        LocationShortDto locationShortDtoFromService = locations.get(0);
        LocationShortDto locationShortDto = locationToShortDto(location);

        assertThat(locationShortDto, equalTo(locationShortDtoFromService));
    }
}