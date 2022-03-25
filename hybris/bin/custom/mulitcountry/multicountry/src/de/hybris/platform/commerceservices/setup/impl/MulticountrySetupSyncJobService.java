package de.hybris.platform.commerceservices.setup.impl;

import static de.hybris.platform.catalog.jalo.CatalogManager.OFFLINE_VERSION;
import static de.hybris.platform.catalog.jalo.CatalogManager.ONLINE_VERSION;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.jalo.SyncItemJob;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.cronjob.enums.ErrorMode;
import de.hybris.platform.multicountry.model.synchronization.MultiCountryCatalogVersionSyncJobModel;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Created by i844957 on 5/17/16.
 */
public class MulticountrySetupSyncJobService extends DefaultSetupSyncJobService
{
	private static final Logger LOG = LoggerFactory.getLogger(MulticountrySetupSyncJobService.class);

	private CatalogVersionService catalogVersionService;


	@Override
	public void createProductCatalogSyncJob(final String catalogId)
	{
		// Check if the sync job already exists
		if (getCatalogSyncJob(catalogId) == null)
		{
			LOG.info("Creating product sync item job for [{}]", catalogId);
			final CatalogVersionModel stagedVersion = getCatalogVersionService().getCatalogVersion(catalogId, OFFLINE_VERSION);
			final CatalogVersionModel onlineVersion = getCatalogVersionService().getCatalogVersion(catalogId, ONLINE_VERSION);


			final String jobName = createJobIdentifier(catalogId);
			final MultiCountryCatalogVersionSyncJobModel syncJobModel = new MultiCountryCatalogVersionSyncJobModel();
			syncJobModel.setActive(true);
			syncJobModel.setCode(jobName);
			syncJobModel.setSourceVersion(stagedVersion);
			syncJobModel.setTargetVersion(onlineVersion);
			syncJobModel.setCreateNewItems(true);
			syncJobModel.setRemoveMissingItems(true);
			syncJobModel.setErrorMode(ErrorMode.IGNORE);
			syncJobModel.setLogToDatabase(false);
			syncJobModel.setLogToFile(true);
			syncJobModel.setExclusiveMode(true);
			syncJobModel.setSyncLanguages(new HashSet<>(stagedVersion.getCatalog().getLanguages()));

			getModelService().save(syncJobModel);

			//get the freshly-created sync job Jalo object
			final SyncItemJobModel syncItemJob = getSyncJobForCatalog(catalogId);
			processRootTypes(syncItemJob, catalogId, getProductCatalogRootTypeCodes());
			processEditSyncAttributeDescriptors(syncItemJob, catalogId, getProductCatalogEditSyncDescriptors());

			LOG.info("Created product sync item job [{}]", syncItemJob.getCode());
		}
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}
}
