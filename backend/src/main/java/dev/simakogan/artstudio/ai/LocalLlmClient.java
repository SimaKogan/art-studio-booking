package dev.simakogan.artstudio.ai;

import dev.simakogan.artstudio.dto.Dtos.ClassDto;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Offline "model": writes the answer from the retrieved classes with a template.
 * Keeps the assistant working with no API key (demos, CI) and acts as the fallback if Vertex AI fails.
 */
@Component
public class LocalLlmClient implements LlmClient {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("EEE d MMM, HH:mm", Locale.ENGLISH);

    @Override
    public String name() {
        return "local";
    }

    @Override
    public String generate(Prompt p) {
        String q = p.question().toLowerCase(Locale.ROOT).trim();
        if (q.matches("^(hi|hello|hey|shalom|hola)\\b.*") && p.context().isEmpty()) {
            return "Hi! Tell me what you'd like to make and when you're free, for example "
                    + "\"a beginner watercolor class on Saturday under 200\".";
        }
        if (p.context().isEmpty()) {
            return "I couldn't find an upcoming class that fits. Try another day, a different medium "
                    + "(painting, watercolor, ceramics, drawing, illustration, kids) or a higher price limit.";
        }
        String lines = p.context().stream().map(LocalLlmClient::line).collect(Collectors.joining("\n"));
        String lead = p.context().size() == 1 ? "This one fits best:" : "These fit best:";
        return lead + "\n" + lines + "\nOpen a class to book your seat.";
    }

    private static String line(ClassDto c) {
        String seats = c.seatsLeft() == 0 ? "full" : c.seatsLeft() + " seat" + (c.seatsLeft() == 1 ? "" : "s") + " left";
        return "- " + c.title() + ", " + c.startsAt().format(WHEN) + ", ₪" + (c.priceCents() / 100) + ", " + seats;
    }
}
