package dev.simakogan.artstudio.repo;

import dev.simakogan.artstudio.domain.StudioClass;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudioClassRepository extends JpaRepository<StudioClass, Long> {

    List<StudioClass> findByActiveTrueAndStartsAtAfterOrderByStartsAt(LocalDateTime after);

    List<StudioClass> findAllByOrderByStartsAtDesc();

    /** Row lock so two concurrent bookings cannot both take the last seat. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from StudioClass c where c.id = :id")
    Optional<StudioClass> findByIdForUpdate(@Param("id") Long id);
}
