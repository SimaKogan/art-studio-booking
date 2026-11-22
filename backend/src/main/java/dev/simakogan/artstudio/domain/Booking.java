package dev.simakogan.artstudio.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private StudioClass studioClass;

    private int seats;
    private int totalCents;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Booking() {}

    public Booking(User user, StudioClass studioClass, int seats) {
        this.user = user;
        this.studioClass = studioClass;
        this.seats = seats;
        this.totalCents = seats * studioClass.getPriceCents();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public StudioClass getStudioClass() { return studioClass; }
    public int getSeats() { return seats; }
    public int getTotalCents() { return totalCents; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
