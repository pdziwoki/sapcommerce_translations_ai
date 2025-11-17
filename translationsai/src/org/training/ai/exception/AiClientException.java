/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved
 */
package org.training.ai.exception;

/**
 * Exception thrown when AI client operations fail
 */
public class AiClientException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public AiClientException(final String message)
	{
		super(message);
	}

	public AiClientException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
