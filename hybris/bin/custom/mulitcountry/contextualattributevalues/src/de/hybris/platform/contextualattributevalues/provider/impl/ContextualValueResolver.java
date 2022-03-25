package de.hybris.platform.contextualattributevalues.provider.impl;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributeValueModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * * Gets all the attribute values discriminated by region. Since the PriceService filters the price rows depending on
 * the session user, this implementation gets directly the pricerows from the product itself. This is more performant
 * than asking the service one by one the price for each currency/upg combination (and this is usually performed for two
 * fields, one for the prices and one for the ranges). The drawback is that (a) prices should be defined by product
 * (this class will not index price rows defined, for example, only for a product group); and (b) no gross/net
 * calculation is performed: value is indexed as-is.
 *
 */
public class ContextualValueResolver
		extends AbstractValueResolver<ProductModel, Collection<ContextualAttributeValueModel>, Object>
{
	protected static final Logger LOG = Logger.getLogger(ContextualValueResolver.class);

	private ModelService modelService;

	@Override
	protected Collection<ContextualAttributeValueModel> loadData(final IndexerBatchContext batchContext,
			final Collection<IndexedProperty> indexedProperties, final ProductModel model) throws FieldValueProviderException
	{
		final Collection<ContextualAttributeValueModel> attributeValueModels = model.getContextualAttributeValues();
		return attributeValueModels == null ? new ArrayList<ContextualAttributeValueModel>() : attributeValueModels;

	}

	@Override
	protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final ProductModel model,
			final ValueResolverContext<Collection<ContextualAttributeValueModel>, Object> resolverContext)
					throws FieldValueProviderException
	{

		final Collection<ContextualAttributeValueModel> attributeValueModels = resolverContext.getData();

		if (attributeValueModels.isEmpty())
		{
			//USE DEFAULT VALUE RESOLVER/PROVIDER PER TYPE

			//final Object attributeValue = modelService.getAttributeValue(model, indexedProperty.getExportId());
			//document.addField(indexedProperty, attributeValue);

		}
		else
		{
			for (final ContextualAttributeValueModel attributeValueModel : attributeValueModels)
			{

				final String fieldSuffix = fieldNameToIndex(attributeValueModel);

				final Object attributeValue = getModelService().getAttributeValue(attributeValueModel, indexedProperty.getExportId());

				document.addField(indexedProperty, attributeValue, fieldSuffix);

			}
		}
	}

	protected String fieldNameToIndex(final ContextualAttributeValueModel attributeValueModel)
	{

		//final HybrisEnumValue context = attributeValueModel.getContext();
		//return context == null ? "" : context.getCode();
		return null;
	}


	/**
	 * @return the modelService
	 */
	protected ModelService getModelService()
	{
		return modelService;
	}

	/**
	 * @param modelService
	 *           the modelService to set
	 */
	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
