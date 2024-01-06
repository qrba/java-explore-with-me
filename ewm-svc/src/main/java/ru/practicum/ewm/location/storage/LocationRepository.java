package ru.practicum.ewm.location.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.location.model.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    Boolean existsByNameAndIdNot(String name, int id);

    @Query("SELECT l " +
            "FROM Location AS l " +
            "WHERE ((lower(l.name) LIKE lower(concat('%', :text, '%'))) " +
            "OR (lower(l.description) LIKE lower(concat('%', :text, '%'))) OR (:text = null))")
    List<Location> findLocations(
            String text,
            Pageable pageable
    );
}