package dev.simakogan.artstudio.ai;

import dev.simakogan.artstudio.dto.Dtos.ChatTurn;
import dev.simakogan.artstudio.dto.Dtos.ClassDto;
import java.util.List;

/** Text-generation backend. Implementations: {@link LocalLlmClient} (offline) and {@link VertexGeminiClient}. */
public interface LlmClient {

    String name();

    String generate(Prompt prompt);

    /**
     * @param system   fixed instructions (never contains user text)
     * @param history  previous turns, oldest first
     * @param question the current user message
     * @param context  retrieved classes the answer must be grounded in
     */
    record Prompt(String system, List<ChatTurn> history, String question, List<ClassDto> context) {}
}
