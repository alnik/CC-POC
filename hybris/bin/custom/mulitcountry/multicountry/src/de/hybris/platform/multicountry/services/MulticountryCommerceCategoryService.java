package de.hybris.platform.multicountry.services;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commerceservices.category.CommerceCategoryService;

/**
 * In order to implement a "category catalog" that allows a base store to have a shared product catalog and a custom product and
 * category catalog, we implement a customization of the CommerceCategoryService that allows us to find a category for a code
 * within a specific catalog version.
 */
public interface MulticountryCommerceCategoryService extends CommerceCategoryService
{
	/**
	 * Finds a category for a given code and catalog version.
	 *
	 * @param code The category code
	 * @param catalogVersion The catalog version
	 * @return Returns the category corresponding to the code from the provided catalog version, or if none is provided, from the
	 * default product catalog for the current store.
	 */
	CategoryModel getCategoryForCode(final String code, final CatalogVersionModel catalogVersion);
}
