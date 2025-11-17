/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved
 */
package org.training.ai.util;

import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.training.ai.dto.PromptOptions;

/**
 * Utility class for building AI prompts
 */
public final class PromptBuilder {
    private PromptBuilder() {
        // Utility class
    }

    /**
     * Build a prompt for enhancing a product description
     *
     * @param source  the original description
     * @param locale  the locale for the description
     * @param options customization options
     * @return the prompt to send to the AI
     */
    public static String buildEnhancePrompt(final String source, final Locale locale, final PromptOptions options) {
        // kept for backward compatibility with single-language flows
        return buildEnhanceTranslatePrompt(source, locale, options);
    }

    /**
     * Build a prompt that first enhances the original description in the base language,
     * then translates that enhanced description into the languages requested in options.targetLanguages.
     * The AI must return a strict JSON array where each element has keys: "locale" and "enhanceddescription".
     */
    public static String buildEnhanceTranslatePrompt(final String source, final Locale locale, final PromptOptions options) {
        final String baseLang = Optional.ofNullable(options.getLanguage()).orElse(locale.toLanguageTag());
        final String tone = Optional.ofNullable(options.getTone()).orElse("neutral professional");
        final String length = options.getMaxLength() != null ? ("Limit to " + options.getMaxLength() + " words.") : "";
        final String keywords = (options.getKeywordsToInclude() != null && !options.getKeywordsToInclude().isEmpty())
                ? ("Include these terms if natural: " + String.join(", ", options.getKeywordsToInclude()) + ".") : "";

        final java.util.List<String> targets = Optional.ofNullable(options.getTargetLanguages()).orElse(java.util.Collections.singletonList(baseLang));

        final StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert product copywriter and translator. First, improve the following product description in the base language keeping factual accuracy, then translate that improved version into the requested languages.\n");
        prompt.append("- Base language: ").append(baseLang).append(".\n");
        prompt.append("- Requested output languages (IETF tags): ").append(String.join(", ", targets)).append(".\n");
        prompt.append("- Tone: ").append(tone).append(".\n");
        if (options.isPreserveTechnicalTerms()) {
            prompt.append("- Preserve technical terms and specifications.\n");
        }
        if (StringUtils.isNotBlank(length)) {
            prompt.append("- ").append(length).append("\n");
        }
        if (StringUtils.isNotBlank(keywords)) {
            prompt.append("- ").append(keywords).append("\n");
        }
        prompt.append("Return ONLY a valid JSON array (no preamble, no code fences). Each element must be an object with exactly these keys: \"locale\" and \"enhanceddescription\". The \"locale\" value must be one of the requested output languages.\n\n");
        prompt.append("Original description (base language ").append(baseLang).append("):\n").append(source.trim());
        return prompt.toString();
    }
}
