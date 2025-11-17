/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved
 */
package org.training.ai.client;

import org.training.ai.dto.AiClientOptions;
import org.training.ai.exception.AiClientException;

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
	String enhance(String prompt, AiClientOptions options) throws AiClientException;
}
