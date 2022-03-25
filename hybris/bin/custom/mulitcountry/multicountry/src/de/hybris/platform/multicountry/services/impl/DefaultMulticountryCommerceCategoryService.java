package de.hybris.platform.multicountry.services.impl;

import de.hybris.platform.catalog.impl.CatalogUtils;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commerceservices.category.impl.DefaultCommerceCategoryService;
import de.hybris.platform.multicountry.services.MulticountryCommerceCategoryService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.util.Collection;
import java.util.Collections;

import org.springframework.util.Assert;


/**
 * Created by i844957 on 4/28/16.
 */
public class DefaultMulticountryCommerceCategoryService extends DefaultCommerceCategoryService
		implements MulticountryCommerceCategoryService
{
	@Override
	public CategoryModel getCategoryForCode(final String code, final CatalogVersionModel catalogVersion)
	{
		Assert.notNull(code);
		Assert.notNull(catalogVersion);

		final Collection<CategoryModel> categoriesForCode = getCategoryService().getCategoriesForCode(code);
		for (final CategoryModel categoryModel : categoriesForCode)
		{
			if (categoryModel.getCatalogVersion().equals(catalogVersion))
			{
				return categoryModel;
			}
		}

		throw new UnknownIdentifierException("Category with code '" + code + "' not found in '"
				+ CatalogUtils.getCatalogVersionsString(Collections.singletonList(catalogVersion)) + "'");
	}
}
