/**
 *
 */
package de.hybris.platform.contextualattributevalues.converters.populator;

import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.contextualattributevalues.jalo.ContextualAttributeValue;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributeValueModel;
import de.hybris.platform.contextualattributevalues.services.ContextualAttributeValuesSessionService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Required;


/**
 * @author i304605
 *
 */
public abstract class AbstractContextualProductPopulator<SOURCE extends ProductModel, TARGET extends ProductData>
		implements Populator<SOURCE, TARGET>
{
	private ContextualAttributeValuesSessionService contextualAttributeValuesSessionService;
	private ModelService modelService;

	protected Object getProductAttribute(final ProductModel productModel, final String attribute)
	{
		Object defaultValue = getModelService().getAttributeValue(productModel, attribute);
		Object contextualValue = null;
		if (defaultValue == null && productModel instanceof VariantProductModel)
		{
			final ProductModel baseProduct = ((VariantProductModel) productModel).getBaseProduct();
			if (baseProduct != null)
			{
				defaultValue = getProductAttribute(baseProduct, attribute);
			}
		}

		if (!productModel.getContextualAttributeValues().isEmpty()
				|| contextualAttributeValuesSessionService.getCurrentContext() != null)
		{
			for (final ContextualAttributeValueModel contextualModel : productModel.getContextualAttributeValues())
			{

				if (contextualModel.getContext().equals(contextualAttributeValuesSessionService.getCurrentContext())
						&& attribute != ProductModel.CATALOGVERSION)
				{

					final ContextualAttributeValue contextualAttributeJalo = modelService.getSource(contextualModel);
					try
					{
						if (contextualAttributeJalo.getAllAttributes().containsKey(attribute))
						{
							contextualValue = getModelService().getAttributeValue(contextualModel, attribute);
						}
					}
					catch (JaloInvalidParameterException | JaloSecurityException e)
					{
						contextualValue = null;
					}

				}
			}

		}

		if (contextualValue != null)
		{
			if (contextualValue instanceof Collection && ((Collection) contextualValue).isEmpty())
			{
				contextualValue = null;
			}

		}

		return contextualValue != null ? contextualValue : defaultValue;
	}


	/**
	 * Convert the object value to a string. If the object is null it is converted to a blank string.
	 *
	 * @param value
	 *           the value to convert
	 * @return the value string
	 */
	protected String safeToString(final Object value)
	{
		return (value == null) ? "" : value.toString();
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the contextualAttributeValuesSessionService
	 */
	protected ContextualAttributeValuesSessionService getContextualAttributeValuesSessionService()
	{
		return contextualAttributeValuesSessionService;
	}

	/**
	 * @param contextualAttributeValuesSessionService
	 *           the contextualAttributeValuesSessionService to set
	 */
	@Required
	public void setContextualAttributeValuesSessionService(
			final ContextualAttributeValuesSessionService contextualAttributeValuesSessionService)
	{
		this.contextualAttributeValuesSessionService = contextualAttributeValuesSessionService;
	}
}
