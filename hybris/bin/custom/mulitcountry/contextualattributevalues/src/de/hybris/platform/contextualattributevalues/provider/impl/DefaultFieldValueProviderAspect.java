/**
 *
 */
package de.hybris.platform.contextualattributevalues.provider.impl;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;


/**
 * @author i304605
 *
 */
public class DefaultFieldValueProviderAspect
{
	private static final Logger LOG = Logger.getLogger(DefaultFieldValueProviderAspect.class);

	public Object getProductAttribute(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		//TEST INTECEPTING FieldValueProvider Interface
		//Collection<FieldValue> getFieldValues(IndexConfig indexConfig, IndexedProperty indexedProperty, Object model)



		if (joinPoint.getArgs() == null || joinPoint.getArgs().length != 3)
		{
			return joinPoint.proceed();
		}
		if (!(joinPoint.getArgs()[2] instanceof ProductModel) && !(joinPoint.getArgs()[0] instanceof IndexConfig)
				&& !(joinPoint.getArgs()[0] instanceof IndexedProperty))
		{
			return joinPoint.proceed();
		}

		final Object[] args = joinPoint.getArgs();
		final ProductModel productModel = (ProductModel) args[2];
		final IndexedProperty indexedProperty = (IndexedProperty) args[1];
		final IndexConfig indexConfig = (IndexConfig) args[0];

		if (!productModel.getContextualAttributeValues().isEmpty())
		{

			if (LOG.isDebugEnabled())
			{
				LOG.debug(indexedProperty.isContextual());
				LOG.debug(productModel.getContextualAttributeValues().iterator().next().getContext());
				indexConfig.getLanguages();
			}
			if (indexedProperty.getExportId().equals("description"))
			{
				indexedProperty.setExportId(indexedProperty.getExportId() + "_UK-availability");
				args[1] = indexedProperty;
				productModel.setDescription(productModel.getContextualAttributeValues().iterator().next().getDescription());
				args[2] = productModel;
			}
		}

		final Object result = joinPoint.proceed(args);
		return result;


	}



}
