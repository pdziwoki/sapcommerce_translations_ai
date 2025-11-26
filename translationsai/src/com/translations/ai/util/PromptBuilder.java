package com.translations.ai.util;

import com.translations.ai.dto.response.Translation;
import com.translations.ai.dto.response.TranslationsResponse;
import org.apache.commons.lang3.StringUtils;
import com.translations.ai.dto.options.PromptOptions;

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
     * Build a prompt for description processing. Behavior depends on {@link PromptOptions#isEnhanceSource()}:
     * - If true: first improve the original description in the base language, then translate the improved version
     * into requested target languages (enhance + translate).
     * - If false: translate the original description as-is into requested target languages (translate-only).
     * This method only constructs natural-language instructions; the OpenAI Java SDK is configured
     * to return a structured {@link TranslationsResponse} that maps
     * to a list of {@link Translation} items (lang, description).
     */
    public static String buildTranslatePrompt(final String sourceDescription, final Locale locale, final PromptOptions options) {
        final boolean enhance = options != null && options.isEnhanceSource();

        final String sourceLanguage = Optional.ofNullable(options != null ? options.getSourceLanguage() : null)
                .orElse(locale.toLanguageTag());
        final String tone = Optional.ofNullable(options != null ? options.getTone() : null)
                .orElse("neutral professional");
        final String length = (options != null && options.getMaxLength() != null)
                ? ("Limit to " + options.getMaxLength() + " words.")
                : "";

        final List<String> targets = Optional.ofNullable(options != null ? options.getTargetLanguages() : null)
                .orElse(enhance ? Collections.singletonList(sourceLanguage) : Collections.emptyList());

        final StringBuilder prompt = new StringBuilder();
        if (enhance) {
            prompt.append("You are an expert product copywriter and translator. First, improve the following product description in the base language keeping factual accuracy, " +
                    "then translate that improved version into the requested languages.\n");
        } else {
            prompt.append("You are a precise product translator. Translate the original product description from the base language to the requested languages WITHOUT enhancing or rewriting it. Preserve meaning and important terms.\n");
        }
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
