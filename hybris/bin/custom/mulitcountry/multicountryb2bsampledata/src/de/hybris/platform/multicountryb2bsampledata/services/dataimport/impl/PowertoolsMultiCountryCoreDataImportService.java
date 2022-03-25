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

import de.hybris.platform.commerceservices.dataimport.impl.CoreDataImportService;
import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.data.ImportData;
import de.hybris.platform.core.initialization.SystemSetupContext;

import java.util.List;



/**
 * Implementation to handle specific Sample Data Import services to PowertoolsMultiCountry.
 */
public class PowertoolsMultiCountryCoreDataImportService extends CoreDataImportService
{

	@Override
	public void execute(final AbstractSystemSetup systemSetup, final SystemSetupContext context, final List<ImportData> importData)
	{
		super.execute(systemSetup, context, importData);
		importPowertoolsMultiCountrySolrIndex(context.getExtensionName());
	}

	protected void importPowertoolsMultiCountrySolrIndex(final String extensionName)
	{
		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/stores/solr.impex", extensionName), false);

		getSetupSolrIndexerService().createSolrIndexerCronJobs("powertoolsMulticountryIndex");

		getSetupImpexService().importImpexFile(String.format("/%s/import/coredata/stores/solrtrigger.impex", extensionName), false);
	}

	@Override
	protected void importSolrIndex(final String extensionName, final String storeName)
	{
		//NOPE
	}
}
