package de.hybris.platform.multicountry.strategies.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemModel;
import de.hybris.platform.cms2.model.contents.ContentCatalogModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.multicountry.strategies.MulticountryCategoryCatalogSelector;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by i844957 on 5/4/16.
 */
public class DefaultMulticountryCategoryCatalogSelector implements MulticountryCategoryCatalogSelector
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultMulticountryCategoryCatalogSelector.class);

	@Override
	public CatalogModel select(final BaseStoreModel baseStore)
	{
		validateParameterNotNull(baseStore, "There must always be a base store in the session!");
		LOG.debug("Selecting a category catelog for base store {}", baseStore.getName());

		final Collection<BaseSiteModel> cmsSites = baseStore.getCmsSites();
		if (CollectionUtils.isEmpty(cmsSites))
		{
			LOG.debug("Base store {} has no sites", baseStore.getName());
			return null;
		}

		final BaseSiteModel baseSite = cmsSites.iterator().next();
		if (!(baseSite instanceof CMSSiteModel))
		{
			LOG.debug("Base store {} has no cms sites", baseStore.getName());
			return null;
		}

		final CMSSiteModel siteModel = (CMSSiteModel) baseSite;
		if (siteModel.getDefaultCategoryCatalog() == null)
		{
			final CatalogModel catalog = baseStore.getCatalogs().stream()
					.filter(cat -> !(cat instanceof ContentCatalogModel) || !(cat instanceof ClassificationSystemModel)).findFirst()
					.get();

			if (catalog == null)
			{
				LOG.debug("No active category catalog selected for {}", baseStore.getName());
				return null;
			}
			else
			{
				LOG.debug("Selecting active category catalog {} for {}", catalog.getName(), baseStore.getName());
				return catalog;
			}
		}
		else
		{
			LOG.debug("Using the default category catalog {} for store {}", siteModel.getDefaultCategoryCatalog().getName(),
					  baseStore.getName());
			return siteModel.getDefaultCategoryCatalog();
		}
	}
}
