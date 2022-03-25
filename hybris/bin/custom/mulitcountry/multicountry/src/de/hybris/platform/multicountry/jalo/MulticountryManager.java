/*
 *  
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.multicountry.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import de.hybris.platform.multicountry.constants.MulticountryConstants;
import org.apache.log4j.Logger;

public class MulticountryManager extends GeneratedMulticountryManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( MulticountryManager.class.getName() );
	
	public static final MulticountryManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (MulticountryManager) em.getExtension(MulticountryConstants.EXTENSIONNAME);
	}
	
}
