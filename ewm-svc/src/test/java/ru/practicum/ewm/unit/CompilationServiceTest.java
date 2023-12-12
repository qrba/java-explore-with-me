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
import org.springframework.http.ResponseEntity;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.dto.CompilationDto;
import ru.practicum.ewm.compilation.model.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.service.CompilationServiceImpl;
import ru.practicum.ewm.compilation.storage.CompilationStorage;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.storage.EventStorage;
import ru.practicum.ewm.exception.CompilationAlreadyExistsException;
import ru.practicum.ewm.exception.CompilationNotFoundException;
import ru.practicum.ewm.participationrequest.storage.ParticipationRequestStorage;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class CompilationServiceTest {
    @Mock
    private CompilationStorage compilationStorage;
    @Mock
    private EventStorage eventStorage;
    @Mock
    private ParticipationRequestStorage requestStorage;
    @Mock
    private StatsClient statsClient;
    @InjectMocks
    private CompilationServiceImpl compilationService;

    private final NewCompilationDto newCompilationDto =
            new NewCompilationDto(false, "title", Collections.emptyList());
    private final Compilation compilation =
            new Compilation(1, newCompilationDto.getPinned(), newCompilationDto.getTitle(), new HashSet<>());
    private final UpdateCompilationRequest updateRequest =
            new UpdateCompilationRequest(false, "title", Collections.emptyList());

    @Test
    public void shouldAddCompilation() {
        Mockito
                .when(compilationStorage.save(any(Compilation.class)))
                .then(returnsFirstArg());

        CompilationDto compilationDto = compilationService.addCompilation(newCompilationDto);

        assertThat(newCompilationDto.getTitle(), equalTo(compilationDto.getTitle()));
        assertThat(newCompilationDto.getPinned(), equalTo(compilationDto.getPinned()));
    }

    @Test
    public void shouldNotAddCompilationWhenNameNotUnique() {
        Mockito
                .when(compilationStorage.save(any(Compilation.class)))
                .thenThrow(DataIntegrityViolationException.class);

        CompilationAlreadyExistsException e = Assertions.assertThrows(
                CompilationAlreadyExistsException.class,
                () -> compilationService.addCompilation(newCompilationDto)
        );

        assertThat(e.getMessage(), equalTo("Подборка с заголовком 'title' уже существует"));
    }

    @Test
    public void shouldDeleteCompilation() {
        Mockito
                .when(compilationStorage.existsById(anyInt()))
                .thenReturn(true);
        compilationService.deleteCompilation(1);
        Mockito.verify(compilationStorage).deleteById(anyInt());
    }

    @Test
    public void shouldNotDeleteCompilationWhenCompilationNotFound() {
        CompilationNotFoundException e = Assertions.assertThrows(
                CompilationNotFoundException.class,
                () -> compilationService.deleteCompilation(1)
        );

        assertThat(e.getMessage(), equalTo("Подборка с id=1 не найдена"));
    }

    @Test
    public void shouldGetCompilations() {
        Mockito
                .when(compilationStorage.findByPinned(anyBoolean(), any(Pageable.class)))
                .thenReturn(List.of(compilation));
        List<CompilationDto> compilationDtoList = compilationService.getCompilations(false, 0, 10);

        assertThat(compilationDtoList.size(), equalTo(1));

        CompilationDto compilationDto = compilationDtoList.get(0);

        assertThat(compilation.getId(), equalTo(compilationDto.getId()));
        assertThat(compilation.getTitle(), equalTo(compilationDto.getTitle()));
        assertThat(compilation.getPinned(), equalTo(compilationDto.getPinned()));
    }

    @Test
    public void shouldGetCompilationById() {
        Mockito
                .when(compilationStorage.findById(anyInt()))
                .thenReturn(Optional.of(compilation));

        CompilationDto compilationDto = compilationService.getCompilationById(1);

        assertThat(compilation.getId(), equalTo(compilationDto.getId()));
        assertThat(compilation.getTitle(), equalTo(compilationDto.getTitle()));
        assertThat(compilation.getPinned(), equalTo(compilationDto.getPinned()));
    }

    @Test
    public void shouldNotGetCompilationByIdWhenCompilationNotFound() {
        Mockito
                .when(compilationStorage.findById(anyInt()))
                .thenReturn(Optional.empty());

        CompilationNotFoundException e = Assertions.assertThrows(
                CompilationNotFoundException.class,
                () -> compilationService.getCompilationById(1)
        );

        assertThat(e.getMessage(), equalTo("Подборка с id=1 не найдена"));
    }

    @Test
    public void shouldUpdateCompilation() {
        Event event = new Event(
                1,
                new User(1, "user@email.com", "name"),
                "a".repeat(21),
                new Category(1, "name"),
                "d".repeat(21),
                LocalDateTime.now().plusDays(1),
                0.0,
                0.0,
                false,
                0,
                false,
                "title",
                LocalDateTime.now().minusDays(1),
                EventState.PUBLISHED,
                LocalDateTime.now().minusHours(1),
                null
        );
        Mockito
                .when(compilationStorage.save(any(Compilation.class)))
                .then(returnsFirstArg());
        Mockito
                .when(compilationStorage.findById(anyInt()))
                .thenReturn(Optional.of(compilation));
        Mockito
                .when(eventStorage.findAllById(anyList()))
                .thenReturn(List.of(event));
        Mockito
                .when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().build());
        CompilationDto updatedCompilationDto = compilationService.updateCompilation(
                new UpdateCompilationRequest(true, "new title", List.of(1)),
                1
        );

        assertThat(compilation.getId(), equalTo(updatedCompilationDto.getId()));
        assertThat(compilation.getTitle(), equalTo(updatedCompilationDto.getTitle()));
        assertThat(compilation.getPinned(), equalTo(updatedCompilationDto.getPinned()));
        assertThat(1, equalTo(updatedCompilationDto.getEvents().get(0).getId()));
    }

    @Test
    public void shouldNotUpdateCompilationWhenCompilationNotFound() {
        Mockito
                .when(compilationStorage.findById(anyInt()))
                .thenReturn(Optional.empty());

        CompilationNotFoundException e = Assertions.assertThrows(
                CompilationNotFoundException.class,
                () -> compilationService.updateCompilation(updateRequest, 1)
        );

        assertThat(e.getMessage(), equalTo("Подборка с id=1 не найдена"));
    }

    @Test
    public void shouldNotUpdateCompilationWhenNameNotUnique() {
        Mockito
                .when(compilationStorage.findById(anyInt()))
                .thenReturn(Optional.of(new Compilation(1, false, "other title", new HashSet<>())));
        Mockito
                .when(compilationStorage.existsByTitle(anyString()))
                .thenReturn(true);

        CompilationAlreadyExistsException e = Assertions.assertThrows(
                CompilationAlreadyExistsException.class,
                () -> compilationService.updateCompilation(updateRequest, 1)
        );

        assertThat(e.getMessage(), equalTo("Подборка с заголовком 'title' уже существует"));
    }
}