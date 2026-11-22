package dev.simakogan.artstudio.service;

import dev.simakogan.artstudio.config.AppProperties;
import dev.simakogan.artstudio.domain.Booking;
import dev.simakogan.artstudio.domain.BookingStatus;
import dev.simakogan.artstudio.domain.Payment;
import dev.simakogan.artstudio.domain.PaymentStatus;
import dev.simakogan.artstudio.domain.StudioClass;
import dev.simakogan.artstudio.dto.Dtos.BookingDto;
import dev.simakogan.artstudio.dto.Dtos.BookingRequest;
import dev.simakogan.artstudio.repo.BookingRepository;
import dev.simakogan.artstudio.repo.PaymentRepository;
import dev.simakogan.artstudio.repo.StudioClassRepository;
import dev.simakogan.artstudio.repo.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Booking lifecycle: PENDING (seat hold, expires) -> CONFIRMED (after payment) -> CANCELLED.
 * Capacity is enforced under a row lock on the class.
 */
@Service
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookings;
    private final StudioClassRepository classes;
    private final UserRepository users;
    private final PaymentRepository payments;
    private final PaymentGateway gateway;
    private final Duration hold;
    private final Duration freeCancellation;

    public BookingService(BookingRepository bookings, StudioClassRepository classes, UserRepository users,
                          PaymentRepository payments, PaymentGateway gateway, AppProperties props) {
        this.bookings = bookings;
        this.classes = classes;
        this.users = users;
        this.payments = payments;
        this.gateway = gateway;
        this.hold = Duration.ofMinutes(props.booking().holdMinutes());
        this.freeCancellation = Duration.ofHours(props.booking().freeCancellationHours());
    }

    @Transactional
    public BookingDto create(Long userId, BookingRequest req) {
        expireStaleHolds();
        StudioClass c = classes.findByIdForUpdate(req.classId()).orElseThrow(() -> Errors.notFound("Class"));
        if (!c.isActive() || !c.getStartsAt().isAfter(LocalDateTime.now())) {
            throw Errors.badRequest("This class is no longer open for booking");
        }
        if (bookings.existsByUserIdAndStudioClassIdAndStatusIn(userId, c.getId(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED))) {
            throw Errors.conflict("You already have a booking for this class");
        }
        int taken = (int) bookings.seatsTaken(c.getId(), BookingStatus.CONFIRMED, BookingStatus.PENDING,
                Instant.now().minus(hold));
        int left = c.getCapacity() - taken;
        if (req.seats() > left) {
            throw Errors.conflict(left <= 0 ? "This class is full" : "Only " + left + " seat(s) left");
        }
        Booking b = bookings.save(new Booking(users.getReferenceById(userId), c, req.seats()));
        return Mappers.booking(b, hold);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> mine(Long userId) {
        return bookings.findForUser(userId).stream().map(b -> Mappers.booking(b, hold)).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDto> all() {
        return bookings.findAllWithDetails().stream().map(b -> Mappers.booking(b, hold)).toList();
    }

    @Transactional
    public BookingDto cancel(Long userId, boolean isAdmin, Long bookingId) {
        Booking b = bookings.findByIdForUpdate(bookingId).orElseThrow(() -> Errors.notFound("Booking"));
        if (!isAdmin && !b.getUser().getId().equals(userId)) {
            throw Errors.notFound("Booking");
        }
        if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.EXPIRED) {
            throw Errors.conflict("This booking is already closed");
        }
        if (b.getStatus() == BookingStatus.CONFIRMED) {
            boolean tooLate = LocalDateTime.now().plus(freeCancellation).isAfter(b.getStudioClass().getStartsAt());
            if (tooLate && !isAdmin) {
                throw Errors.conflict("Free cancellation ends " + freeCancellation.toHours() + " hours before the class");
            }
            payments.findFirstByBookingIdAndStatus(b.getId(), PaymentStatus.SUCCEEDED).ifPresent(p -> {
                gateway.refund(p.getReference());
                p.setStatus(PaymentStatus.REFUNDED);
            });
        }
        b.setStatus(BookingStatus.CANCELLED);
        return Mappers.booking(b, hold);
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireStaleHolds() {
        int n = bookings.expireHolds(BookingStatus.PENDING, BookingStatus.EXPIRED, Instant.now().minus(hold));
        if (n > 0) log.info("Released {} expired seat hold(s)", n);
    }
}
