package de.hybris.platform.commerceservices.dataimport;

import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.data.ImportData;
import de.hybris.platform.core.initialization.SystemSetupContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * A helper implementation of a data import service that overrides importAllData().
 */
public class MulticountryDataImportServiceHelper extends AbstractDataImportService
{
	/**
	 * <p>
	 * If the extra product catalogs are specified, the number and order must exactly match the order of the content
	 * catalogs and stores MINUS the global product catalog name. For example:
	 * </p>
	 * <table>
	 * <tr>
	 * <td>productCatalogName</td>
	 * <td>
	 * 
	 * <pre>
	 * global
	 * </pre>
	 * 
	 * </td>
	 * </tr>
	 * <tr>
	 * <td>contentCatalogNames</td>
	 * <td>
	 * 
	 * <pre>
	 * global,local1,local2
	 * </pre>
	 * 
	 * </td>
	 * </tr>
	 * <tr>
	 * <td>extraProductCatalogNames</td>
	 * <td>
	 * 
	 * <pre>
	 * local1,local2
	 * </pre>
	 * 
	 * </td>
	 * </tr>
	 * </table>
	 * <p>
	 * Copied from super class and modified to allow the import of the extra product catalogs. This implementation needs
	 * to be shared by three services; instead of re-implementing this huge method three times, we do it once here.
	 * </p>
	 *
	 * @param systemSetup
	 *           The system setup bean
	 * @param context
	 *           The system setup context
	 * @param importData
	 *           The import data configuration
	 * @param syncCatalogs
	 *           Should we synchronize the catalogs?
	 * @param service
	 *           The calling service
	 */
	public void importAllData(final AbstractSystemSetup systemSetup, final SystemSetupContext context,
			final ImportData importData, final boolean syncCatalogs, final AbstractDataImportService service)
	{
		createCommonData(systemSetup, context, service);
		createCatalogs(systemSetup, context, importData, service);
		if (syncCatalogs)
		{
			synchronizeCatalogs(systemSetup, context, importData, service);
		}
		createStores(systemSetup, context, importData, service);
	}

	/**
	 * Creates the product catalog and content catalogs, and prepares the sync jobs.
	 *
	 * @param systemSetup
	 *           The system setup bean
	 * @param context
	 *           The context
	 * @param service
	 *           The calling service
	 */
	protected void createCommonData(final AbstractSystemSetup systemSetup, final SystemSetupContext context,
			final AbstractDataImportService service)
	{
		systemSetup.logInfo(context, String.format("Begin importing common data for [%s]", context.getExtensionName()));
		service.importCommonData(context.getExtensionName());
	}

	/**
	 * Creates the product catalog and content catalogs, and prepares the sync jobs. If extra product catalogs have been
	 * specified they will also be created.
	 *
	 * @param systemSetup
	 *           The system setup bean
	 * @param context
	 *           The context
	 * @param importData
	 *           The import data
	 * @param service
	 *           The calling service
	 */
	protected void createCatalogs(final AbstractSystemSetup systemSetup, final SystemSetupContext context,
			final ImportData importData, final AbstractDataImportService service)
	{
		final boolean importExtraProductCatalogs = CollectionUtils.isNotEmpty(importData.getExtraProductCatalogNames());

		//import product catalog
		systemSetup.logInfo(context,
				String.format("Begin importing product catalog data for [%s]", importData.getProductCatalogName()));
		service.importProductCatalog(context.getExtensionName(), importData.getProductCatalogName());

		//import extra product catalogs
		if (importExtraProductCatalogs)
		{
			for (final String extraProductCatalogName : importData.getExtraProductCatalogNames())
			{
				systemSetup.logInfo(context,
						String.format("Begin importing extra product catalog data for [%s]", extraProductCatalogName));
				service.importProductCatalog(context.getExtensionName(), extraProductCatalogName);
			}
		}

		//import content catalogs
		for (final String contentCatalogName : importData.getContentCatalogNames())
		{
			systemSetup.logInfo(context, String.format("Begin importing content catalog data for [%s]", contentCatalogName));
			service.importContentCatalog(context.getExtensionName(), contentCatalogName);
		}

		//sync product catalog w/false to create the sync job
		service.synchronizeProductCatalog(systemSetup, context, importData.getProductCatalogName(), false);

		//sync extra product catalogs w/false to create the sync job
		if (importExtraProductCatalogs)
		{
			for (final String extraProductCatalogName : importData.getExtraProductCatalogNames())
			{
				service.synchronizeProductCatalog(systemSetup, context, extraProductCatalogName, false);
			}
		}

		//sync content catalogs w/false to create the sync job
		for (final String contentCatalog : importData.getContentCatalogNames())
		{
			service.synchronizeContentCatalog(systemSetup, context, contentCatalog, false);
		}

		//assign content catalogs as dependents of product catalog
		service.assignDependent(importData.getProductCatalogName(), importData.getContentCatalogNames());
		if (importExtraProductCatalogs)
		{
			assignExtraDependentCatalogs(importData, service);
		}
	}

