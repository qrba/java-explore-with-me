package ru.practicum.ewm.compilation.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.compilation.model.Compilation;

import java.util.List;

public interface CompilationStorage extends JpaRepository<Compilation, Integer> {
    @Query("SELECT c " +
            "FROM Compilation AS c " +
            "WHERE (c.pinned = :pinned OR :pinned = null)")
    List<Compilation> findByPinned(Boolean pinned, Pageable pageable);
}