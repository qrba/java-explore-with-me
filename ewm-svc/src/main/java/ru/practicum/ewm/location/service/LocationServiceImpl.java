package ru.practicum.ewm.location.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Coordinate;
import ru.practicum.ewm.exception.LocationAlreadyExistsException;
import ru.practicum.ewm.exception.LocationNotFoundException;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.model.dto.NewLocationDto;
import ru.practicum.ewm.location.model.dto.LocationFullDto;
import ru.practicum.ewm.location.model.dto.LocationMapper;
import ru.practicum.ewm.location.model.dto.LocationShortDto;
import ru.practicum.ewm.location.model.dto.LocationUpdateRequest;
import ru.practicum.ewm.location.storage.LocationRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.location.model.dto.LocationMapper.locationFromNewLocationDto;
import static ru.practicum.ewm.location.model.dto.LocationMapper.locationToFullDto;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;

    @Override
    public LocationFullDto addLocation(NewLocationDto newLocationDto) {
        try {
            Location location = locationRepository.save(locationFromNewLocationDto(newLocationDto));
            log.info("Добавлена локация {}", location);
            return locationToFullDto(location);
        } catch (DataIntegrityViolationException e) {
            throw new LocationAlreadyExistsException(
                    "Локация с названием '" + newLocationDto.getName() + "' уже существует"
            );
        }
    }

    @Override
    public void deleteLocation(int locationId) {
        if (!locationRepository.existsById(locationId))
            throw new LocationNotFoundException("Локация с id=" + locationId + " не найдена");
        locationRepository.deleteById(locationId);
        log.info("Удалена локация с id={}", locationId);
    }

    @Override
    public LocationFullDto updateLocation(LocationUpdateRequest request) {
        int locationId = request.getId();
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException("Локация с id=" + locationId + " не найдена"));
        String name = request.getName();
        String description = request.getDescription();
        Coordinate coordinate = request.getCoordinate();
        Double radius = request.getRadius();
        if (name != null) {
            if (locationRepository.existsByNameAndIdNot(name, locationId))
                throw new LocationAlreadyExistsException("Локация с названием '" + name + "' уже существует");
            location.setName(name);
        }
        if (description != null) location.setDescription(description);
        if (coordinate != null) {
            location.setLon(coordinate.getLon());
            location.setLat(coordinate.getLat());
        }
        if (radius != null) location.setRadius(radius);
        locationRepository.save(location);
        log.info("Обновлена локация {}", location);
        return locationToFullDto(location);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationFullDto getLocationById(int locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException("Локация с id=" + locationId + " не найдена"));
        log.info("Запрошена локация {}", location);
        return locationToFullDto(location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationShortDto> getLocations(String text, int from, int size) {
        log.info("Запрошен список локаций");
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        return locationRepository.findLocations(text, pageable).stream()
                .map(LocationMapper::locationToShortDto)
                .collect(Collectors.toList());
    }
}