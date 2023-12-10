package ru.practicum.ewm.compilation.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.compilation.model.Compilation;

import java.util.List;

public interface CompilationStorage extends JpaRepository<Compilation, Integer> {
    List<Compilation> findByPinned(Boolean pinned, Pageable pageable);
}