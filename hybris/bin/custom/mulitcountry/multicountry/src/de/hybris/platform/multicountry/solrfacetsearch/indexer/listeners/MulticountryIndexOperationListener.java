/**
 *
 */
package de.hybris.platform.multicountry.solrfacetsearch.indexer.listeners;

import de.hybris.platform.multicountry.services.MulticountryRestrictionService;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchListener;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import org.springframework.beans.factory.annotation.Required;


/**
 * Class that should remove restrictions when indexing operations are executed within a Multi-Country catalog scenario
 *
 */
public class MulticountryIndexOperationListener implements IndexerBatchListener
{

	private MulticountryRestrictionService multicountryRestrictionService;

	@Override
	public void beforeBatch(final IndexerBatchContext batchContext) throws IndexerException
	{
		// We want to see also disabled product assignment, to index their online date to implement the
		// correct inheritance rule (if product assignment were filtered we would not be able to distinguish between
		// disabled assignment and non-existing assignments)
		multicountryRestrictionService.disableOnlineDateRestriction();
		multicountryRestrictionService.disableApprovalStatusRestriction();
	}

	@Override
	public void afterBatch(final IndexerBatchContext batchContext) throws IndexerException
	{
		// NOOP
	}

	@Override
	public void afterBatchError(final IndexerBatchContext batchContext) throws IndexerException
	{
		// NOOP
	}

	@Required
	public void setMulticountryRestrictionService(final MulticountryRestrictionService multicountryRestrictionService)
	{
		this.multicountryRestrictionService = multicountryRestrictionService;
	}

}
