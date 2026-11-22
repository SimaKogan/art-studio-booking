package dev.simakogan.artstudio.web;

import dev.simakogan.artstudio.domain.Category;
import dev.simakogan.artstudio.domain.Level;
import dev.simakogan.artstudio.dto.Dtos.ClassDto;
import dev.simakogan.artstudio.service.ClassService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/classes")
public class ClassController {
    private final ClassService classes;

    public ClassController(ClassService classes) {
        this.classes = classes;
    }

    @GetMapping
    public List<ClassDto> list(@RequestParam(required = false) Category category,
                               @RequestParam(required = false) Level level,
                               @RequestParam(required = false) String q,
                               @RequestParam(required = false) Integer maxPrice) {
        return classes.search(category, level, q, maxPrice == null ? null : maxPrice * 100);
    }

    @GetMapping("/{id}")
    public ClassDto get(@PathVariable Long id) {
        return classes.get(id);
    }
}
