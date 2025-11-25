/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved
 */
package org.training.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.training.ai.client.AiClient;
import org.training.ai.dto.options.AiClientOptions;
import org.training.ai.dto.options.PromptOptions;
import org.training.ai.dto.response.Translation;
import org.training.ai.exception.AiClientException;
import org.training.ai.service.TranslationsAiService;
import org.training.ai.util.PromptBuilder;

import java.time.Duration;
import java.util.*;

/**
 * Default implementation of TranslationsAiService
 */
public class DefaultTranslationsAiService implements TranslationsAiService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultTranslationsAiService.class);

    private static final String CONFIG_ENABLED = "translationsai.enabled";
    private static final String CONFIG_MODEL = "translationsai.openai.model";
    private static final String CONFIG_MAX_TOKENS = "translationsai.maxTokens";
    private static final String CONFIG_TIMEOUT_MS = "translationsai.timeout.ms";

    private AiClient aiClient;
    private ConfigurationService configurationService;

    @Override
    public List<Translation> enhanceDescription(final ProductModel product, final Locale locale, final PromptOptions options)
            throws AiClientException {
        if (!isEnabled()) {
            throw new AiClientException("AI enhancement feature is not enabled");
        }

        // Mock response path
        if (configurationService.getConfiguration().getBoolean("translationsai.mock.response", false)) {
            return getMock(locale, options);
        }

        final String sourceDescription = product.getDescription(locale);
        if (StringUtils.isBlank(sourceDescription)) {
            throw new AiClientException("Product description is empty for locale: " + locale);
        }

        final String prompt = PromptBuilder.buildEnhanceTranslatePrompt(sourceDescription, locale, options);
        LOG.info("Built prompt\n[{}]", prompt);

        final AiClientOptions clientOptions = buildClientOptions();
        return aiClient.enhance(prompt, clientOptions);
    }

    private static List<Translation> getMock(Locale locale, PromptOptions options) {
        final List<String> targets = options.getTargetLanguages() != null && !options.getTargetLanguages().isEmpty()
                ? options.getTargetLanguages()
                : Collections.singletonList(options.getSourceLanguage() != null ? options.getSourceLanguage() : locale.toLanguageTag());
        final List<Translation> mock = new ArrayList<>();
        for (String t : targets) {
            final Translation translation = new Translation(t, "MOCK ENHANCED DESCRIPTION for " + t + " – lorem ipsum");
            mock.add(translation);
        }
        return mock;
    }

    private Map<String, String> parseResponseToMap(final String response) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final List<Map<String, Object>> list = mapper.readValue(response, mapper.getTypeFactory().constructCollectionType(List.class, mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)));
        final Map<String, String> result = new LinkedHashMap<>();
        for (Map<String, Object> item : list) {
            final Object loc = item.get("locale");
            final Object desc = item.get("enhanceddescription");
            if (loc != null && desc != null) {
                result.put(String.valueOf(loc), String.valueOf(desc));
            }
        }
        return result;
    }

    @Override
    public boolean isEnabled() {
        return configurationService.getConfiguration().getBoolean(CONFIG_ENABLED, false);
    }

    /**
     * Build AI client options from configuration
     */
    private AiClientOptions buildClientOptions() {
        final AiClientOptions options = new AiClientOptions();
        options.setModel(configurationService.getConfiguration().getString(CONFIG_MODEL, "gpt-5-nano"));

        final int timeoutMs = configurationService.getConfiguration().getInteger(CONFIG_TIMEOUT_MS, 20000);
        options.setTimeout(Duration.ofMillis(timeoutMs));

        return options;
    }

    public void setAiClient(final AiClient aiClient) {
        this.aiClient = aiClient;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
