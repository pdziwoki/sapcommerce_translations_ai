package com.translations.ai.client.impl;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import org.apache.log4j.Logger;
import com.translations.ai.client.AiClient;
import com.translations.ai.dto.options.AiClientOptions;
import com.translations.ai.dto.response.Translation;
import com.translations.ai.dto.response.TranslationsResponse;
import com.translations.ai.exception.AiClientException;

import java.util.List;

/**
 * OpenAI client implementation using the OpenAI Java SDK (Chat Completions) with structured responses.
 * <p>
 * The SDK is configured to map the assistant response directly into a
 * {@link TranslationsResponse} via responseFormat.
 * The API key is injected via the {@link #setApiKey(String)} setter (e.g., from Spring properties),
 * not via environment variables.
 */
public class OpenAiClient implements AiClient {
    private static final Logger LOG = Logger.getLogger(OpenAiClient.class);

    private String apiKey;

    @Override
    public List<Translation> translate(final String prompt, final AiClientOptions options) throws AiClientException {
        try {

            final OpenAIClient client = OpenAIOkHttpClient.builder()
                    .apiKey(apiKey)
                    .timeout(options.getTimeout())
                    .build();

            final StructuredChatCompletionCreateParams<TranslationsResponse> params = ChatCompletionCreateParams.builder()
                    .addUserMessage(prompt)
                    .model(options.getModel())
                    .responseFormat(TranslationsResponse.class)
                    .n(1)
                    .build();

            final List<Translation> translations = client
                    .chat()
                    .completions()
                    .create(params)
                    .choices()
                    .stream()
                    .findFirst() // get the first (and only) choice
                    .flatMap(choice -> choice.message().content()) // Optional<List<ResponseContent>>
                    .map(TranslationsResponse::getTranslations)
                    .orElseThrow(() -> new AiClientException("OpenAI returned no translations"));

            return translations;

        } catch (final Exception e) {
            LOG.error("Error calling OpenAI via SDK", e);
            throw new AiClientException("OpenAI SDK error", e);
        }
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

}
