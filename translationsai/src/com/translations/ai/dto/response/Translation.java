package com.translations.ai.dto.response;

/**
 * Single language-tagged suggestion returned by the AI.
 * <p>
 * Fields:
 * - lang: IETF BCP 47 language tag (e.g., "en", "de-DE").
 * - description: Suggested enhanced/translated description text for the given language.
 */
public class Translation {

    public Translation() {
    }

    public Translation(String lang, String description) {
        this.lang = lang;
        this.description = description;
    }

    private String lang;

    private String description;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
