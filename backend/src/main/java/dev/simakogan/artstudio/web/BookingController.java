package dev.simakogan.artstudio.web;

import dev.simakogan.artstudio.dto.Dtos.BookingDto;
import dev.simakogan.artstudio.dto.Dtos.BookingRequest;
import dev.simakogan.artstudio.dto.Dtos.PaymentDto;
import dev.simakogan.artstudio.dto.Dtos.PaymentRequest;
import dev.simakogan.artstudio.service.BookingService;
import dev.simakogan.artstudio.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingService bookings;
    private final PaymentService payments;

    public BookingController(BookingService bookings, PaymentService payments) {
        this.bookings = bookings;
        this.payments = payments;
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody BookingRequest req) {
        return bookings.create(userId(jwt), req);
    }

    @GetMapping("/bookings/my")
    public List<BookingDto> mine(@AuthenticationPrincipal Jwt jwt) {
        return bookings.mine(userId(jwt));
    }

    @DeleteMapping("/bookings/{id}")
    public BookingDto cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return bookings.cancel(userId(jwt), false, id);
    }

    /** Payment stub. See MockPaymentGateway for test card numbers. */
    @PostMapping("/payments/checkout")
    public PaymentDto checkout(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PaymentRequest req) {
        return payments.pay(userId(jwt), req);
    }

    private static Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
