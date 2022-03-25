package com.hybris.multicountry.widgets.backoffice.jalo;

import com.hybris.multicountry.widgets.backoffice.constants.MulticountrywidgetsbackofficeConstants;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import org.apache.log4j.Logger;

public class MulticountrywidgetsbackofficeManager extends GeneratedMulticountrywidgetsbackofficeManager
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( MulticountrywidgetsbackofficeManager.class.getName() );
	
	public static final MulticountrywidgetsbackofficeManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (MulticountrywidgetsbackofficeManager) em.getExtension(MulticountrywidgetsbackofficeConstants.EXTENSIONNAME);
	}
	
}
