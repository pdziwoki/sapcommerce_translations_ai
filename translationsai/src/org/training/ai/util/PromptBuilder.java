package org.training.ai.util;

import org.apache.commons.lang3.StringUtils;
import org.training.ai.dto.options.PromptOptions;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Utility class for building AI prompts
 */
public final class PromptBuilder {
    private PromptBuilder() {
    }

    /**
     * Build a prompt that first enhances the original description in the base language,
     * then translates that enhanced description into the languages requested in options.targetLanguages.
     * The AI must return a strict JSON array where each element has keys: "locale" and "enhanceddescription".
     */
    public static String buildEnhanceTranslatePrompt(final String sourceDescription, final Locale locale, final PromptOptions options) {
        final String sourceLanguage = Optional.ofNullable(options.getSourceLanguage()).orElse(locale.toLanguageTag());
        final String tone = Optional.ofNullable(options.getTone()).orElse("neutral professional");
        final String length = options.getMaxLength() != null ? ("Limit to " + options.getMaxLength() + " words.") : "";

        final List<String> targets = Optional.ofNullable(options.getTargetLanguages()).orElse(Collections.singletonList(sourceLanguage));

        final StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert product copywriter and translator. First, improve the following product description in the base language keeping factual accuracy, " +
                "then translate that improved version into the requested languages.\n");
        prompt.append("- Base language: ").append(sourceLanguage).append(".\n");
        prompt.append("- Requested output languages (IETF tags): ").append(String.join(", ", targets)).append(".\n");
        prompt.append("- Tone: ").append(tone).append(".\n");
        if (StringUtils.isNotBlank(length)) {
            prompt.append("- ").append(length).append("\n");
        }
        prompt.append("Original description (base language ").append(sourceLanguage).append("):\n").append(sourceDescription.trim());
        return prompt.toString();
    }
}
