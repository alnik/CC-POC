package de.hybris.platform.multicountry.solr.resolver.impl;

/*
 * [y] SAP Hybris Platform
 *
 * Copyright (c) 2000-2017 SAP SE
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP SE
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */

import de.hybris.platform.catalog.model.classification.ClassificationClassModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;


/**
 * Gets the category paths for product in a multi country assignment.
 *
 * For each CMSSite (and not availability group!!), the category path is calculated for the defaultCategoryCatalog
 * associated with the CMSSite, or otherwise, if none present, to the same catalog as the product.
 *
 */
public class MulticountryCategoryPathValueResolver extends AbstractValueResolver<ProductModel, Collection<CMSSiteModel>, Object>
{
	private CategoryService categoryService;

	protected static final Logger LOG = Logger.getLogger(MulticountryCategoryPathValueResolver.class);

	@Override
	protected Collection<CMSSiteModel> loadData(final IndexerBatchContext batchContext,
			final Collection<IndexedProperty> indexedProperties, final ProductModel model) throws FieldValueProviderException
	{
		final Set<CMSSiteModel> sites = internalGetAllPossibleSites(model);
		return sites;
	}

	/**
	 * Finds all the CMSSites to which this product can be associated via the Availability Group
	 *
	 * @param product
	 * @return Set<CMSSiteModel>
	 */
	@SuppressWarnings("unchecked")
	protected Set<CMSSiteModel> internalGetAllPossibleSites(final ProductModel product)
	{
		final Set<CMSSiteModel> sites;

		// If this is a variant, recursively collect the parent sites from availability groups
		if (product instanceof VariantProductModel)
		{
			sites = internalGetAllPossibleSites(((VariantProductModel) product).getBaseProduct());
			// for each availability, find the basestores. For each base store, find the sites.
			for (final ProductAvailabilityAssignmentModel availability : product.getAvailability())
			{
				final Set<BaseStoreModel> baseStoresForProduct = availability.getAvailabilityGroup().getStores();
				for (final BaseStoreModel baseStoreModel : baseStoresForProduct)
				{
					sites.addAll((Collection<CMSSiteModel>) (Collection<?>) baseStoreModel.getCmsSites());
				}
			}
		}
		else
		{
			// ..else, collect all active groups
			sites = Sets.newHashSet();
			if (product.getAvailability() != null && !product.getAvailability().isEmpty())
			{
				for (final ProductAvailabilityAssignmentModel availability : product.getAvailability())
				{
					final Set<BaseStoreModel> baseStoresForProduct = availability.getAvailabilityGroup().getStores();
					for (final BaseStoreModel baseStoreModel : baseStoresForProduct)
					{
						sites.addAll((Collection<CMSSiteModel>) (Collection<?>) baseStoreModel.getCmsSites());
					}
				}
			}
		}
		return sites;
	}

	@Override
	protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final ProductModel model,
			final ValueResolverContext<Collection<CMSSiteModel>, Object> resolverContext) throws FieldValueProviderException
	{
		{
			// We look up to all the variant and parent groups!
			// If the variant does not have a value, we should look it up on its base product (or base product's base product)
			final Collection<CMSSiteModel> sites = resolverContext.getData();
			for (final CMSSiteModel site : sites)
			{
				final Object fieldValue = getPropertyValue(model, site);
				//Get the name(s) of the index field(s)...
				document.addField(indexedProperty, fieldValue, site.getUid());
			}
		}
	}

	/**
	 * Return the value of a property defined in {@link IndexedProperty}. If the property is null we look at the value on
	 * the base product.
	 *
	 * @param product
	 * @param site
	 * @return the property value to index (a collection of strings in this case)
	 */
	private Object getPropertyValue(final ProductModel product, final CMSSiteModel site)
	{
		Collection<String> categoryPaths = Sets.newHashSet();

		// Try to set the category catalog to the one linked to the site, otherwise just use the same one as the product
		// if there's not particular category catalog linked to the site.
		final String defaultCategoryCatalog = site.getDefaultCategoryCatalog() != null ? site.getDefaultCategoryCatalog().getId()
				: product.getCatalogVersion().getCatalog().getId();

		// Get's the categories linked to the product (regardless of catalog)
		final Collection<CategoryModel> allCategories = product.getSupercategories();

		// And now filter only categories in the catalog linked to the site
		final Collection<CategoryModel> categoriesToUse = Sets.newHashSet();
		for (final CategoryModel category : allCategories)
		{
			final String catalogId = category.getCatalogVersion().getCatalog().getId();
			if (defaultCategoryCatalog.equals(catalogId))
			{
				categoriesToUse.add(category);
			}
		}

		if (categoriesToUse != null && !categoriesToUse.isEmpty())
		{
			categoryPaths = getCategoryPaths(categoriesToUse);
		}
		return categoryPaths;
	}

	private Set<String> getCategoryPaths(final Collection<CategoryModel> categories)
	{
		final Set<String> allPaths = new HashSet<String>();

		for (final CategoryModel category : categories)
		{
			if (!(category instanceof ClassificationClassModel))
			{
				final Collection<List<CategoryModel>> pathsForCategory = getCategoryService().getPathsForCategory(category);
				if (pathsForCategory != null)
				{
					for (final List<CategoryModel> categoryPath : pathsForCategory)
					{
						accumulateCategoryPaths(categoryPath, allPaths);
					}
				}
			}
		}

		return allPaths;
	}

	private void accumulateCategoryPaths(final List<CategoryModel> categoryPath, final Set<String> output)
	{
		final StringBuilder accumulator = new StringBuilder();
		for (final CategoryModel category : categoryPath)
		{
			if (category instanceof ClassificationClassModel)
			{
				break;
			}
			accumulator.append('/').append(category.getCode());
			output.add(accumulator.toString());
		}
	}

	protected CategoryService getCategoryService()
	{
		return categoryService;
	}

	@Required
	public void setCategoryService(final CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}


}



