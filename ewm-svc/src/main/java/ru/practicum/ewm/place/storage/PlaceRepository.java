package ru.practicum.ewm.place.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.place.model.Place;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Integer> {
    Boolean existsByNameAndIdNot(String name, int id);

    @Query("SELECT p " +
            "FROM Place AS p " +
            "WHERE ((lower(p.name) LIKE lower(concat('%', :text, '%'))) " +
            "OR (lower(p.description) LIKE lower(concat('%', :text, '%'))) OR (:text = null))")
    List<Place> findPlaces(
            String text,
            Pageable pageable
    );
}