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
package de.hybris.platform.multicountry.facades.storesession.impl;

import static de.hybris.platform.multicountry.constants.MulticountryConstants.ACTIVE_CATEGORY_CATALOG;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.commercefacades.storesession.impl.DefaultStoreSessionFacade;
import de.hybris.platform.europe1.constants.Europe1Constants;
import de.hybris.platform.europe1.enums.UserPriceGroup;
import de.hybris.platform.multicountry.enums.TimezoneEnum;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.multicountry.services.MulticountryRestrictionService;
import de.hybris.platform.multicountry.strategies.MulticountryCategoryCatalogSelector;
import de.hybris.platform.multicountry.util.TimeZoneHelper;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Extends the DefaultStoreSessionFacade, for adding the user price group to the session when the later is initialized.
 *
 */
public class MulticountryStoreSessionFacade extends DefaultStoreSessionFacade
{
	private static final Logger LOG = Logger.getLogger(MulticountryStoreSessionFacade.class);

	private MulticountryRestrictionService multicountryRestrictionService;
	private TimeService timeService;
	private MulticountryCategoryCatalogSelector categoryCatalogSelector;


	@Override
	public void initializeSession(final List<Locale> preferredLocales)
	{
		super.initializeSession(preferredLocales);
		//Get the current base store
		final BaseStoreModel currentBaseStore = getBaseStoreService().getCurrentBaseStore();
		if (currentBaseStore != null)
		{
			initializeSessionPriceGroup(currentBaseStore);
			initializeSessionProductAssignment(currentBaseStore);
			// set the timezone and timezone offset for this session

			initializeTimezone(currentBaseStore);
			initializeCategoryCatalog(currentBaseStore);

		}
	}

	protected void initializeSessionProductAssignment(final BaseStoreModel currentBaseStore)
	{
		// Get and save the availability groups for the current base store
		final Collection<ProductAvailabilityGroupModel> availabilityGroups = currentBaseStore.getAvailabilityGroups();
		getMulticountryRestrictionService().setCurrentProductAvailabilityGroups(availabilityGroups);
	}


	protected void initializeCategoryCatalog(final BaseStoreModel currentBaseStore)
	{
		final CatalogModel catalog = getCategoryCatalogSelector().select(currentBaseStore);
		if (catalog == null)
		{
			LOG.info("No category catalog was found for " + currentBaseStore.getName());
		}
		else
		{
			LOG.info("Setting category catalog " + catalog.getName() + " in session for store " + currentBaseStore.getName());
			getSessionService().setAttribute(ACTIVE_CATEGORY_CATALOG, catalog);
		}
	}

	/**
	 * TODO put logic into a service so it can be reused
	 *
	 * @param currentBaseStore
	 */
	protected void initializeTimezone(final BaseStoreModel currentBaseStore)
	{
		if (currentBaseStore.getTimezone() != null)
		{
			final TimezoneEnum storeTimeZone = currentBaseStore.getTimezone();
			final TimeZone timeZone = TimeZone.getTimeZone(storeTimeZone.getCode());

			getSessionService().getCurrentSession().setAttribute("timezone", timeZone);

			final int timeZoneOffsetDifferential = TimeZoneHelper.getTimeZoneOffsetDifferential(storeTimeZone.getCode());
			getTimeService().setTimeOffset(timeZoneOffsetDifferential);
		}
	}

	protected void initializeSessionPriceGroup(final BaseStoreModel currentBaseStore)
	{
		//Get the price group assigned to the current base store...
		final UserPriceGroup priceGroup = currentBaseStore.getUserPriceGroup();
		if (priceGroup == null)
		{
			LOG.warn("Store [" + currentBaseStore.getUid() + "] doesn't have a UserPriceGroup");
			return; //no price group?? error?
		}

		//... set price group into the session.
		getSessionService().setAttribute(Europe1Constants.PARAMS.UPG, priceGroup);

	}

	public MulticountryRestrictionService getMulticountryRestrictionService()
	{
		return multicountryRestrictionService;
	}

	@Required
	public void setMulticountryRestrictionService(final MulticountryRestrictionService multicountryRestrictionService)
	{
		this.multicountryRestrictionService = multicountryRestrictionService;
	}

	public TimeService getTimeService()
	{
		return timeService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}

	public MulticountryCategoryCatalogSelector getCategoryCatalogSelector()
	{
		return categoryCatalogSelector;
	}

	@Required
	public void setCategoryCatalogSelector(final MulticountryCategoryCatalogSelector categoryCatalogSelector)
	{
		this.categoryCatalogSelector = categoryCatalogSelector;
	}
}
