/**
 *
 */
package de.hybris.multicountry.backoffice.search.adapters;

import java.util.Optional;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.searchadapters.CatalogTreeFilterAdvancedSearchInitializer;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public class BaseStoreTreeFilterAdvancedSearchInitializer extends CatalogTreeFilterAdvancedSearchInitializer
{


	@Override
	public void addSearchDataConditions(final AdvancedSearchData searchData, final Optional<NavigationNode> navigationNode)
	{
		if (navigationNode.isPresent())
		{
			final NavigationNode node = navigationNode.get();
			getConditionsAdapters().stream().filter((adapter) -> {
				return adapter.canHandle(node);
			}).forEach((adapter) -> {
				adapter.addSearchCondition(searchData, node);
			});
		}

	}

}
