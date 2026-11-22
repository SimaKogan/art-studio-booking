package dev.simakogan.artstudio.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Result of a (simulated) charge. Only the last four card digits are ever stored. */
@Entity
@Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Booking booking;

    private int amountCents;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private PaymentStatus status;

    private String reference;
    private String cardBrand;
    private String last4;
    private String message;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Payment() {}

    public Payment(Booking booking, int amountCents, PaymentStatus status, String reference,
                   String cardBrand, String last4, String message) {
        this.booking = booking;
        this.amountCents = amountCents;
        this.status = status;
        this.reference = reference;
        this.cardBrand = cardBrand;
        this.last4 = last4;
        this.message = message;
    }

    public Long getId() { return id; }
    public Booking getBooking() { return booking; }
    public int getAmountCents() { return amountCents; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getReference() { return reference; }
    public String getCardBrand() { return cardBrand; }
    public String getLast4() { return last4; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
