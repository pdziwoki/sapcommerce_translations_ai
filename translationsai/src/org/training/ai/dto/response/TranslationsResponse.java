package org.training.ai.dto.response;

import java.util.List;

public class TranslationsResponse {

    private List<Translation> translations;

    public List<Translation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }

}
