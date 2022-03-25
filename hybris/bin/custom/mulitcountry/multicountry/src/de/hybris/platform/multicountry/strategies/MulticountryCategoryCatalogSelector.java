package de.hybris.platform.multicountry.strategies;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.store.BaseStoreModel;

/**
 * Created by i844957 on 5/4/16.
 */
public interface MulticountryCategoryCatalogSelector
{
	/**
	 * Select a catalog.
	 *
	 * @param baseStore The base store
	 * @return The catalog, may be null
	 */
	CatalogModel select(final BaseStoreModel baseStore);
}
