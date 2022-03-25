/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2012 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *
 */
package de.hybris.platform.contextualattributevalues.facades.storesession.impl;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.contextualattributevalues.services.ContextualAttributeValuesSessionService;
import de.hybris.platform.multicountry.facades.storesession.impl.MulticountryStoreSessionFacade;
import de.hybris.platform.store.BaseStoreModel;

import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Extends the MulticountryStoreSessionFacade, for adding contextual contexts to the session when the later is
 * initialized.
 *
 */
public class ContextualAttributeValuesStoreSessionFacade extends MulticountryStoreSessionFacade
{
	private static final Logger LOG = Logger.getLogger(ContextualAttributeValuesStoreSessionFacade.class);

	private ContextualAttributeValuesSessionService contextualAttributeValuesSessionService;

	@Override
	public void initializeSession(final List<Locale> preferredLocales)
	{
		super.initializeSession(preferredLocales);
		//Get the current base store
		final BaseStoreModel currentBaseStore = getBaseStoreService().getCurrentBaseStore();
		if (currentBaseStore != null)
		{
			initializeContextualContext(currentBaseStore);
		}
	}

	protected void initializeContextualContext(final BaseStoreModel currentBaseStore)
	{
		// Get and save the contextual context for the current base store
		final ContextualAttributesContextModel currentContext = currentBaseStore.getContextualAttributesContext();
		getContextualAttributeValuesSessionService().setCurrentContext(currentContext);
	}

	/**
	 * @return the contextualAttributeValuesSessionService
	 */
	protected ContextualAttributeValuesSessionService getContextualAttributeValuesSessionService()
	{
		return contextualAttributeValuesSessionService;
	}

	/**
	 * @param contextualAttributeValuesSessionService
	 *           the contextualAttributeValuesSessionService to set
	 */
	@Required
	public void setContextualAttributeValuesSessionService(
			final ContextualAttributeValuesSessionService contextualAttributeValuesSessionService)
	{
		this.contextualAttributeValuesSessionService = contextualAttributeValuesSessionService;
	}

}
