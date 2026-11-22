package dev.simakogan.artstudio.service;

/** Seam for a real provider (Stripe, Tranzila, ...). The app only talks to this interface. */
public interface PaymentGateway {

    ChargeResult charge(ChargeRequest request);

    void refund(String reference);

    record ChargeRequest(int amountCents, String cardholder, String cardNumber, String expiry, String cvc) {}

    record ChargeResult(boolean approved, String reference, String brand, String last4, String message) {}
}
