/**
 *
 */
package de.hybris.platform.multicountry.solr.indexer.strategies.impl;

import static de.hybris.platform.multicountry.constants.MulticountryConstants.AVAILABILITY_GROUPS;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.indexer.IndexerContext;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.strategies.impl.DefaultIndexerStrategy;
import de.hybris.platform.solrfacetsearch.indexer.workers.IndexerWorker;
import de.hybris.platform.solrfacetsearch.indexer.workers.IndexerWorkerParameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;


/**
 * @author i314082
 *
 */
public class MulticountryIndexerStrategy extends DefaultIndexerStrategy
{

	private static final Logger LOG = Logger.getLogger(MulticountryIndexerStrategy.class);

	@Override
	protected IndexerWorker createIndexerWorker(final IndexerContext indexerContext, final long workerNumber,
			final List<PK> workerPks) throws IndexerException
	{
		final Collection<String> indexedProperties = new ArrayList<>();
		for (final IndexedProperty indexedProperty : indexerContext.getIndexedProperties())
		{
			indexedProperties.add(indexedProperty.getName());
		}

		final IndexerWorkerParameters workerParameters = new IndexerWorkerParameters();
		workerParameters.setWorkerNumber(workerNumber);
		workerParameters.setIndexOperationId(indexerContext.getIndexOperationId());
		workerParameters.setIndexOperation(indexerContext.getIndexOperation());
		workerParameters.setExternalIndexOperation(indexerContext.isExternalIndexOperation());
		workerParameters.setFacetSearchConfig(indexerContext.getFacetSearchConfig().getName());
		workerParameters.setIndexedType(indexerContext.getIndexedType().getUniqueIndexedTypeCode());
		workerParameters.setIndexedProperties(indexedProperties);
		workerParameters.setPks(workerPks);
		workerParameters.setIndexerHints(indexerContext.getIndexerHints());

		// pass only the qualifier to avoid an additional query on the database
		workerParameters.setIndex(indexerContext.getIndex().getQualifier());

		// session related parameters
		final String tenantId = resolveTenantId();
		final UserModel sessionUser = resolveSessionUser();
		final LanguageModel sessionLanguage = resolveSessionLanguage();
		final CurrencyModel sessionCurrency = resolveSessionCurrency();
		final Collection<ProductAvailabilityGroupModel> availabilityGroups = resolveSessionProductAvailabilityGroups();
		workerParameters.setTenant(tenantId);
		workerParameters.setSessionUser(sessionUser.getUid());
		workerParameters.setSessionLanguage(sessionLanguage == null ? null : sessionLanguage.getIsocode());
		workerParameters.setSessionCurrency(sessionCurrency == null ? null : sessionCurrency.getIsocode());
		workerParameters.setSessionAvailabilityGroups(availabilityGroups);

		final IndexerWorker indexerWorker = getIndexerWorkerFactory().createIndexerWorker(getFacetSearchConfig());
		indexerWorker.initialize(workerParameters);
		return indexerWorker;
	}

	protected Collection<ProductAvailabilityGroupModel> resolveSessionProductAvailabilityGroups()
	{
		final Session session = getSessionService().getCurrentSession();

		if (!(session.getAttribute(AVAILABILITY_GROUPS) instanceof Collection))
		{

			final Collection<ProductAvailabilityGroupModel> availabilityGroups = Lists.newArrayList();

			LOG.info(session.getAttribute(AVAILABILITY_GROUPS));

			if ((session.getAttribute(AVAILABILITY_GROUPS) instanceof ProductAvailabilityGroupModel))
			{
				availabilityGroups.add(session.getAttribute(AVAILABILITY_GROUPS));
			}


			return availabilityGroups;
		}
		else
		{
			return session.getAttribute(AVAILABILITY_GROUPS);
		}
	}
}