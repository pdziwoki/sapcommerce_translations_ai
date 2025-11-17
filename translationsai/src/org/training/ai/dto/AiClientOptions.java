/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved
 */
package org.training.ai.dto;

import java.time.Duration;

/**
 * Configuration options for AI client calls
 */
public class AiClientOptions
{
	private String model;
	private Double temperature;
	private Integer maxTokens;
	private Duration timeout;

	public String getModel()
	{
		return model;
	}

	public void setModel(final String model)
	{
		this.model = model;
	}

	public Double getTemperature()
	{
		return temperature;
	}

	public void setTemperature(final Double temperature)
	{
		this.temperature = temperature;
	}

	public Integer getMaxTokens()
	{
		return maxTokens;
	}

	public void setMaxTokens(final Integer maxTokens)
	{
		this.maxTokens = maxTokens;
	}

	public Duration getTimeout()
	{
		return timeout;
	}

	public void setTimeout(final Duration timeout)
	{
		this.timeout = timeout;
	}
}
