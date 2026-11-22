package dev.simakogan.artstudio.repo;

import dev.simakogan.artstudio.domain.Booking;
import dev.simakogan.artstudio.domain.BookingStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Seats currently taken: confirmed bookings plus pending holds that have not expired. */
    @Query("""
            select coalesce(sum(b.seats), 0) from Booking b
            where b.studioClass.id = :classId
              and (b.status = :confirmed or (b.status = :pending and b.createdAt > :cutoff))
            """)
    long seatsTaken(@Param("classId") Long classId,
                   @Param("confirmed") BookingStatus confirmed,
                   @Param("pending") BookingStatus pending,
                   @Param("cutoff") Instant cutoff);

    @Query("""
            select b.studioClass.id, coalesce(sum(b.seats), 0) from Booking b
            where b.studioClass.id in :ids
              and (b.status = :confirmed or (b.status = :pending and b.createdAt > :cutoff))
            group by b.studioClass.id
            """)
    List<Object[]> seatsTakenByClass(@Param("ids") Collection<Long> ids,
                                     @Param("confirmed") BookingStatus confirmed,
                                     @Param("pending") BookingStatus pending,
                                     @Param("cutoff") Instant cutoff);

    @Query("select b from Booking b join fetch b.studioClass join fetch b.user "
            + "where b.user.id = :userId order by b.createdAt desc")
    List<Booking> findForUser(@Param("userId") Long userId);

    @Query("select b from Booking b join fetch b.studioClass join fetch b.user order by b.createdAt desc")
    List<Booking> findAllWithDetails();

    @Query("select b from Booking b join fetch b.studioClass where b.status = :status")
    List<Booking> findByStatusWithClass(@Param("status") BookingStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b join fetch b.studioClass where b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);

    boolean existsByUserIdAndStudioClassIdAndStatusIn(Long userId, Long classId, Collection<BookingStatus> statuses);

    @Modifying
    @Query("update Booking b set b.status = :expired where b.status = :pending and b.createdAt < :cutoff")
    int expireHolds(@Param("pending") BookingStatus pending,
                    @Param("expired") BookingStatus expired,
                    @Param("cutoff") Instant cutoff);

    long countByStatus(BookingStatus status);
}
