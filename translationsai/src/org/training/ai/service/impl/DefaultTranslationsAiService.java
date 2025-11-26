/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved
 */
package org.training.ai.service.impl;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Default implementation of {@link org.training.ai.service.TranslationsAiService}.
 * <p>
 * Features:
 * - Two prompt modes controlled by {@link org.training.ai.dto.options.PromptOptions#isEnhanceSource()} (enhance+translate vs translate-only)
 * - Mock mode when property `translationsai.mock.response` is true
 * - OpenAI Java SDK used via {@link org.training.ai.client.AiClient}
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
    public List<Translation> translateDescription(final ProductModel product, final Locale locale, final PromptOptions options)
            throws AiClientException {
        if (!isEnabled()) {
            throw new AiClientException("AI translation feature is not enabled");
        }

        // Mock response path
        if (configurationService.getConfiguration().getBoolean("translationsai.mock.response", false)) {
            return getMock(locale, options);
        }

        final String sourceDescription = product.getDescription(locale);
        if (StringUtils.isBlank(sourceDescription)) {
            throw new AiClientException("Product description is empty for locale: " + locale);
        }

        final String prompt = PromptBuilder.buildTranslatePrompt(sourceDescription, locale, options);
        LOG.info("Built prompt\n[{}]", prompt);

        final AiClientOptions clientOptions = buildClientOptions();
        return aiClient.translate(prompt, clientOptions);
    }

    private static List<Translation> getMock(Locale locale, PromptOptions options) {
        final List<String> targets = options.getTargetLanguages() != null && !options.getTargetLanguages().isEmpty()
                ? options.getTargetLanguages()
                : Collections.singletonList(options.getSourceLanguage() != null ? options.getSourceLanguage() : locale.toLanguageTag());
        final List<Translation> mock = new ArrayList<>();
        for (final String t : targets) {
            final Translation translation = new Translation(t, "MOCK TRANSLATED DESCRIPTION for [" + t + "] – language");
            mock.add(translation);
        }
        return mock;
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
