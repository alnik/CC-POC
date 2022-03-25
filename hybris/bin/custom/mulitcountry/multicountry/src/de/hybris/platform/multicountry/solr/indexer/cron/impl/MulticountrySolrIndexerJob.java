package de.hybris.platform.multicountry.solr.indexer.cron.impl;


import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.multicountry.services.MulticountryRestrictionService;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.solrfacetsearch.indexer.cron.SolrIndexerJob;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.indexer.cron.SolrIndexerCronJobModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * This class adds an active site to the session, so the search restriction for products by base store doesn't raise a
 * "null" exception. The added site should hold all (the desired) base stores to be processed and indexed into Solr by
 * this cronjob.
 *
 */
public class MulticountrySolrIndexerJob extends SolrIndexerJob
{
	private static final Logger LOG = Logger.getLogger(MulticountrySolrIndexerJob.class);
	private static final String CURRENTSITE = "currentSite";

	private String defaultSiteName = "allBaseStoresSite";
	private BaseSiteService baseSiteService;
	private BaseStoreService baseStoreService;
	private MulticountryRestrictionService multicountryRestrictionService;

	@Override
	public PerformResult performIndexingJob(final CronJobModel cronJob)
	{
		//Check if we need to add a site to the session...
		final Session session = super.sessionService.getCurrentSession();
		final BaseSiteModel defaultSiteModel = getBaseSiteService().getBaseSiteForUID(getDefaultSiteName());
		if (defaultSiteModel != null && !session.getAllAttributes().containsKey(CURRENTSITE))
		{
			//We've got a site and there's none in the session => set the activeSite in the session
			session.setAttribute(CURRENTSITE, defaultSiteModel);
		}
		SolrIndexerCronJobModel solrIndexerCronJob = null;
		if (cronJob instanceof SolrIndexerCronJobModel)
		{
			solrIndexerCronJob = (SolrIndexerCronJobModel) cronJob;
		}
		else
		{
			LOG.warn("Unexpected cronjob type: " + cronJob);
			return new PerformResult(CronJobResult.FAILURE, CronJobStatus.ABORTED);
		}

		final SolrFacetSearchConfigModel facetSearchConfigModel = solrIndexerCronJob.getFacetSearchConfig();
		// Add product availability groups to session
		addProductAvailabilityGroups(facetSearchConfigModel);
		getMulticountryRestrictionService().disableOnlineDateRestriction();

		//... and then go on with the normal indexing job.
		return super.performIndexingJob(cronJob);
	}

	private void addProductAvailabilityGroups(final SolrFacetSearchConfigModel facetSearchConfigModel)
	{
		final Set<ProductAvailabilityGroupModel> activeGroups = new HashSet<>();


		final List<BaseStoreModel> stores = getBaseStoreService().getAllBaseStores();

		stores.stream().filter(baseStore -> isValidBaseStore(baseStore, facetSearchConfigModel))
				.forEach(baseStore -> activeGroups.addAll(baseStore.getAvailabilityGroups()));

		getMulticountryRestrictionService().setCurrentProductAvailabilityGroups(activeGroups);
	}

	protected static boolean isValidBaseStore(final BaseStoreModel baseStore, final SolrFacetSearchConfigModel facetConfig)
	{
		return baseStore != null && CollectionUtils.isNotEmpty(baseStore.getAvailabilityGroups());

		//&& facetConfig.equals(baseStore.getSolrFacetSearchConfiguration())
	}

	public String getDefaultSiteName()
	{
		return defaultSiteName;
	}

	public void setDefaultSiteName(final String defaultSiteName)
	{
		this.defaultSiteName = defaultSiteName;
	}

	public BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	@Required
	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}

	public BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	@Required
	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}

	public MulticountryRestrictionService getMulticountryRestrictionService()
	{
		return multicountryRestrictionService;
	}

	@Required
	public void setMulticountryRestrictionService(final MulticountryRestrictionService multicountryRestrictionService)
	{
		this.multicountryRestrictionService = multicountryRestrictionService;
	}

}
