/**
 *
 */
package de.hybris.platform.contextualattributevalues.services;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;


/**
 * @author i304605
 *
 */
public interface ContextualAttributeValuesSessionService
{
	/**
	 * Gets current context in the session.
	 *
	 * @return the current context in the session or null if not set
	 */
	ContextualAttributesContextModel getCurrentContext();

	/**
	 * Sets the current context to the session.
	 *
	 * @param context
	 *           the new region
	 */
	void setCurrentContext(ContextualAttributesContextModel context);
}
