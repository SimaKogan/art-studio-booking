package dev.simakogan.artstudio.ai;

import dev.simakogan.artstudio.domain.Category;
import dev.simakogan.artstudio.domain.Level;
import dev.simakogan.artstudio.dto.Dtos.ClassDto;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * The "R" in RAG for the schedule: lexical retrieval over upcoming classes.
 * It understands category synonyms, level, weekdays, today/tomorrow/weekend, morning/evening and a price cap,
 * and scores each class against the question. The LLM only ever sees the top-k hits, so answers are grounded
 * in real availability. Swap this for embeddings (e.g. Vertex AI text-embedding + pgvector) when the catalogue grows.
 */
@Component
public class ClassRetriever {

    private static final Map<Category, List<String>> SYNONYMS = Map.of(
            Category.PAINTING, List.of("paint", "oil", "acrylic", "canvas"),
            Category.WATERCOLOR, List.of("watercolor", "watercolour", "wash"),
            Category.CERAMICS, List.of("ceramic", "clay", "pottery", "wheel", "glaze"),
            Category.DRAWING, List.of("draw", "sketch", "charcoal", "pencil", "portrait"),
            Category.ILLUSTRATION, List.of("illustrat", "digital", "procreate", "comic", "ipad"),
            Category.KIDS, List.of("kid", "child", "children", "family", "teen"));

    private static final Set<String> STOP = Set.of("the", "and", "for", "with", "any", "you", "have", "what", "when",
            "can", "class", "classes", "want", "like", "some", "there", "this", "that", "about", "from", "are", "get",
            "something", "please", "show", "find", "need", "looking");

    private static final Pattern PRICE = Pattern.compile("(?:under|below|less than|up to|max|<=?)\\s*[₪$€]?\\s*(\\d{2,4})");

    public List<ClassDto> retrieve(String question, List<ClassDto> pool, LocalDate today, int k) {
        String q = question == null ? "" : question.toLowerCase(Locale.ROOT);
        Integer maxPrice = maxPrice(q);
        Set<DayOfWeek> days = days(q, today);
        boolean evening = q.contains("evening") || q.contains("after work") || q.contains("night");
        boolean morning = q.contains("morning");
        boolean beginner = q.contains("beginner") || q.contains("first time") || q.contains("never") || q.contains("new to");
        boolean advanced = q.contains("advanced") || q.contains("experienced");
        Set<String> tokens = tokens(q);
        Set<Category> wanted = EnumSet.noneOf(Category.class);
        SYNONYMS.forEach((cat, words) -> {
            if (words.stream().anyMatch(q::contains)) wanted.add(cat);
        });

        List<Scored> scored = new ArrayList<>();
        for (ClassDto c : pool) {
            if (maxPrice != null && c.priceCents() > maxPrice * 100) continue;
            int score = 0;
            if (wanted.contains(c.category())) score += 5;
            String haystack = (c.title() + " " + c.description() + " " + c.instructor()).toLowerCase(Locale.ROOT);
            for (String t : tokens) if (haystack.contains(t)) score += 1;
            if (!days.isEmpty() && days.contains(c.startsAt().getDayOfWeek())) score += 3;
            if (q.contains("today") && c.startsAt().toLocalDate().equals(today)) score += 3;
            if (q.contains("tomorrow") && c.startsAt().toLocalDate().equals(today.plusDays(1))) score += 3;
            if (evening && c.startsAt().getHour() >= 17) score += 2;
            if (morning && c.startsAt().getHour() < 12) score += 2;
            if (beginner && (c.level() == Level.BEGINNER || c.level() == Level.ALL_LEVELS)) score += 2;
            if (advanced && c.level() == Level.INTERMEDIATE) score += 2;
            if (c.seatsLeft() == 0) score -= 6;
            scored.add(new Scored(c, score));
        }

        boolean anySignal = scored.stream().anyMatch(s -> s.score > 0);
        return scored.stream()
                .filter(s -> !anySignal || s.score > 0)
                .sorted(Comparator.comparingInt((Scored s) -> s.score).reversed()
                        .thenComparing(s -> s.cls.startsAt()))
                .limit(k)
                .map(s -> s.cls)
                .toList();
    }

    private static Integer maxPrice(String q) {
        Matcher m = PRICE.matcher(q);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    private static Set<DayOfWeek> days(String q, LocalDate today) {
        Set<DayOfWeek> out = EnumSet.noneOf(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) {
            if (q.contains(d.name().toLowerCase(Locale.ROOT))) out.add(d);
        }
        if (q.contains("weekend")) {
            out.add(DayOfWeek.FRIDAY);
            out.add(DayOfWeek.SATURDAY);
        }
        return out;
    }

    private static Set<String> tokens(String q) {
        Set<String> out = new java.util.LinkedHashSet<>();
        for (String t : q.split("[^\\p{L}]+")) {
            if (t.length() > 3 && !STOP.contains(t)) out.add(t);
        }
        return out;
    }

    private record Scored(ClassDto cls, int score) {}
}
