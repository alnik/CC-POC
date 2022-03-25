/*
 *  
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.commercesearchbackoffice.jalo;

import de.hybris.commercesearchbackoffice.constants.MulticountrybackofficeConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.apache.log4j.Logger;

public class MulticountrybackofficeManager extends GeneratedMulticountrybackofficeManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( MulticountrybackofficeManager.class.getName() );
	
	public static final MulticountrybackofficeManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (MulticountrybackofficeManager) em.getExtension(MulticountrybackofficeConstants.EXTENSIONNAME);
	}
	
}
