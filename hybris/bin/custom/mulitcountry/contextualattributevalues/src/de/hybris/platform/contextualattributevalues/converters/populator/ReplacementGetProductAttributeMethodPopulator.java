/**
 *
 */
package de.hybris.platform.contextualattributevalues.converters.populator;

import de.hybris.platform.core.model.product.ProductModel;

import java.lang.reflect.Method;

import org.springframework.beans.factory.support.MethodReplacer;


/**
 * @author i304605
 */
public class ReplacementGetProductAttributeMethodPopulator extends AbstractContextualGetProductAttribute implements MethodReplacer
{
	@Override
	public Object reimplement(final Object o, final Method m, final Object[] args) throws Throwable
	{
		final ProductModel productModel = (ProductModel) args[0];
		final String attribute = (String) args[1];

		return getProductAttribute(productModel, attribute);
	}
}