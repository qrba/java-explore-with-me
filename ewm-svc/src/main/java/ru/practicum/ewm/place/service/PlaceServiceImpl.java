package ru.practicum.ewm.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.exception.PlaceAlreadyExistsException;
import ru.practicum.ewm.exception.PlaceNotFoundException;
import ru.practicum.ewm.place.model.Place;
import ru.practicum.ewm.place.model.dto.NewPlaceDto;
import ru.practicum.ewm.place.model.dto.PlaceFullDto;
import ru.practicum.ewm.place.model.dto.PlaceMapper;
import ru.practicum.ewm.place.model.dto.PlaceShortDto;
import ru.practicum.ewm.place.model.dto.PlaceUpdateRequest;
import ru.practicum.ewm.place.storage.PlaceRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.place.model.dto.PlaceMapper.placeFromNewPlaceDto;
import static ru.practicum.ewm.place.model.dto.PlaceMapper.placeToFullDto;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepository placeRepository;

    @Override
    public PlaceFullDto addPlace(NewPlaceDto newPlaceDto) {
        try {
            Place place = placeRepository.save(placeFromNewPlaceDto(newPlaceDto));
            log.info("Добавлено место {}", place);
            return placeToFullDto(place);
        } catch (DataIntegrityViolationException e) {
            throw new PlaceAlreadyExistsException(
                    "Место с названием '" + newPlaceDto.getName() + "' уже существует"
            );
        }
    }

    @Override
    public void deletePlace(int placeId) {
        if (!placeRepository.existsById(placeId))
            throw new PlaceNotFoundException("Место с id=" + placeId + " не найдено");
        placeRepository.deleteById(placeId);
        log.info("Удалено место с id={}", placeId);
    }

    @Override
    public PlaceFullDto updatePlace(PlaceUpdateRequest request) {
        int placeId = request.getId();
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new PlaceNotFoundException("Место с id=" + placeId + " не найдено"));
        String name = request.getName();
        String description = request.getDescription();
        Location location = request.getLocation();
        Double radius = request.getRadius();
        if (name != null) {
            if (placeRepository.existsByNameAndIdNot(name, placeId))
                throw new PlaceAlreadyExistsException("Место с названием '" + name + "' уже существует");
            place.setName(name);
        }
        if (description != null) place.setDescription(description);
        if (location != null) {
            place.setLon(location.getLon());
            place.setLat(location.getLat());
        }
        if (radius != null) place.setRadius(radius);
        placeRepository.save(place);
        log.info("Обновлено место {}", place);
        return placeToFullDto(place);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceFullDto getPlaceById(int placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new PlaceNotFoundException("Место с id=" + placeId + " не найдено"));
        log.info("Запрошено место {}", place);
        return placeToFullDto(place);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaceShortDto> getPlaces(String text, int from, int size) {
        log.info("Запрошен список мест");
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        return placeRepository.findPlaces(text, pageable).stream()
                .map(PlaceMapper::placeToShortDto)
                .collect(Collectors.toList());
    }
}