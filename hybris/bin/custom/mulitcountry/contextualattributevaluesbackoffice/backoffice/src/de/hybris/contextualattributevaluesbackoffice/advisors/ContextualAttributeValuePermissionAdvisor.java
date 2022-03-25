/**
 *
 */
package de.hybris.contextualattributevaluesbackoffice.advisors;

import de.hybris.multicountry.backoffice.advisors.AbstractMultiCountryInstancePermissionAdvisor;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributeValueModel;
import de.hybris.platform.contextualattributevalues.util.ContextualAttributeValuesUtil;



/**
 * @author cyrill.pedol@sap.com
 *
 */
public class ContextualAttributeValuePermissionAdvisor
		extends AbstractMultiCountryInstancePermissionAdvisor<ContextualAttributeValueModel>
{


	@Override
	public boolean isApplicableTo(final Object instance)
	{
		return instance instanceof ContextualAttributeValueModel;
	}

	@Override
	protected boolean isObjectWritable(final ContextualAttributeValueModel taxRowModel)
	{
		return ContextualAttributeValuesUtil.canEditContextualAttributeValue(taxRowModel);
	}

}
