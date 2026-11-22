package com.artstudio.booking.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Typed view of the {@code app.*} section of application.yml. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @DefaultValue Jwt jwt,
        @DefaultValue Bookings booking,
        @DefaultValue Studio studio,
        @DefaultValue Assistant assistant,
        @DefaultValue Cors cors,
        @DefaultValue Seed seed) {

    public record Jwt(
            @DefaultValue("dev-only-secret-change-me-dev-only-secret-change-me") String secret,
            @DefaultValue("120") long expirationMinutes) {}

    public record Bookings(
            @DefaultValue("15") int holdMinutes,
            @DefaultValue("24") int cancelWindowHours,
            @DefaultValue("6") int maxSeats) {}

    public record Studio(
            @DefaultValue("Atelier Booking") String name,
            @DefaultValue("ILS") String currency,
            @DefaultValue("Asia/Jerusalem") String zone) {}

    public record Assistant(
            @DefaultValue("mock") String provider,
            @DefaultValue("20") int rateLimitPerMinute,
            @DefaultValue Anthropic anthropic,
            @DefaultValue Vertex vertex) {}

    public record Anthropic(
            @DefaultValue("") String apiKey,
            @DefaultValue("claude-haiku-5-5") String model) {}

    public record Vertex(
            @DefaultValue("") String apiKey,
            @DefaultValue("gemini-2.5-flash") String model,
            @DefaultValue("https://aiplatform.googleapis.com/v1") String baseUrl) {}

    public record Cors(@DefaultValue("http://localhost:5173") List<String> allowedOrigins) {}

    public record Seed(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("admin@artstudio.local") String adminEmail,
            @DefaultValue("Admin123!") String adminPassword) {}
}
