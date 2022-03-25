/**
 *
 */
package de.hybris.platform.multicountry.solr.indexer.workers.impl;

import static de.hybris.platform.multicountry.constants.MulticountryConstants.AVAILABILITY_GROUPS;

import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.solrfacetsearch.indexer.workers.IndexerWorkerParameters;
import de.hybris.platform.solrfacetsearch.indexer.workers.impl.DefaultIndexerWorker;

import java.util.Collection;

import org.apache.log4j.Logger;


/**
 * @author i314082
 *
 */
public class MulticountryIndexerWorker extends DefaultIndexerWorker
{
	private static final Logger LOG = Logger.getLogger(MulticountryIndexerWorker.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.solrfacetsearch.indexer.workers.impl.DefaultIndexerWorker#isInitialized() Redefining
	 * workerParameters because super.workerParameters can't be reached
	 */
	private IndexerWorkerParameters workerParameters;

	@Override
	public void initialize(final IndexerWorkerParameters workerParameters)
	{

		super.initialize(workerParameters);
		this.workerParameters = workerParameters;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.solrfacetsearch.indexer.workers.impl.DefaultIndexerWorker#isInitialized() Adding
	 * abailavilityGroups to the worker's session.
	 */
	@Override
	protected void initializeSession()
	{

		super.initializeSession();

		final Collection<ProductAvailabilityGroupModel> availabilityGroups = this.workerParameters.getSessionAvailabilityGroups();
		final Session session = getSessionService().getCurrentSession();
		session.setAttribute(AVAILABILITY_GROUPS, availabilityGroups);

	}

}