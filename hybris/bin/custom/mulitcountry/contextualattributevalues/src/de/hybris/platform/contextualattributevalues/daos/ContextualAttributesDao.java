/**
 *
 */
package de.hybris.platform.contextualattributevalues.daos;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;

import java.util.List;


/**
 * @author i307088
 *
 */
public interface ContextualAttributesDao
{

	/**
	 * Finds all available ContextualAttributesContext.
	 *
	 * @return A list of ContextualAttributesContext
	 */
	List<ContextualAttributesContextModel> findAllAvailableContexts();

	/**
	 * Finds the first ContextualAttributesContext with a specific code.
	 *
	 * @param code
	 *           The code to search for.
	 * @return A ContextualAttributesContext matching the code, or null if none was be found.
	 */
	ContextualAttributesContextModel findContextByCode(String code);

}