	/**
	 * Synchronize the catalogs: product catalog, content catalog, extra product catalogs (if provided).
	 *
	 * @param systemSetup
	 *           The system setup bean
	 * @param context
	 *           The context
	 * @param importData
	 *           The import data
	 * @param service
	 *           The calling service
	 */
	protected void synchronizeCatalogs(final AbstractSystemSetup systemSetup, final SystemSetupContext context,
			final ImportData importData, final AbstractDataImportService service)
	{
		systemSetup.logInfo(context, String.format("Synchronizing product catalog for [%s]", importData.getProductCatalogName()));
		final boolean productSyncSuccess = service.synchronizeProductCatalog(systemSetup, context,
				importData.getProductCatalogName(), true);

		//run the extra product catalog syncs
		boolean extraProductSyncSuccess = true;
		if (CollectionUtils.isNotEmpty(importData.getExtraProductCatalogNames()))
		{
			for (final String catalogName : importData.getExtraProductCatalogNames())
			{
				systemSetup.logInfo(context, String.format("Synchronizing product catalog for [%s]", catalogName));
				if (!service.synchronizeProductCatalog(systemSetup, context, catalogName, true))
				{
					extraProductSyncSuccess = false;
				}
			}
		}
		//end customization

		for (final String contentCatalogName : importData.getContentCatalogNames())
		{
			systemSetup.logInfo(context, String.format("Synchronizing content catalog for [%s]", contentCatalogName));
			service.synchronizeContentCatalog(systemSetup, context, contentCatalogName, true);
		}

		if (!productSyncSuccess)
		{
			// Rerun the product sync if required
			systemSetup.logInfo(context,
					String.format("Rerunning product catalog synchronization for [%s]", importData.getProductCatalogName()));
			if (!service.synchronizeProductCatalog(systemSetup, context, importData.getProductCatalogName(), true))
			{
				systemSetup.logInfo(context, String.format(
						"Rerunning product catalog synchronization for [%s], failed. Please consult logs for more details.",
						importData.getProductCatalogName()));
			}
		}

		//re-run the extra product catalog syncs
		if (!extraProductSyncSuccess)
		{
			// Rerun the product sync if required
			for (final String catalogName : importData.getExtraProductCatalogNames())
			{
				systemSetup.logInfo(context, String.format("Rerunning product catalog synchronization for [%s]", catalogName));
				if (!service.synchronizeProductCatalog(systemSetup, context, catalogName, true))
				{
					systemSetup.logInfo(context, String.format(
							"Rerunning product catalog synchronization for [%s], failed. Please consult logs for more details.",
							catalogName));
				}
			}
		}
		//end customization
	}

	/**
	 * Creates the stores data defined in the ImportData.
	 *
	 * @param systemSetup
	 *           The system setup bean
	 * @param context
	 *           The context
	 * @param importData
	 *           The import data
	 * @param service
	 *           The calling service
	 */
	protected void createStores(final AbstractSystemSetup systemSetup, final SystemSetupContext context,
			final ImportData importData, final AbstractDataImportService service)
	{
		for (final String storeName : importData.getStoreNames())
		{
			if (StringUtils.isEmpty(storeName))
			{
				continue;
			}

			systemSetup.logInfo(context, String.format("Begin importing store data for [%s]", storeName));
			service.importStore(context.getExtensionName(), storeName, importData.getProductCatalogName());

			systemSetup.logInfo(context, String.format("Begin importing job data for [%s]", storeName));
			service.importJobs(context.getExtensionName(), storeName);

			systemSetup.logInfo(context, String.format("Begin importing solr index data for [%s]", storeName));
			service.importSolrIndex(context.getExtensionName(), storeName);

			if (systemSetup.getBooleanSystemSetupParameter(context, ACTIVATE_SOLR_CRON_JOBS))
			{
				systemSetup.logInfo(context, String.format("Activating solr index for [%s]", storeName));
				service.runSolrIndex(context.getExtensionName(), storeName);
			}
		}
	}

	/**
	 * <p>
	 * Assigns dependent catalogs for the extra product catalogs. Includes content catalogs for each extra product
	 * catalog, plus dependency to the global product catalog for all extra product catalogs.
	 * </p>
	 *
	 * <p>
	 * Given the following settings:
	 * </p>
	 * <ul>
	 * <li><code>productCatalog=global</code>
	 * <li><code>contentCatalogs=global,market1,market2</code>
	 * <li><code>stores=global,market1,market2</code>
	 * <li><code>productCatalog.extras=market1,market2</code>
	 * </ul>
	 * <p>
	 * The following dependencies are created:
	 * </p>
	 * <ul>
	 * <li>market1ProductCatalog -> market1ContentCatalog</li>
	 * <li>market1ProductCatalog -> centralProductCatalog</li>
	 * <li>market2ProductCatalog -> market2ContentCatalog</li>
	 * <li>market2ProductCatalog -> centralProductCatalog</li>
	 * </ul>
	 *
	 * @param importData
	 *           The import data
	 * @param service
	 *           The import service
	 */
	protected void assignExtraDependentCatalogs(final ImportData importData, final AbstractDataImportService service)
	{
		final Set<String> productCatalogDep = Collections.singleton(importData.getProductCatalogName() + "ProductCatalog");
		for (final String catalog : importData.getExtraProductCatalogNames())
		{
			//assign the content catalog dependency for each extra product catalog
			service.assignDependent(catalog, Collections.singletonList(catalog));
			//assign the central product catalog as a dependency of each extra product catalog
			getSetupSyncJobService().assignDependentSyncJobs(catalog + "ProductCatalog", productCatalogDep);
		}
	}

	@Override
	public void execute(final AbstractSystemSetup systemSetup, final SystemSetupContext context, final List<ImportData> importData)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void importCommonData(final String extensionName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void importProductCatalog(final String extensionName, final String productCatalogName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void importContentCatalog(final String extensionName, final String contentCatalogName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void importStore(final String extensionName, final String storeName, final String productCatalogName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void importSolrIndex(final String extensionName, final String storeName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void importJobs(final String extensionName, final String storeName)
	{
		throw new UnsupportedOperationException();
	}
}
