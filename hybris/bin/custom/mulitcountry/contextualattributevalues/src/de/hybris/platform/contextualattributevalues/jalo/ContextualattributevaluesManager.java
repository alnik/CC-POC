/*
 *  
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.contextualattributevalues.jalo;

import de.hybris.platform.contextualattributevalues.constants.ContextualattributevaluesConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.apache.log4j.Logger;

public class ContextualattributevaluesManager extends GeneratedContextualattributevaluesManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( ContextualattributevaluesManager.class.getName() );
	
	public static final ContextualattributevaluesManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (ContextualattributevaluesManager) em.getExtension(ContextualattributevaluesConstants.EXTENSIONNAME);
	}
	
}
