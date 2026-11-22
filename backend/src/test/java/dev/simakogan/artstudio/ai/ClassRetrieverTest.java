package dev.simakogan.artstudio.ai;

import static org.assertj.core.api.Assertions.assertThat;

import dev.simakogan.artstudio.domain.Category;
import dev.simakogan.artstudio.domain.Level;
import dev.simakogan.artstudio.dto.Dtos.ClassDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassRetrieverTest {
    // 2026-11-21 is a Saturday.
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 21);
    private final ClassRetriever retriever = new ClassRetriever();

    private static ClassDto cls(long id, String title, Category cat, Level level, int daysAhead, int hour,
                                int priceIls, int seatsLeft) {
        return new ClassDto(id, title, title + " description", cat, level, "Teacher",
                "Studio", LocalDateTime.of(TODAY.plusDays(daysAhead), LocalTime.of(hour, 0)), 120,
                priceIls * 100, 10, seatsLeft, true);
    }

    private final List<ClassDto> pool = List.of(
            cls(1, "Oil Painting", Category.PAINTING, Level.BEGINNER, 2, 18, 220, 5),
            cls(2, "Watercolor Wet-on-Wet", Category.WATERCOLOR, Level.BEGINNER, 7, 10, 180, 4),
            cls(3, "Wheel Throwing", Category.CERAMICS, Level.BEGINNER, 4, 19, 280, 0),
            cls(4, "Kids Animal Paintings", Category.KIDS, Level.ALL_LEVELS, 3, 10, 110, 8));

    @Test
    void matchesMediumFirst() {
        List<ClassDto> hits = retriever.retrieve("I'd like a watercolor class", pool, TODAY, 3);
        assertThat(hits.get(0).id()).isEqualTo(2L);
    }

    @Test
    void honoursPriceCap() {
        List<ClassDto> hits = retriever.retrieve("anything under 150", pool, TODAY, 5);
        assertThat(hits).extracting(ClassDto::id).containsExactly(4L);
    }

    @Test
    void pushesFullClassesDown() {
        List<ClassDto> hits = retriever.retrieve("pottery or painting", pool, TODAY, 3);
        assertThat(hits.get(0).id()).isNotEqualTo(3L);
    }

    @Test
    void fallsBackToSoonestWhenNoSignal() {
        List<ClassDto> hits = retriever.retrieve("what's on?", pool, TODAY, 2);
        assertThat(hits).extracting(ClassDto::id).containsExactly(1L, 4L);
    }
}
