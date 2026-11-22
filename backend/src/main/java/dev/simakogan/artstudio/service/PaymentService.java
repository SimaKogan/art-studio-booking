package dev.simakogan.artstudio.service;

import dev.simakogan.artstudio.config.AppProperties;
import dev.simakogan.artstudio.domain.Booking;
import dev.simakogan.artstudio.domain.BookingStatus;
import dev.simakogan.artstudio.domain.Payment;
import dev.simakogan.artstudio.domain.PaymentStatus;
import dev.simakogan.artstudio.dto.Dtos.PaymentDto;
import dev.simakogan.artstudio.dto.Dtos.PaymentRequest;
import dev.simakogan.artstudio.repo.BookingRepository;
import dev.simakogan.artstudio.repo.PaymentRepository;
import dev.simakogan.artstudio.service.PaymentGateway.ChargeRequest;
import dev.simakogan.artstudio.service.PaymentGateway.ChargeResult;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final BookingRepository bookings;
    private final PaymentRepository payments;
    private final PaymentGateway gateway;
    private final Duration hold;

    public PaymentService(BookingRepository bookings, PaymentRepository payments, PaymentGateway gateway,
                          AppProperties props) {
        this.bookings = bookings;
        this.payments = payments;
        this.gateway = gateway;
        this.hold = Duration.ofMinutes(props.booking().holdMinutes());
    }

    /**
     * Charges the card through the gateway. A declined card is a normal outcome (HTTP 200, status FAILED)
     * and keeps the seat hold alive until it expires so the user can retry.
     */
    @Transactional
    public PaymentDto pay(Long userId, PaymentRequest req) {
        Booking booking = bookings.findByIdForUpdate(req.bookingId()).orElseThrow(() -> Errors.notFound("Booking"));
        if (!booking.getUser().getId().equals(userId)) {
            throw Errors.notFound("Booking");
        }
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw Errors.conflict("This booking is already paid");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw Errors.conflict("This booking can no longer be paid");
        }
        if (booking.getCreatedAt().isBefore(Instant.now().minus(hold))) {
            booking.setStatus(BookingStatus.EXPIRED);
            throw Errors.conflict("Your seat hold expired. Please book again.");
        }

        ChargeResult result = gateway.charge(new ChargeRequest(booking.getTotalCents(), req.cardholder(),
                req.cardNumber(), req.expiry(), req.cvc()));

        Payment payment = payments.save(new Payment(booking, booking.getTotalCents(),
                result.approved() ? PaymentStatus.SUCCEEDED : PaymentStatus.FAILED,
                result.reference(), result.brand(), result.last4(), result.message()));
        if (result.approved()) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }
        return new PaymentDto(payment.getId(), booking.getId(), payment.getStatus(), payment.getAmountCents(),
                payment.getReference(), payment.getCardBrand(), payment.getLast4(), payment.getMessage(),
                booking.getStatus());
    }
}
