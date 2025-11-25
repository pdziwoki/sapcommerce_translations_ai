package org.training.ai.dto.options;

import java.time.Duration;

/**
 * Configuration options for AI client calls
 */
public class AiClientOptions {
    private String model;
    private Duration timeout;

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(final Duration timeout) {
        this.timeout = timeout;
    }
}
