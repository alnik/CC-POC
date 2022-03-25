/**
 *
 */
package de.hybris.platform.contextualattributevalues.services.impl;

import de.hybris.platform.contextualattributevalues.constants.ContextualattributevaluesConstants;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.contextualattributevalues.services.ContextualAttributeValuesSessionService;
import de.hybris.platform.servicelayer.internal.service.AbstractBusinessService;


/**
 * @author i304605
 *
 */
public class DefaultContextualAttributeValuesSessionService extends AbstractBusinessService
		implements ContextualAttributeValuesSessionService
{
	@Override
	public void setCurrentContext(final ContextualAttributesContextModel context)
	{
		getSessionService().setAttribute(ContextualattributevaluesConstants.CONTEXT_SESSION_ATTR_KEY, context);
	}

	@Override
	public ContextualAttributesContextModel getCurrentContext()
	{
		return getSessionService().getAttribute(ContextualattributevaluesConstants.CONTEXT_SESSION_ATTR_KEY);
	}
}
