package com.translations.ai.dto.response;

import java.util.List;

/**
 * Structured response mapped from the OpenAI assistant reply.
 * The SDK deserializes the assistant message into this POJO when
 * {@code responseFormat(TranslationsResponse.class)} is used.
 */
public class TranslationsResponse {

    /**
     * List of language-tagged suggestions returned by the model.
     */
    private List<Translation> translations;

    public List<Translation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }

}
