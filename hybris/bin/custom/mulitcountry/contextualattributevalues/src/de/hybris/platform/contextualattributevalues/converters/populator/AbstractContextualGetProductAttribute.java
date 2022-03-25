/**
 *
 */
package de.hybris.platform.contextualattributevalues.converters.populator;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributeValueModel;
import de.hybris.platform.contextualattributevalues.services.ContextualAttributeValuesSessionService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Required;


/**
 * @author i304605
 *
 */
public abstract class AbstractContextualGetProductAttribute
{
	private ContextualAttributeValuesSessionService contextualAttributeValuesSessionService;
	private ModelService modelService;
	private TypeService typeService;

	public Object getProductAttribute(final ProductModel productModel, final String attribute)
	{
		final ComposedTypeModel ct = getTypeService().getComposedTypeForClass(ContextualAttributeValueModel.class);

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

					if (getTypeService().hasAttribute(ct, attribute))
					{
						contextualValue = getModelService().getAttributeValue(contextualModel, attribute);
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
	 * @return the typeService
	 */
	protected TypeService getTypeService()
	{
		return typeService;
	}

	/**
	 * @param typeService
	 *           the typeService to set
	 */
	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
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
