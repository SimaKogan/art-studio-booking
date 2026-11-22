package dev.simakogan.artstudio.dto;

import dev.simakogan.artstudio.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/** All request/response shapes of the public API. Money is always in cents. */
public final class Dtos {
    private Dtos() {}

    // ---- auth
    public record RegisterRequest(@NotBlank @Email String email,
                                  @NotBlank @Size(min = 8, max = 72) String password,
                                  @NotBlank @Size(max = 80) String fullName) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record UserDto(Long id, String email, String fullName, Role role) {}

    public record AuthResponse(String token, UserDto user) {}

    // ---- classes
    public record ClassDto(Long id, String title, String description, Category category, Level level,
                           String instructor, String location, LocalDateTime startsAt, int durationMinutes,
                           int priceCents, int capacity, int seatsLeft, boolean active) {}

    public record ClassRequest(@NotBlank @Size(max = 120) String title,
                               @NotBlank @Size(max = 2000) String description,
                               @NotNull Category category,
                               @NotNull Level level,
                               @NotBlank String instructor,
                               @NotBlank String location,
                               @NotNull LocalDateTime startsAt,
                               @Min(15) @Max(480) int durationMinutes,
                               @Min(0) int priceCents,
                               @Min(1) @Max(100) int capacity,
                               Boolean active) {}

    // ---- bookings
    public record BookingRequest(@NotNull Long classId, @Min(1) @Max(6) int seats) {}

    public record BookingDto(Long id, Long classId, String classTitle, Category category,
                             LocalDateTime startsAt, String location, int seats, int totalCents,
                             BookingStatus status, Instant createdAt, Instant holdExpiresAt,
                             String userName, String userEmail) {}

    // ---- payments (stub)
    public record PaymentRequest(@NotNull Long bookingId,
                                 @NotBlank @Size(max = 80) String cardholder,
                                 @NotBlank @Size(min = 12, max = 23) String cardNumber,
                                 @NotBlank @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Use MM/YY") String expiry,
                                 @NotBlank @Pattern(regexp = "^\\d{3,4}$", message = "3 or 4 digits") String cvc) {}

    public record PaymentDto(Long id, Long bookingId, PaymentStatus status, int amountCents, String reference,
                             String cardBrand, String last4, String message, BookingStatus bookingStatus) {}

    // ---- assistant
    public record ChatTurn(@NotBlank String role, @NotBlank @Size(max = 1000) String text) {}

    public record ChatRequest(@NotBlank @Size(max = 500) String message,
                              @Size(max = 12) List<@Valid ChatTurn> history) {}

    public record ChatResponse(String reply, List<ClassDto> suggestions, String provider) {}

    // ---- admin stats
    public record ClassStat(Long classId, String title, Category category, LocalDateTime startsAt,
                            int capacity, int booked, int revenueCents) {}

    public record CategoryStat(Category category, int bookings, int revenueCents) {}

    public record StatsDto(int revenueCents, long confirmedBookings, long pendingBookings, long upcomingClasses,
                           double averageOccupancy, long users, List<ClassStat> classes,
                           List<CategoryStat> categories) {}
}
