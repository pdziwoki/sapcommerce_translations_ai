/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved
 */
package org.training.ai.dto;

import java.util.List;

/**
 * Options for customizing the AI prompt for description enhancement
 */
public class PromptOptions {
    private String tone;
    private Integer maxLength;
    private boolean preserveTechnicalTerms = true;
    private List<String> keywordsToInclude;
    private String language;
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

    public boolean isPreserveTechnicalTerms() {
        return preserveTechnicalTerms;
    }

    public void setPreserveTechnicalTerms(final boolean preserveTechnicalTerms) {
        this.preserveTechnicalTerms = preserveTechnicalTerms;
    }

    public List<String> getKeywordsToInclude() {
        return keywordsToInclude;
    }

    public void setKeywordsToInclude(final List<String> keywordsToInclude) {
        this.keywordsToInclude = keywordsToInclude;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public java.util.List<String> getTargetLanguages() {
        return targetLanguages;
    }

    public void setTargetLanguages(final java.util.List<String> targetLanguages) {
        this.targetLanguages = targetLanguages;
    }
}
