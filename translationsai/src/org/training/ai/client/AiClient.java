package org.training.ai.client;

import org.training.ai.dto.options.AiClientOptions;
import org.training.ai.dto.response.Translation;
import org.training.ai.exception.AiClientException;

import java.util.List;

/**
 * Interface for AI client implementations (OpenAI, Azure OpenAI, etc.)
 */
public interface AiClient
{
	/**
	 * Enhance text using AI based on the provided prompt
	 *
	 * @param prompt the prompt to send to the AI
	 * @param options configuration options for the AI call
	 * @return enhanced text
	 * @throws AiClientException if the AI call fails
	 */
	List<Translation> enhance(String prompt, AiClientOptions options) throws AiClientException;
}
