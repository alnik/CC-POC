/*
 *  
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.contextualattributevaluesbackoffice.jalo;

import de.hybris.contextualattributevaluesbackoffice.constants.ContextualattributevaluesbackofficeConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.apache.log4j.Logger;

public class ContextualattributevaluesbackofficeManager extends GeneratedContextualattributevaluesbackofficeManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( ContextualattributevaluesbackofficeManager.class.getName() );
	
	public static final ContextualattributevaluesbackofficeManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (ContextualattributevaluesbackofficeManager) em.getExtension(ContextualattributevaluesbackofficeConstants.EXTENSIONNAME);
	}
	
}
