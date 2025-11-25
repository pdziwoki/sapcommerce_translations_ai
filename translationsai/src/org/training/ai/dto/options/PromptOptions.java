package org.training.ai.dto.options;

import java.util.List;

/**
 * Options for customizing the AI prompt for description enhancement
 */
public class PromptOptions {
    private String tone;
    private Integer maxLength;
    private String sourceLanguage;
    private List<String> targetLanguages;

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
}
