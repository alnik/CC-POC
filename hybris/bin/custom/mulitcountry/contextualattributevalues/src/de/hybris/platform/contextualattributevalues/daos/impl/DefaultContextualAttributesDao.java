/**
 *
 */
package de.hybris.platform.contextualattributevalues.daos.impl;

import de.hybris.platform.contextualattributevalues.daos.ContextualAttributesDao;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

import java.util.List;


/**
 * @author i307088
 *
 */
public class DefaultContextualAttributesDao extends AbstractItemDao implements ContextualAttributesDao
{
	@Override
	public List<ContextualAttributesContextModel> findAllAvailableContexts()
	{
		final FlexibleSearchQuery fsq = new FlexibleSearchQuery(
				"SELECT {pk} FROM {" + ContextualAttributesContextModel._TYPECODE + "}");
		return getFlexibleSearchService().<ContextualAttributesContextModel> search(fsq).getResult();
	}

	@Override
	public ContextualAttributesContextModel findContextByCode(final String code)
	{
		final FlexibleSearchQuery fsq = new FlexibleSearchQuery("SELECT {pk} FROM {" + ContextualAttributesContextModel._TYPECODE
				+ "} WHERE {" + ContextualAttributesContextModel.CODE + "} = ?code");
		fsq.addQueryParameter("code", code);
		final List<ContextualAttributesContextModel> result = getFlexibleSearchService()
				.<ContextualAttributesContextModel> search(fsq).getResult();
		if (result.isEmpty())
		{
			return null;
		}
		return result.get(0);
	}
}
