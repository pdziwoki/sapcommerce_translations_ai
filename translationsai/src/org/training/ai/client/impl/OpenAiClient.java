/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved
 */
package org.training.ai.client.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.training.ai.client.AiClient;
import org.training.ai.dto.AiClientOptions;
import org.training.ai.exception.AiClientException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * OpenAI client implementation using Chat Completions API
 */
public class OpenAiClient implements AiClient
{
	private static final Logger LOG = Logger.getLogger(OpenAiClient.class);

	private String apiKey;
	private String baseUrl = "https://api.openai.com/v1";
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String enhance(final String prompt, final AiClientOptions options) throws AiClientException
	{
		if (apiKey == null || apiKey.trim().isEmpty())
		{
			throw new AiClientException("OpenAI API key is not configured");
		}

		final HttpPost post = new HttpPost(baseUrl + "/chat/completions");
		post.setHeader("Authorization", "Bearer " + apiKey);
		post.setHeader("Content-Type", "application/json");

		final ObjectNode body = JsonNodeFactory.instance.objectNode();
		body.put("model", options.getModel());

		final ArrayNode messages = body.putArray("messages");
		final ObjectNode systemMessage = messages.addObject();
		systemMessage.put("role", "system");
		systemMessage.put("content", "You improve product descriptions.");

		final ObjectNode userMessage = messages.addObject();
		userMessage.put("role", "user");
		userMessage.put("content", prompt);

		body.put("temperature", options.getTemperature());
		body.put("max_tokens", options.getMaxTokens());

		try
		{
			post.setEntity(new StringEntity(body.toString(), StandardCharsets.UTF_8));

			try (CloseableHttpClient client = HttpClients.createDefault();
					CloseableHttpResponse resp = client.execute(post))
			{
				final int statusCode = resp.getStatusLine().getStatusCode();
				final String json = EntityUtils.toString(resp.getEntity());

				LOG.debug("OpenAI response status: " + statusCode);

				if (statusCode >= 200 && statusCode < 300)
				{
					final JsonNode root = objectMapper.readTree(json);
					final String enhancedText = root.path("choices").get(0).path("message").path("content").asText();
					return enhancedText.trim();
				}
				else
				{
					LOG.error("OpenAI API error: " + statusCode + " - " + json);
					throw new AiClientException("OpenAI error: " + statusCode + " - " + json);
				}
			}
		}
		catch (final IOException e)
		{
			LOG.error("HTTP error calling OpenAI", e);
			throw new AiClientException("HTTP error", e);
		}
	}

	public void setApiKey(final String apiKey)
	{
		this.apiKey = apiKey;
	}

	public void setBaseUrl(final String baseUrl)
	{
		this.baseUrl = baseUrl;
	}
}
