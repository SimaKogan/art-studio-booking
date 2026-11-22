package dev.simakogan.artstudio.repo;

import dev.simakogan.artstudio.domain.Payment;
import dev.simakogan.artstudio.domain.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findFirstByBookingIdAndStatus(Long bookingId, PaymentStatus status);

    @Query("select p from Payment p join fetch p.booking b join fetch b.studioClass where p.status = :status")
    List<Payment> findByStatusWithBooking(@Param("status") PaymentStatus status);
}
