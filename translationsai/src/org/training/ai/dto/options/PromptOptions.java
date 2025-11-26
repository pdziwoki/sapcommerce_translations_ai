package org.training.ai.dto.options;

import java.util.List;

/**
 * Options for customizing the AI prompt for description enhancement/translation.
 * <p>
 * Behavior notes:
 * - sourceLanguage: IETF BCP 47 tag for the input text (e.g., "en", "de-DE"). If null, the caller locale is used.
 * - targetLanguages: List of IETF language tags for which the AI should produce suggestions.
 * - enhanceSource: If true, first enhance the source description in the base language and include the base language
 * in targets; if false, perform pure translation of the original text. In pure translation mode it is recommended
 * NOT to include the base language in targets.
 */
public class PromptOptions {
    private String tone;
    private Integer maxLength;
    private String sourceLanguage;
    private List<String> targetLanguages;
    /**
     * If true, first enhance the source description in base language and include the base language in targets.
     * If false, do not enhance the source; translate the original source description and do not include base language.
     */
    private boolean enhanceSource;

    public String getTone() {
        return tone;
    }

    public void setTone(final String tone) {
        this.tone = tone;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(final Integer maxLength) {
        this.maxLength = maxLength;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(final String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public List<String> getTargetLanguages() {
        return targetLanguages;
    }

    public void setTargetLanguages(final List<String> targetLanguages) {
        this.targetLanguages = targetLanguages;
    }

    public boolean isEnhanceSource() {
        return enhanceSource;
    }

    public void setEnhanceSource(final boolean enhanceSource) {
        this.enhanceSource = enhanceSource;
    }
}
