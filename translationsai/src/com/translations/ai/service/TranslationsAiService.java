package com.translations.ai.service;

import de.hybris.platform.core.model.product.ProductModel;
import com.translations.ai.dto.options.PromptOptions;
import com.translations.ai.dto.response.Translation;
import com.translations.ai.exception.AiClientException;

import java.util.List;
import java.util.Locale;

/**
 * Service for AI-powered product description translation and enhancement.
 */
public interface TranslationsAiService {
    /**
     * Translate and eventually enhance a product description using AI and return suggestions for multiple locales.
     * Each suggestion is represented as a {@link Translation} item with
     * an IETF BCP 47 language tag (e.g., "en", "de-DE") and the suggested description text.
     *
     * @param product the product whose description should be enhanced/translated
     * @param locale  the base (source) locale
     * @param options options for customizing the AI prompt including target languages and mode
     * @return list of language-tagged suggestions
     * @throws AiClientException if the AI call fails
     */
    List<Translation> translateDescription(ProductModel product, Locale locale, PromptOptions options) throws AiClientException;

    /**
     * Check if the AI enhancement feature is enabled
     *
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();
}
