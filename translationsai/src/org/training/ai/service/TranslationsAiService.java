package org.training.ai.service;

import de.hybris.platform.core.model.product.ProductModel;
import org.training.ai.dto.options.PromptOptions;
import org.training.ai.dto.response.Translation;
import org.training.ai.exception.AiClientException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service for AI-powered product description enhancement
 */
public interface TranslationsAiService {
    /**
     * Enhance product description using AI and return enhanced text for multiple locales.
     * The returned map keys are IETF BCP 47 language tags (e.g., "en", "de-DE").
     *
     * @param product the product whose description should be enhanced
     * @param locale  the base locale for enhancement (source language)
     * @param options options for customizing the AI prompt including target languages
     * @return map of locale isocode -> enhanced description
     * @throws AiClientException if the AI call fails
     */
    List<Translation> enhanceDescription(ProductModel product, Locale locale, PromptOptions options) throws AiClientException;

    /**
     * Check if the AI enhancement feature is enabled
     *
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();
}
