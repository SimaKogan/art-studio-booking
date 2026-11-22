package dev.simakogan.artstudio.seed;

import dev.simakogan.artstudio.domain.*;
import dev.simakogan.artstudio.repo.BookingRepository;
import dev.simakogan.artstudio.repo.PaymentRepository;
import dev.simakogan.artstudio.repo.StudioClassRepository;
import dev.simakogan.artstudio.repo.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fictional demo data for a fictional studio ("Atelier Nahal"). Runs only on an empty database.
 * Accounts: admin@atelier.local / Admin123!   demo@atelier.local / Demo12345!
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final String HALL = "Atelier Nahal, 7 Indigo Lane, Rivertown";

    private final UserRepository users;
    private final StudioClassRepository classes;
    private final BookingRepository bookings;
    private final PaymentRepository payments;
    private final PasswordEncoder encoder;

    public DemoDataSeeder(UserRepository users, StudioClassRepository classes, BookingRepository bookings,
                          PaymentRepository payments, PasswordEncoder encoder) {
        this.users = users;
        this.classes = classes;
        this.bookings = bookings;
        this.payments = payments;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) return;

        users.save(new User("admin@atelier.local", encoder.encode("Admin123!"), "Studio Admin", Role.ADMIN));
        users.save(new User("demo@atelier.local", encoder.encode("Demo12345!"), "Demo Student", Role.USER));
        String studentHash = encoder.encode("Student12345!");
        List<User> students = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            students.add(users.save(new User("student" + i + "@example.com", studentHash, "Student " + i, Role.USER)));
        }

        LocalDate d = LocalDate.now();
        List<StudioClass> list = classes.saveAll(List.of(
                c("Oil Painting: Light and Shadow", "Build a still life in layers. You will learn to mix a limited palette, block in values and finish with glazes. All materials included.",
                        Category.PAINTING, Level.BEGINNER, "Dana Mizrahi", d.plusDays(2), 18, 0, 150, 220, 10),
                c("Watercolor Wet-on-Wet", "Soft skies, loose florals and how to control water. Paper and paints are provided; bring a small brush if you have one.",
                        Category.WATERCOLOR, Level.BEGINNER, "Yael Stern", d.plusDays(3), 10, 0, 120, 180, 8),
                c("Wheel Throwing: First Bowl", "Center the clay, open, pull walls and shape your first bowl. Pieces are trimmed, glazed and fired by us; pick up in two weeks.",
                        Category.CERAMICS, Level.BEGINNER, "Omer Katz", d.plusDays(4), 19, 0, 180, 280, 6),
                c("Figure Drawing with Charcoal", "Short and long poses from a live model. Focus on gesture, proportion and tonal shading.",
                        Category.DRAWING, Level.INTERMEDIATE, "Dana Mizrahi", d.plusDays(5), 19, 30, 120, 160, 12),
                c("Procreate Illustration Basics", "Brushes, layers and a clean workflow on iPad. Finish a small character illustration. iPads are available to borrow.",
                        Category.ILLUSTRATION, Level.BEGINNER, "Tal Ben-David", d.plusDays(6), 17, 0, 150, 200, 10),
                c("Little Artists: Animal Paintings", "A relaxed morning of acrylics for ages 6 to 10. Aprons provided, parents welcome to stay.",
                        Category.KIDS, Level.ALL_LEVELS, "Yael Stern", d.plusDays(8), 10, 30, 90, 110, 12),
                c("Acrylic Pour Evening", "Fluid art with swirling colour on a 30x30 cm canvas. No experience needed, takes home a finished piece.",
                        Category.PAINTING, Level.ALL_LEVELS, "Tal Ben-David", d.plusDays(9), 19, 0, 120, 190, 14),
                c("Handbuilding: Planters and Mugs", "Pinch, coil and slab techniques for useful little objects. Glazing session included.",
                        Category.CERAMICS, Level.ALL_LEVELS, "Omer Katz", d.plusDays(11), 11, 0, 180, 260, 8),
                c("Urban Sketching Walk", "Draw the old city on location with pen and a touch of watercolor. Meet at the studio, finish with coffee.",
                        Category.DRAWING, Level.ALL_LEVELS, "Yael Stern", d.plusDays(13), 9, 30, 150, 90, 12),
                c("Watercolor Botanicals", "Layering, wet edges and lifting to paint leaves and petals with botanical accuracy.",
                        Category.WATERCOLOR, Level.INTERMEDIATE, "Yael Stern", d.plusDays(16), 18, 0, 150, 200, 8),
                c("Portrait in Oils", "Proportion, skin tones and edges in a single-session portrait study from a reference photo.",
                        Category.PAINTING, Level.INTERMEDIATE, "Dana Mizrahi", d.plusDays(18), 18, 30, 180, 260, 8)));

        // Some confirmed history so occupancy and revenue charts are not empty.
        Random rnd = new Random(42);
        for (StudioClass c : list) {
            int target = (int) Math.round(c.getCapacity() * (0.25 + rnd.nextDouble() * 0.55));
            int booked = 0;
            int s = 0;
            while (booked < target && s < students.size()) {
                int seats = Math.min(1 + rnd.nextInt(2), target - booked);
                Booking b = new Booking(students.get(s++), c, seats);
                b.setStatus(BookingStatus.CONFIRMED);
                b.setCreatedAt(Instant.now().minus(1 + rnd.nextInt(20), ChronoUnit.DAYS));
                bookings.save(b);
                payments.save(new Payment(b, b.getTotalCents(), PaymentStatus.SUCCEEDED,
                        "SEED-" + b.getId(), "Visa", "4242", "Payment approved"));
                booked += seats;
            }
        }
        log.info("Seeded demo data: {} classes, {} users. Admin: admin@atelier.local / Admin123!",
                list.size(), users.count());
    }

    private static StudioClass c(String title, String desc, Category cat, Level level, String teacher,
                                 LocalDate day, int hour, int minute, int minutes, int priceIls, int capacity) {
        return new StudioClass(title, desc, cat, level, teacher, HALL,
                LocalDateTime.of(day, LocalTime.of(hour, minute)), minutes, priceIls * 100, capacity);
    }
}
