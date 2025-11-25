package org.training.ai.dto.response;

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
