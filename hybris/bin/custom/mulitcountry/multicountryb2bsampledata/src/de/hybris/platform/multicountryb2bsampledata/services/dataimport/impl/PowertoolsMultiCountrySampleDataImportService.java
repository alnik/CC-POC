/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.multicountryb2bsampledata.services.dataimport.impl;

import de.hybris.platform.commerceservices.dataimport.impl.SampleDataImportService;
import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.data.ImportData;
import de.hybris.platform.core.initialization.SystemSetupContext;

import java.util.List;


/**
 * Implementation to handle specific Sample Data Import services to Powertools.
 */
public class PowertoolsMultiCountrySampleDataImportService extends SampleDataImportService
{
	@Override
	public void execute(final AbstractSystemSetup systemSetup, final SystemSetupContext context, final List<ImportData> importData)
	{
		super.execute(systemSetup, context, importData);
		importPowertoolsMultiCountrySolrIndex(context.getExtensionName());
	}

	/**
	 * Imports the data related to Commerce Org.
	 *
	 * @param context
	 *           the context used.
	 */
	public void importCommerceOrgData(final SystemSetupContext context)
	{
		final String extensionName = context.getExtensionName();

		getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/commerceorg/user-groups.impex", extensionName),
				false);
	}

	@Override
	protected void importProductCatalog(final String extensionName, final String productCatalogName)
	{
		super.importProductCatalog(extensionName, productCatalogName);

		// Load Multi-Dimension Categories
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/multi-d/dimension-categories.impex",
						extensionName, productCatalogName),
				false);
		// Load Multi-Dimension Products
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/multi-d/dimension-products.impex",
						extensionName, productCatalogName),
				false);
		// Load Multi-Dimension Products-Media
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/multi-d/dimension-products-media.impex",
						extensionName, productCatalogName),
				false);
		// Load Multi-Dimension Products-Prices
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/multi-d/dimension-products-prices.impex",
						extensionName, productCatalogName),
				false);
		// Load Multi-Dimension Products-Stocklevels
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/multi-d/dimension-products-stock-levels.impex",
						extensionName, productCatalogName),
				false);
		// Load future stock for multi -D products
		getSetupImpexService()
				.importImpexFile(String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/products-futurestock.impex",
						extensionName, productCatalogName), false);

		// Load product availability for brands
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/product-availability-powertools-uk.impex",
						extensionName, productCatalogName),
				false);
		getSetupImpexService().importImpexFile(
				String.format("/%s/import/sampledata/productCatalogs/%sProductCatalog/product-availability-powertools-de.impex",
						extensionName, productCatalogName),
				false);
	}

	protected void importPowertoolsMultiCountrySolrIndex(final String extensionName)
	{
		getSetupImpexService().importImpexFile(String.format("/%s/import/sampledata/stores/solr.impex", extensionName), false);

		getSetupSolrIndexerService().createSolrIndexerCronJobs("PowertoolsMultiCountryIndex");
	}

	@Override
	protected void importSolrIndex(final String extensionName, final String storeName)
	{
		//NOPE
	}
}
