package dev.simakogan.artstudio.service;

import static org.assertj.core.api.Assertions.assertThat;

import dev.simakogan.artstudio.service.PaymentGateway.ChargeRequest;
import dev.simakogan.artstudio.service.PaymentGateway.ChargeResult;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MockPaymentGatewayTest {
    private final MockPaymentGateway gateway = new MockPaymentGateway();
    private final String futureExpiry = "12/" + (LocalDate.now().getYear() % 100 + 2);

    private ChargeResult charge(String number, String expiry) {
        return gateway.charge(new ChargeRequest(18000, "Test User", number, expiry, "123"));
    }

    @Test
    void approvesValidTestCard() {
        ChargeResult r = charge("4242 4242 4242 4242", futureExpiry);
        assertThat(r.approved()).isTrue();
        assertThat(r.brand()).isEqualTo("Visa");
        assertThat(r.last4()).isEqualTo("4242");
        assertThat(r.reference()).startsWith("MOCK-");
    }

    @Test
    void declinesTheDeclineTestCard() {
        ChargeResult r = charge("4000 0000 0000 0002", futureExpiry);
        assertThat(r.approved()).isFalse();
        assertThat(r.message()).contains("declined");
    }

    @Test
    void rejectsNumbersThatFailLuhn() {
        assertThat(charge("4242 4242 4242 4241", futureExpiry).approved()).isFalse();
    }

    @Test
    void rejectsExpiredCards() {
        ChargeResult r = charge("4242424242424242", "01/20");
        assertThat(r.approved()).isFalse();
        assertThat(r.message()).contains("expired");
    }
}
