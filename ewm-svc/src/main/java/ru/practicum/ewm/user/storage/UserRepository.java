package ru.practicum.ewm.user.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.user.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u " +
            "FROM User AS u " +
            "WHERE (u.id IN :ids OR :ids = null)")
    List<User> findUsers(List<Integer> ids, Pageable pageable);
}