package org.training.ai.client;

import org.training.ai.dto.options.AiClientOptions;
import org.training.ai.dto.response.Translation;
import org.training.ai.exception.AiClientException;

import java.util.List;

/**
 * Interface for AI client implementations (OpenAI, Azure OpenAI, etc.).
 * Implementations should accept a natural-language prompt and return a structured list
 * of language-tagged description suggestions.
 */
public interface AiClient {
    /**
     * Invoke the AI using the provided prompt and options.
     *
     * @param prompt  the prompt to send to the AI
     * @param options configuration options for the AI call (model, timeout)
     * @return a list of language-tagged suggestions (lang + description)
     * @throws AiClientException if the AI call fails
     */
    List<Translation> translate(String prompt, AiClientOptions options) throws AiClientException;
}
