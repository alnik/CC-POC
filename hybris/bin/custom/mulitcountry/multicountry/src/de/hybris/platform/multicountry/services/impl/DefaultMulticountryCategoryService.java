package de.hybris.platform.multicountry.services.impl;

import static de.hybris.platform.multicountry.constants.MulticountryConstants.ACTIVE_CATEGORY_CATALOG;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.impl.DefaultCategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.multicountry.strategies.MulticountryCategoryCatalogSelector;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

import org.springframework.beans.factory.annotation.Required;

/**
 * Created by i844957 on 5/4/16.
 */
public class DefaultMulticountryCategoryService extends DefaultCategoryService
{
	private BaseStoreService baseStoreService;
	private MulticountryCategoryCatalogSelector categoryCatalogSelector;

	/**
	 * Gets the category for code, respecting the selected default category catalog version (if set).
	 *
	 * @param code The code
	 * @return The category
	 */
	@Override
	public CategoryModel getCategoryForCode(final String code)
	{
		final CatalogVersionModel catalogVersion = getCategoryCatalogVersion();
		return catalogVersion == null
				? super.getCategoryForCode(code)
				: super.getCategoryForCode(catalogVersion, code);
	}

	/**
	 * Attempts to select a category catalog from the session, or from the default configuration of the site.
	 *
	 * @return A catalog version model, may be null
	 */
	protected CatalogVersionModel getCategoryCatalogVersion()
	{
		//get the category for code from the session-based category catalog
		final CatalogModel currentCatalog = getSessionService().getAttribute(ACTIVE_CATEGORY_CATALOG);
		if (currentCatalog != null)
		{
			return currentCatalog.getActiveCatalogVersion();
		}
		else
		{
			final BaseStoreModel baseStore = getBaseStoreService().getCurrentBaseStore();
			if (baseStore == null)
			{
				return null;
			}
			else
			{
				final CatalogModel catalog = getCategoryCatalogSelector().select(baseStore);
				return catalog == null ? null : catalog.getActiveCatalogVersion();
			}
		}
	}

	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	@Required
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
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
