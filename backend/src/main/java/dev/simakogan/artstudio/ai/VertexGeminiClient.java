package dev.simakogan.artstudio.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auth.oauth2.GoogleCredentials;
import dev.simakogan.artstudio.config.AppProperties;
import dev.simakogan.artstudio.dto.Dtos.ChatTurn;
import dev.simakogan.artstudio.dto.Dtos.ClassDto;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Gemini on Vertex AI over the REST API (generateContent), authenticated with Application Default Credentials
 * (`gcloud auth application-default login` locally, a service account on Cloud Run/GKE).
 * Enabled with AI_PROVIDER=vertex, VERTEX_PROJECT_ID, VERTEX_LOCATION, VERTEX_MODEL.
 */
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "vertex")
public class VertexGeminiClient implements LlmClient {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy, HH:mm", Locale.ENGLISH);

    private final AppProperties.Vertex cfg;
    private final ObjectMapper mapper;
    private final RestClient http = RestClient.create();

    public VertexGeminiClient(AppProperties props, ObjectMapper mapper) {
        this.cfg = props.ai().vertex();
        this.mapper = mapper;
        if (cfg.projectId() == null || cfg.projectId().isBlank()) {
            throw new IllegalStateException("VERTEX_PROJECT_ID is required when AI_PROVIDER=vertex");
        }
    }

    @Override
    public String name() {
        return "vertex:" + cfg.model();
    }

    @Override
    public String generate(Prompt p) {
        try {
            JsonNode res = http.post()
                    .uri(endpoint())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken())
                    .body(body(p))
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode text = res == null ? null : res.at("/candidates/0/content/parts/0/text");
            if (text == null || text.isMissingNode() || text.asText().isBlank()) {
                throw new IllegalStateException("Empty response from Vertex AI");
            }
            return text.asText().trim();
        } catch (IOException e) {
            throw new IllegalStateException("Could not obtain Google credentials", e);
        }
    }

    private String endpoint() {
        String host = "global".equals(cfg.location()) ? "aiplatform.googleapis.com"
                : cfg.location() + "-aiplatform.googleapis.com";
        return "https://" + host + "/v1/projects/" + cfg.projectId() + "/locations/" + cfg.location()
                + "/publishers/google/models/" + cfg.model() + ":generateContent";
    }

    private String accessToken() throws IOException {
        GoogleCredentials creds = GoogleCredentials.getApplicationDefault()
                .createScoped("https://www.googleapis.com/auth/cloud-platform");
        creds.refreshIfExpired();
        return creds.getAccessToken().getTokenValue();
    }

    private ObjectNode body(Prompt p) {
        ObjectNode root = mapper.createObjectNode();
        root.putObject("systemInstruction").putArray("parts").addObject().put("text", p.system());

        ArrayNode contents = root.putArray("contents");
        for (ChatTurn t : p.history()) {
            ObjectNode c = contents.addObject();
            c.put("role", "assistant".equalsIgnoreCase(t.role()) ? "model" : "user");
            c.putArray("parts").addObject().put("text", t.text());
        }
        ObjectNode last = contents.addObject();
        last.put("role", "user");
        last.putArray("parts").addObject().put("text", "Classes you may use (upcoming, bookable):\n" + catalogue(p)
                + "\n\nQuestion: " + p.question());

        ObjectNode gen = root.putObject("generationConfig");
        gen.put("temperature", 0.3);
        gen.put("maxOutputTokens", 512);
        return root;
    }

    private static String catalogue(Prompt p) {
        if (p.context().isEmpty()) return "(no matching classes)";
        StringBuilder sb = new StringBuilder();
        for (ClassDto c : p.context()) {
            sb.append("- ").append(c.title()).append(" | ").append(c.category()).append(" | ").append(c.level())
                    .append(" | ").append(c.startsAt().format(WHEN)).append(" | ").append(c.durationMinutes())
                    .append(" min | ₪").append(c.priceCents() / 100).append(" | ").append(c.seatsLeft())
                    .append(" seats left | instructor ").append(c.instructor()).append('\n');
        }
        return sb.toString();
    }
}
