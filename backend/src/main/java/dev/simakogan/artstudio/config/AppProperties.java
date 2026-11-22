package dev.simakogan.artstudio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        boolean seedDemoData,
        String corsOrigins,
        Jwt jwt,
        Booking booking,
        Ai ai) {

    public record Jwt(String secret, long ttlMinutes) {}

    public record Booking(long holdMinutes, long freeCancellationHours) {}

    public record Ai(String provider, Vertex vertex) {}

    public record Vertex(String projectId, String location, String model) {}
}
