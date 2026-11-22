package dev.simakogan.artstudio.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** A single scheduled session. Times are studio-local (no zone conversion). */
@Entity
@Table(name = "studio_classes")
public class StudioClass {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Level level;

    @Column(nullable = false)
    private String instructor;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    private int durationMinutes;
    private int priceCents;
    private int capacity;
    private boolean active = true;

    protected StudioClass() {}

    public StudioClass(String title, String description, Category category, Level level, String instructor,
                       String location, LocalDateTime startsAt, int durationMinutes, int priceCents, int capacity) {
        apply(title, description, category, level, instructor, location, startsAt, durationMinutes, priceCents, capacity);
    }

    public void apply(String title, String description, Category category, Level level, String instructor,
                      String location, LocalDateTime startsAt, int durationMinutes, int priceCents, int capacity) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.level = level;
        this.instructor = instructor;
        this.location = location;
        this.startsAt = startsAt;
        this.durationMinutes = durationMinutes;
        this.priceCents = priceCents;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Level getLevel() { return level; }
    public String getInstructor() { return instructor; }
    public String getLocation() { return location; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getPriceCents() { return priceCents; }
    public int getCapacity() { return capacity; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
