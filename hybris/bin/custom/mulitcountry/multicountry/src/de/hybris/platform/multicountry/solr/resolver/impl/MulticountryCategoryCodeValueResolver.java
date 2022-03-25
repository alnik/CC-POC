package de.hybris.platform.multicountry.solr.resolver.impl;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commerceservices.search.solrfacetsearch.provider.CategorySource;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;


/**
 * Gets the category paths for product in a multi country assignment.
 *
 * For each CMSSite (and not availability group!!), the category path is calculated for the defaultCategoryCatalog
 * associated with the CMSSite, or otherwise, if none present, to the same catalog as the product.
 *
 */
public class MulticountryCategoryCodeValueResolver extends AbstractValueResolver<ProductModel, Collection<CMSSiteModel>, Object>
{
	private CategorySource categorySource;
	private ModelService modelService;
	private String propertyName;

	protected static final Logger LOG = Logger.getLogger(MulticountryCategoryCodeValueResolver.class);

	@Override
	protected Collection<CMSSiteModel> loadData(final IndexerBatchContext batchContext,
			final Collection<IndexedProperty> indexedProperties, final ProductModel model) throws FieldValueProviderException
	{
		final Set<CMSSiteModel> sites = internalGetAllPossibleSites(model);
		return sites;
	}

	/**
	 * Finds all the CMSSites to which this product can be associated via the Availability Group recursively
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
		Collection<String> categoryFieldValues = Sets.newHashSet();

		final Collection<CategoryModel> unfilteredCategories = categorySource.getCategoriesForConfigAndProperty(null, null,
				product);

		// Try to set the category catalog to the one linked to the site, otherwise just use the same one as the product
		// if there's not particular category catalog linked to the site.
		final String defaultCategoryCatalog = site.getDefaultCategoryCatalog() != null ? site.getDefaultCategoryCatalog().getId()
				: product.getCatalogVersion().getCatalog().getId();

		// And now filter only categories in the catalog linked to the site
		final Collection<CategoryModel> categoriesToUse = Sets.newHashSet();
		for (final CategoryModel category : unfilteredCategories)
		{
			final String catalogId = category.getCatalogVersion().getCatalog().getId();
			if (defaultCategoryCatalog.equals(catalogId))
			{
				categoriesToUse.add(category);
			}
		}

		if (categoriesToUse != null && !categoriesToUse.isEmpty())
		{
			categoryFieldValues = getCategoryFieldValues(categoriesToUse);
		}
		return categoryFieldValues;

	}

	protected Collection<String> getCategoryFieldValues(final Collection<CategoryModel> categories)
	{
		final Collection<String> results = Sets.newHashSet();
		for (final CategoryModel category : categories)
		{
			final String value = getPropertyValue(category, getPropertyName()).toString();
			results.add(value);
		}
		return results;
	}


	protected Object getPropertyValue(final Object model, final String propertyName)
	{
		return modelService.getAttributeValue(model, propertyName);
	}

	public CategorySource getCategorySource()
	{
		return categorySource;
	}

	public void setCategorySource(final CategorySource categorySource)
	{
		this.categorySource = categorySource;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public String getPropertyName()
	{
		return propertyName;
	}

	public void setPropertyName(final String propertyName)
	{
		this.propertyName = propertyName;
	}

}



