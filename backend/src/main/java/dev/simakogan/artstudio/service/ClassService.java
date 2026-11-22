package dev.simakogan.artstudio.service;

import dev.simakogan.artstudio.config.AppProperties;
import dev.simakogan.artstudio.domain.BookingStatus;
import dev.simakogan.artstudio.domain.Category;
import dev.simakogan.artstudio.domain.Level;
import dev.simakogan.artstudio.domain.StudioClass;
import dev.simakogan.artstudio.dto.Dtos.ClassDto;
import dev.simakogan.artstudio.dto.Dtos.ClassRequest;
import dev.simakogan.artstudio.repo.BookingRepository;
import dev.simakogan.artstudio.repo.StudioClassRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassService {
    private final StudioClassRepository classes;
    private final BookingRepository bookings;
    private final Duration hold;

    public ClassService(StudioClassRepository classes, BookingRepository bookings, AppProperties props) {
        this.classes = classes;
        this.bookings = bookings;
        this.hold = Duration.ofMinutes(props.booking().holdMinutes());
    }

    /** Upcoming, bookable classes with optional filters. */
    @Transactional(readOnly = true)
    public List<ClassDto> search(Category category, Level level, String q, Integer maxPriceCents) {
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return upcoming().stream()
                .filter(c -> category == null || c.category() == category)
                .filter(c -> level == null || c.level() == level || c.level() == Level.ALL_LEVELS)
                .filter(c -> maxPriceCents == null || c.priceCents() <= maxPriceCents)
                .filter(c -> needle.isEmpty() || (c.title() + " " + c.description() + " " + c.instructor())
                        .toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    /** All upcoming active classes with live seat availability. */
    @Transactional(readOnly = true)
    public List<ClassDto> upcoming() {
        List<StudioClass> list = classes.findByActiveTrueAndStartsAtAfterOrderByStartsAt(LocalDateTime.now());
        return withAvailability(list);
    }

    @Transactional(readOnly = true)
    public ClassDto get(Long id) {
        StudioClass c = classes.findById(id).orElseThrow(() -> Errors.notFound("Class"));
        int taken = (int) bookings.seatsTaken(id, BookingStatus.CONFIRMED, BookingStatus.PENDING,
                Instant.now().minus(hold));
        return Mappers.cls(c, c.getCapacity() - taken);
    }

    @Transactional(readOnly = true)
    public List<ClassDto> listAllForAdmin() {
        return withAvailability(classes.findAllByOrderByStartsAtDesc());
    }

    @Transactional
    public ClassDto create(ClassRequest r) {
        StudioClass c = new StudioClass(r.title().trim(), r.description().trim(), r.category(), r.level(),
                r.instructor().trim(), r.location().trim(), r.startsAt(), r.durationMinutes(), r.priceCents(),
                r.capacity());
        if (r.active() != null) c.setActive(r.active());
        return Mappers.cls(classes.save(c), r.capacity());
    }

    @Transactional
    public ClassDto update(Long id, ClassRequest r) {
        StudioClass c = classes.findByIdForUpdate(id).orElseThrow(() -> Errors.notFound("Class"));
        int taken = (int) bookings.seatsTaken(id, BookingStatus.CONFIRMED, BookingStatus.PENDING,
                Instant.now().minus(hold));
        if (r.capacity() < taken) {
            throw Errors.conflict("Capacity cannot be lower than the " + taken + " seats already booked");
        }
        c.apply(r.title().trim(), r.description().trim(), r.category(), r.level(), r.instructor().trim(),
                r.location().trim(), r.startsAt(), r.durationMinutes(), r.priceCents(), r.capacity());
        if (r.active() != null) c.setActive(r.active());
        return Mappers.cls(c, c.getCapacity() - taken);
    }

    /** Soft delete: past bookings keep pointing at the class. */
    @Transactional
    public void deactivate(Long id) {
        StudioClass c = classes.findById(id).orElseThrow(() -> Errors.notFound("Class"));
        c.setActive(false);
    }

    private List<ClassDto> withAvailability(Collection<StudioClass> list) {
        if (list.isEmpty()) return List.of();
        List<Long> ids = list.stream().map(StudioClass::getId).toList();
        Map<Long, Integer> taken = new HashMap<>();
        for (Object[] row : bookings.seatsTakenByClass(ids, BookingStatus.CONFIRMED, BookingStatus.PENDING,
                Instant.now().minus(hold))) {
            taken.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return list.stream()
                .map(c -> Mappers.cls(c, c.getCapacity() - taken.getOrDefault(c.getId(), 0)))
                .toList();
    }
}
