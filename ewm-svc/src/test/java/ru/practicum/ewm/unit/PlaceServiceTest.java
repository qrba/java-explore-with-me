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
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.exception.PlaceAlreadyExistsException;
import ru.practicum.ewm.exception.PlaceNotFoundException;
import ru.practicum.ewm.place.model.Place;
import ru.practicum.ewm.place.model.dto.NewPlaceDto;
import ru.practicum.ewm.place.model.dto.PlaceFullDto;
import ru.practicum.ewm.place.model.dto.PlaceShortDto;
import ru.practicum.ewm.place.model.dto.PlaceUpdateRequest;
import ru.practicum.ewm.place.service.PlaceServiceImpl;
import ru.practicum.ewm.place.storage.PlaceRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static ru.practicum.ewm.place.model.dto.PlaceMapper.placeToFullDto;
import static ru.practicum.ewm.place.model.dto.PlaceMapper.placeToShortDto;

@ExtendWith(MockitoExtension.class)
public class PlaceServiceTest {
    @Mock
    private PlaceRepository placeRepository;
    @InjectMocks
    private PlaceServiceImpl placeService;

    private final Place place = new Place(
            1,
            "place",
            "d".repeat(20),
            0,
            0,
            1
    );

    PlaceUpdateRequest request = new PlaceUpdateRequest(
            1,
            place.getName(),
            place.getDescription(),
            new Location(place.getLat(), place.getLon()),
            place.getRadius()
    );

    @Test
    public void shouldAddPlace() {
        NewPlaceDto newPlaceDto = new NewPlaceDto(
                place.getName(),
                place.getDescription(),
                new Location(place.getLat(), place.getLon()),
                place.getRadius()
        );

        Mockito
                .when(placeRepository.save(any(Place.class)))
                .thenReturn(place);

        PlaceFullDto placeFullDtoFromService = placeService.addPlace(newPlaceDto);
        PlaceFullDto placeFullDto = placeToFullDto(place);

        assertThat(placeFullDto, equalTo(placeFullDtoFromService));
    }

    @Test
    public void shouldNotAddPlaceWhenNameNotUnique() {
        NewPlaceDto newPlaceDto = new NewPlaceDto(
                place.getName(),
                place.getDescription(),
                new Location(place.getLat(), place.getLon()),
                place.getRadius()
        );

        Mockito
                .when(placeRepository.save(any(Place.class)))
                .thenThrow(DataIntegrityViolationException.class);

        PlaceAlreadyExistsException e = Assertions.assertThrows(
                PlaceAlreadyExistsException.class,
                () -> placeService.addPlace(newPlaceDto)
        );

        assertThat(e.getMessage(), equalTo("Место с названием 'place' уже существует"));
    }

    @Test
    public void shouldDeletePlace() {
        Mockito
                .when(placeRepository.existsById(anyInt()))
                .thenReturn(true);
        placeService.deletePlace(1);

        Mockito.verify(placeRepository).deleteById(anyInt());
    }

    @Test
    public void shouldNotDeletePlaceWhenPlaceNotFound() {
        PlaceNotFoundException e = Assertions.assertThrows(
                PlaceNotFoundException.class,
                () -> placeService.deletePlace(1)
        );

        assertThat(e.getMessage(), equalTo("Место с id=1 не найдено"));
    }

    @Test
    public void shouldUpdatePlace() {
        Mockito
                .when(placeRepository.findById(anyInt()))
                .thenReturn(Optional.of(place));
        Mockito
                .when(placeRepository.existsByNameAndIdNot(anyString(), anyInt()))
                .thenReturn(false);
        Mockito
                .when(placeRepository.save(any(Place.class)))
                .then(returnsFirstArg());

        PlaceFullDto placeFullDtoFromService = placeService.updatePlace(request);
        PlaceFullDto placeFullDto = placeToFullDto(place);

        assertThat(placeFullDto, equalTo(placeFullDtoFromService));
    }

    @Test
    public void shouldNotUpdatePlaceWhenPlaceNotFound() {
        Mockito
                .when(placeRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        PlaceNotFoundException e = Assertions.assertThrows(
                PlaceNotFoundException.class,
                () -> placeService.updatePlace(request)
        );

        assertThat(e.getMessage(), equalTo("Место с id=1 не найдено"));
    }

    @Test
    public void shouldNotUpdatePlaceWhenNameNotUnique() {
        Mockito
                .when(placeRepository.findById(anyInt()))
                .thenReturn(Optional.of(place));
        Mockito
                .when(placeRepository.existsByNameAndIdNot(anyString(), anyInt()))
                .thenReturn(true);

        PlaceAlreadyExistsException e = Assertions.assertThrows(
                PlaceAlreadyExistsException.class,
                () -> placeService.updatePlace(request)
        );

        assertThat(e.getMessage(), equalTo("Место с названием 'place' уже существует"));
    }

    @Test
    public void shouldGetPlaceById() {
        Mockito
                .when(placeRepository.findById(anyInt()))
                .thenReturn(Optional.of(place));

        PlaceFullDto placeFullDtoFromService = placeService.getPlaceById(1);
        PlaceFullDto placeFullDto = placeToFullDto(place);

        assertThat(placeFullDto, equalTo(placeFullDtoFromService));
    }

    @Test
    public void shouldNotGetPlaceByIdWhenPlaceNotFound() {
        Mockito
                .when(placeRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        PlaceNotFoundException e = Assertions.assertThrows(
                PlaceNotFoundException.class,
                () -> placeService.updatePlace(request)
        );

        assertThat(e.getMessage(), equalTo("Место с id=1 не найдено"));
    }

    @Test
    public void shouldGetPlaces() {
        Mockito
                .when(placeRepository.findPlaces(anyString(), any(Pageable.class)))
                .thenReturn(List.of(place));

        List<PlaceShortDto> places = placeService.getPlaces("place", 0, 10);

        assertThat(1, equalTo(places.size()));

        PlaceShortDto placeShortDtoFromService = places.get(0);
        PlaceShortDto placeShortDto = placeToShortDto(place);

        assertThat(placeShortDto, equalTo(placeShortDtoFromService));
    }
}