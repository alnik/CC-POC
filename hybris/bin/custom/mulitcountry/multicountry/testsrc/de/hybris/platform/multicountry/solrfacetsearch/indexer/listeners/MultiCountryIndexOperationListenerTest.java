/**
 *
 */
package de.hybris.platform.multicountry.solrfacetsearch.indexer.listeners;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.multicountry.services.MulticountryRestrictionService;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import org.junit.Test;
import org.mockito.Mockito;


/**
 * Test class that should remove restrictions when indexing operations are executed within a Multi-Country catalog
 * scenario.
 */
@UnitTest
public class MultiCountryIndexOperationListenerTest
{
	@Test
	public void assertRestrictionsTurnedOff() throws IndexerException
	{
		final MulticountryRestrictionService multiCountryRestrictionService = Mockito.mock(MulticountryRestrictionService.class);
		final MulticountryIndexOperationListener multiCountryIndexOperationListener = new MulticountryIndexOperationListener();
		final IndexerBatchContext batchContext = Mockito.mock(IndexerBatchContext.class);

		multiCountryIndexOperationListener.setMulticountryRestrictionService(multiCountryRestrictionService);


		multiCountryIndexOperationListener.beforeBatch(batchContext);
		Mockito.verify(multiCountryRestrictionService).disableApprovalStatusRestriction();
		Mockito.verify(multiCountryRestrictionService).disableOnlineDateRestriction();
	}

}