package dev.simakogan.artstudio.service;

import dev.simakogan.artstudio.domain.Booking;
import dev.simakogan.artstudio.domain.BookingStatus;
import dev.simakogan.artstudio.domain.StudioClass;
import dev.simakogan.artstudio.domain.User;
import dev.simakogan.artstudio.dto.Dtos.BookingDto;
import dev.simakogan.artstudio.dto.Dtos.ClassDto;
import dev.simakogan.artstudio.dto.Dtos.UserDto;
import java.time.Duration;

final class Mappers {
    private Mappers() {}

    static UserDto user(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getFullName(), u.getRole());
    }

    static ClassDto cls(StudioClass c, int seatsLeft) {
        return new ClassDto(c.getId(), c.getTitle(), c.getDescription(), c.getCategory(), c.getLevel(),
                c.getInstructor(), c.getLocation(), c.getStartsAt(), c.getDurationMinutes(),
                c.getPriceCents(), c.getCapacity(), Math.max(0, seatsLeft), c.isActive());
    }

    static BookingDto booking(Booking b, Duration hold) {
        StudioClass c = b.getStudioClass();
        return new BookingDto(b.getId(), c.getId(), c.getTitle(), c.getCategory(), c.getStartsAt(), c.getLocation(),
                b.getSeats(), b.getTotalCents(), b.getStatus(), b.getCreatedAt(),
                b.getStatus() == BookingStatus.PENDING ? b.getCreatedAt().plus(hold) : null,
                b.getUser().getFullName(), b.getUser().getEmail());
    }
}
