package dev.simakogan.artstudio.web;

import dev.simakogan.artstudio.dto.Dtos.BookingDto;
import dev.simakogan.artstudio.dto.Dtos.ClassDto;
import dev.simakogan.artstudio.dto.Dtos.ClassRequest;
import dev.simakogan.artstudio.dto.Dtos.StatsDto;
import dev.simakogan.artstudio.service.BookingService;
import dev.simakogan.artstudio.service.ClassService;
import dev.simakogan.artstudio.service.StatsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Everything under /api/admin requires the ADMIN role (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final ClassService classes;
    private final BookingService bookings;
    private final StatsService stats;

    public AdminController(ClassService classes, BookingService bookings, StatsService stats) {
        this.classes = classes;
        this.bookings = bookings;
        this.stats = stats;
    }

    @GetMapping("/stats")
    public StatsDto stats() {
        return stats.overview();
    }

    @GetMapping("/classes")
    public List<ClassDto> classes() {
        return classes.listAllForAdmin();
    }

    @PostMapping("/classes")
    @ResponseStatus(HttpStatus.CREATED)
    public ClassDto create(@Valid @RequestBody ClassRequest req) {
        return classes.create(req);
    }

    @PutMapping("/classes/{id}")
    public ClassDto update(@PathVariable Long id, @Valid @RequestBody ClassRequest req) {
        return classes.update(id, req);
    }

    @DeleteMapping("/classes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        classes.deactivate(id);
    }

    @GetMapping("/bookings")
    public List<BookingDto> bookings() {
        return bookings.all();
    }

    @DeleteMapping("/bookings/{id}")
    public BookingDto cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return bookings.cancel(Long.valueOf(jwt.getSubject()), true, id);
    }
}
