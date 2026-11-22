package dev.simakogan.artstudio.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Payment stub. Nothing leaves the process and nothing real is charged.
 * <ul>
 *   <li>Any Luhn-valid number with a future expiry is approved, e.g. 4242 4242 4242 4242.</li>
 *   <li>Numbers ending in 0002 are declined, e.g. 4000 0000 0000 0002.</li>
 *   <li>Invalid numbers and expired cards are declined with a reason.</li>
 * </ul>
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public ChargeResult charge(ChargeRequest r) {
        String digits = r.cardNumber() == null ? "" : r.cardNumber().replaceAll("[\\s-]", "");
        String last4 = digits.length() >= 4 ? digits.substring(digits.length() - 4) : "";
        String brand = brand(digits);

        if (!digits.matches("\\d{13,19}") || !luhn(digits)) {
            return declined(brand, last4, "Card number is not valid");
        }
        if (isExpired(r.expiry())) {
            return declined(brand, last4, "Card has expired");
        }
        if (digits.endsWith("0002")) {
            return declined(brand, last4, "Card declined by the issuer (test card)");
        }
        return new ChargeResult(true, "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                brand, last4, "Payment approved");
    }

    @Override
    public void refund(String reference) {
        // Nothing to call in the stub. A real gateway would issue a refund for `reference` here.
    }

    private static ChargeResult declined(String brand, String last4, String message) {
        return new ChargeResult(false, null, brand, last4, message);
    }

    static boolean isExpired(String expiry) {
        try {
            String[] p = expiry.split("/");
            YearMonth exp = YearMonth.of(2000 + Integer.parseInt(p[1]), Integer.parseInt(p[0]));
            return exp.isBefore(YearMonth.from(LocalDate.now()));
        } catch (RuntimeException e) {
            return true;
        }
    }

    static String brand(String digits) {
        if (digits.startsWith("4")) return "Visa";
        if (digits.matches("^(5[1-5]|2[2-7]).*")) return "Mastercard";
        if (digits.matches("^3[47].*")) return "Amex";
        return "Card";
    }

    static boolean luhn(String digits) {
        int sum = 0;
        boolean dbl = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            dbl = !dbl;
        }
        return sum % 10 == 0;
    }
}
