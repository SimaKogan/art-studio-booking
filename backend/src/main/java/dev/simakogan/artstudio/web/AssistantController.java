package dev.simakogan.artstudio.web;

import dev.simakogan.artstudio.ai.ScheduleAssistant;
import dev.simakogan.artstudio.dto.Dtos.ChatRequest;
import dev.simakogan.artstudio.dto.Dtos.ChatResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final ScheduleAssistant assistant;

    public AssistantController(ScheduleAssistant assistant) {
        this.assistant = assistant;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest req) {
        return assistant.chat(req);
    }
}
