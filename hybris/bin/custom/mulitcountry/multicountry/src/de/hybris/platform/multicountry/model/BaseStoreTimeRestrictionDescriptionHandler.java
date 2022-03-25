package de.hybris.platform.multicountry.model;

import de.hybris.platform.cms2.model.restrictions.CMSBaseStoreTimeRestrictionModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import de.hybris.platform.util.localization.Localization;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;



/**
 * @Autor Luca
 *
 */
public class BaseStoreTimeRestrictionDescriptionHandler implements
		DynamicAttributeHandler<String, CMSBaseStoreTimeRestrictionModel>
{
	@Override
	public String get(final CMSBaseStoreTimeRestrictionModel model)
	{
		String stores;
		if (CollectionUtils.isEmpty(model.getBaseStores()))
		{
			stores = "all stores";
		}
		else
		{
			stores = model.getBaseStores().stream().map(store -> store.getName()).collect(Collectors.joining(","));
		}
		final DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		final String from = model.getActiveFrom() == null ? null : format.format(model.getActiveFrom());
		final String until = model.getActiveUntil() == null ? null : format.format(model.getActiveUntil());

		final Object[] args = new Object[]
		{ stores, from, until };
		final String message = Localization.getLocalizedString("type.CMSBaseStoreTimeRestriction.descriptionPattern", args);
		return message == null ? "Applies to " + stores + " from " + from + " to " + until : message;
	}

	@Override
	public void set(final CMSBaseStoreTimeRestrictionModel model, final String s)
	{
		throw new UnsupportedOperationException();
	}
}
