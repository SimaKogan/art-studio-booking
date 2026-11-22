package dev.simakogan.artstudio.web;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookingFlowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(body).get("token").asText();
    }

    private long createClass(String adminToken, int capacity) throws Exception {
        Map<String, Object> req = Map.of(
                "title", "Test class", "description", "Created by a test", "category", "PAINTING",
                "level", "ALL_LEVELS", "instructor", "Tester", "location", "Studio",
                "startsAt", LocalDateTime.of(LocalDate.now().plusDays(10), java.time.LocalTime.of(18, 0)).toString(),
                "durationMinutes", 90, "priceCents", 15000, "capacity", capacity);
        String body = mvc.perform(post("/api/admin/classes").header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asLong();
    }

    private String payload(Object o) throws Exception {
        return json.writeValueAsString(o);
    }

    @Test
    void catalogueIsPublic() throws Exception {
        mvc.perform(get("/api/classes")).andExpect(status().isOk()).andExpect(jsonPath("$.length()", greaterThan(3)));
    }

    @Test
    void bookingsRequireLogin() throws Exception {
        mvc.perform(get("/api/bookings/my")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminAreaIsAdminOnly() throws Exception {
        String user = login("demo@atelier.local", "Demo12345!");
        String admin = login("admin@atelier.local", "Admin123!");
        mvc.perform(get("/api/admin/stats").header("Authorization", user)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/stats").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revenueCents", greaterThan(0)));
    }

    @Test
    void bookDeclinedThenPaid() throws Exception {
        String admin = login("admin@atelier.local", "Admin123!");
        String user = login("demo@atelier.local", "Demo12345!");
        long classId = createClass(admin, 5);

        String created = mvc.perform(post("/api/bookings").header("Authorization", user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(Map.of("classId", classId, "seats", 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalCents").value(30000))
                .andReturn().getResponse().getContentAsString();
        long bookingId = json.readTree(created).get("id").asLong();

        // same user, same class: rejected
        mvc.perform(post("/api/bookings").header("Authorization", user).contentType(MediaType.APPLICATION_JSON)
                        .content(payload(Map.of("classId", classId, "seats", 1))))
                .andExpect(status().isConflict());

        Map<String, Object> card = Map.of("bookingId", bookingId, "cardholder", "Demo Student",
                "cardNumber", "4000 0000 0000 0002", "expiry", "12/40", "cvc", "123");
        mvc.perform(post("/api/payments/checkout").header("Authorization", user)
                        .contentType(MediaType.APPLICATION_JSON).content(payload(card)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.bookingStatus").value("PENDING"));

        Map<String, Object> good = Map.of("bookingId", bookingId, "cardholder", "Demo Student",
                "cardNumber", "4242 4242 4242 4242", "expiry", "12/40", "cvc", "123");
        mvc.perform(post("/api/payments/checkout").header("Authorization", user)
                        .contentType(MediaType.APPLICATION_JSON).content(payload(good)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.last4").value("4242"));

        mvc.perform(get("/api/classes/" + classId))
                .andExpect(jsonPath("$.seatsLeft").value(3));
    }

    @Test
    void cannotOverbook() throws Exception {
        String admin = login("admin@atelier.local", "Admin123!");
        String user = login("demo@atelier.local", "Demo12345!");
        long classId = createClass(admin, 2);
        mvc.perform(post("/api/bookings").header("Authorization", user).contentType(MediaType.APPLICATION_JSON)
                        .content(payload(Map.of("classId", classId, "seats", 3))))
                .andExpect(status().isConflict());
    }

    @Test
    void assistantAnswersWithBookableSuggestions() throws Exception {
        String body = mvc.perform(post("/api/assistant/chat").contentType(MediaType.APPLICATION_JSON)
                        .content(payload(Map.of("message", "beginner watercolor class please"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("local"))
                .andExpect(jsonPath("$.suggestions", hasSize(greaterThan(0))))
                .andReturn().getResponse().getContentAsString();
        JsonNode first = json.readTree(body).get("suggestions").get(0);
        org.assertj.core.api.Assertions.assertThat(first.get("category").asText()).isEqualTo("WATERCOLOR");
    }

    @Test
    void validationErrorsExplainThemselves() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(payload(Map.of("email", "not-an-email", "password", "short", "fullName", "X"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }
}
