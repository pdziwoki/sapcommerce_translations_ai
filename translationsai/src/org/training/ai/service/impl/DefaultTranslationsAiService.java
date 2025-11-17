/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved
 */
package org.training.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.training.ai.client.AiClient;
import org.training.ai.dto.AiClientOptions;
import org.training.ai.dto.PromptOptions;
import org.training.ai.exception.AiClientException;
import org.training.ai.service.TranslationsAiService;
import org.training.ai.util.PromptBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Default implementation of TranslationsAiService
 */
public class DefaultTranslationsAiService implements TranslationsAiService {
    private static final Logger LOG = Logger.getLogger(DefaultTranslationsAiService.class);

    private static final String CONFIG_ENABLED = "translationsai.enabled";
    private static final String CONFIG_MODEL = "translationsai.openai.model";
    private static final String CONFIG_TEMPERATURE = "translationsai.temperature";
    private static final String CONFIG_MAX_TOKENS = "translationsai.maxTokens";
    private static final String CONFIG_TIMEOUT_MS = "translationsai.timeout.ms";

    private AiClient aiClient;
    private ConfigurationService configurationService;

    @Override
    public Map<String, String> enhanceDescription(final ProductModel product, final Locale locale, final PromptOptions options)
            throws AiClientException {
        if (!isEnabled()) {
            throw new AiClientException("AI enhancement feature is not enabled");
        }

        final String source = getDescription(product, locale);
        if (StringUtils.isBlank(source)) {
            throw new AiClientException("Product description is empty for locale: " + locale);
        }

        // Mock response path
        if (configurationService.getConfiguration().getBoolean("translationsai.mock.response", false)) {
            return getMock(locale, options);
        }

        final String prompt = PromptBuilder.buildEnhanceTranslatePrompt(source, locale, options);
        LOG.debug("Built prompt for product " + product.getCode() + " in locale " + locale + " with targets: " + options.getTargetLanguages());

        final AiClientOptions clientOptions = buildClientOptions();
        final String response = aiClient.enhance(prompt, clientOptions);

        try {
            return parseResponseToMap(response);
        } catch (final Exception e) {
            LOG.error("Failed to parse AI response", e);
            throw new AiClientException("Failed to parse AI response: " + e.getMessage());
        }
    }

    private static Map<String, String> getMock(Locale locale, PromptOptions options) {
        final List<String> targets = options.getTargetLanguages() != null && !options.getTargetLanguages().isEmpty()
                ? options.getTargetLanguages()
                : java.util.Collections.singletonList(options.getLanguage() != null ? options.getLanguage() : locale.toLanguageTag());
        final Map<String, String> mock = new java.util.LinkedHashMap<>();
        for (String t : targets) {
            mock.put(t, "MOCK ENHANCED DESCRIPTION for " + t + " – lorem ipsum");
        }
        return mock;
    }

    private Map<String, String> parseResponseToMap(final String response) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final List<Map<String, Object>> list = mapper.readValue(response, mapper.getTypeFactory().constructCollectionType(List.class, mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)));
        final Map<String, String> result = new java.util.LinkedHashMap<>();
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
     * Get product description for the specified locale
     */
    private String getDescription(final ProductModel product, final Locale locale) {
        return product.getDescription(locale);
    }

    /**
     * Build AI client options from configuration
     */
    private AiClientOptions buildClientOptions() {
        final AiClientOptions options = new AiClientOptions();
        options.setModel(configurationService.getConfiguration().getString(CONFIG_MODEL, "gpt-4o-mini"));
        options.setTemperature(configurationService.getConfiguration().getDouble(CONFIG_TEMPERATURE, 0.3));
        options.setMaxTokens(configurationService.getConfiguration().getInteger(CONFIG_MAX_TOKENS, 512));

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
