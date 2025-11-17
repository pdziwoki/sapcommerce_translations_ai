/*
 * Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved
 */
package org.training.jalo;

import org.training.constants.TranslationsaiConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;

public class TranslationsaiManager extends GeneratedTranslationsaiManager
{
	public static final TranslationsaiManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (TranslationsaiManager) em.getExtension(TranslationsaiConstants.EXTENSIONNAME);
	}
	
}
