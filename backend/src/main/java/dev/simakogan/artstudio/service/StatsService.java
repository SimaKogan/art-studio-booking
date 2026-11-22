package dev.simakogan.artstudio.service;

import dev.simakogan.artstudio.domain.Booking;
import dev.simakogan.artstudio.domain.BookingStatus;
import dev.simakogan.artstudio.domain.Category;
import dev.simakogan.artstudio.domain.Payment;
import dev.simakogan.artstudio.domain.PaymentStatus;
import dev.simakogan.artstudio.domain.StudioClass;
import dev.simakogan.artstudio.dto.Dtos.CategoryStat;
import dev.simakogan.artstudio.dto.Dtos.ClassStat;
import dev.simakogan.artstudio.dto.Dtos.StatsDto;
import dev.simakogan.artstudio.repo.BookingRepository;
import dev.simakogan.artstudio.repo.PaymentRepository;
import dev.simakogan.artstudio.repo.StudioClassRepository;
import dev.simakogan.artstudio.repo.UserRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aggregates for the admin dashboard. Computed in memory: fine at studio scale, swap for SQL if it grows. */
@Service
public class StatsService {
    private final BookingRepository bookings;
    private final PaymentRepository payments;
    private final StudioClassRepository classes;
    private final UserRepository users;

    public StatsService(BookingRepository bookings, PaymentRepository payments, StudioClassRepository classes,
                        UserRepository users) {
        this.bookings = bookings;
        this.payments = payments;
        this.classes = classes;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public StatsDto overview() {
        List<Booking> confirmed = bookings.findByStatusWithClass(BookingStatus.CONFIRMED);
        List<Payment> paid = payments.findByStatusWithBooking(PaymentStatus.SUCCEEDED);

        Map<Long, Integer> seatsByClass = new HashMap<>();
        Map<Long, Integer> revenueByClass = new HashMap<>();
        Map<Category, int[]> byCategory = new EnumMap<>(Category.class); // [bookings, revenue]
        for (Booking b : confirmed) {
            seatsByClass.merge(b.getStudioClass().getId(), b.getSeats(), Integer::sum);
        }
        int revenue = 0;
        for (Payment p : paid) {
            StudioClass c = p.getBooking().getStudioClass();
            revenueByClass.merge(c.getId(), p.getAmountCents(), Integer::sum);
            revenue += p.getAmountCents();
        }
        for (Booking b : confirmed) {
            int[] agg = byCategory.computeIfAbsent(b.getStudioClass().getCategory(), k -> new int[2]);
            agg[0] += 1;
        }
        for (Payment p : paid) {
            byCategory.computeIfAbsent(p.getBooking().getStudioClass().getCategory(), k -> new int[2])[1] += p.getAmountCents();
        }

        LocalDateTime now = LocalDateTime.now();
        List<StudioClass> all = classes.findAll();
        List<StudioClass> upcoming = all.stream().filter(c -> c.isActive() && c.getStartsAt().isAfter(now)).toList();
        double occupancy = upcoming.stream()
                .mapToDouble(c -> c.getCapacity() == 0 ? 0 : (double) seatsByClass.getOrDefault(c.getId(), 0) / c.getCapacity())
                .average().orElse(0);

        List<ClassStat> classStats = all.stream()
                .filter(c -> seatsByClass.containsKey(c.getId()) || upcoming.contains(c))
                .sorted(Comparator.comparing(StudioClass::getStartsAt))
                .map(c -> new ClassStat(c.getId(), c.getTitle(), c.getCategory(), c.getStartsAt(), c.getCapacity(),
                        seatsByClass.getOrDefault(c.getId(), 0), revenueByClass.getOrDefault(c.getId(), 0)))
                .toList();

        List<CategoryStat> catStats = byCategory.entrySet().stream()
                .map(e -> new CategoryStat(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparingInt(CategoryStat::revenueCents).reversed())
                .toList();

        return new StatsDto(revenue, confirmed.size(), bookings.countByStatus(BookingStatus.PENDING),
                upcoming.size(), Math.round(occupancy * 1000) / 1000.0, users.count(), classStats, catStats);
    }
}
