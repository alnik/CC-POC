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
package de.hybris.platform.multicountryb2bsampledata.setup;

import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.data.ImportData;
import de.hybris.platform.commerceservices.setup.events.CoreDataImportedEvent;
import de.hybris.platform.commerceservices.setup.events.SampleDataImportedEvent;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;
import de.hybris.platform.core.model.enumeration.EnumerationValueModel;
import de.hybris.platform.core.model.type.SearchRestrictionModel;
import de.hybris.platform.multicountryb2bsampledata.constants.Multicountryb2bsampledataConstants;
import de.hybris.platform.multicountryb2bsampledata.services.dataimport.impl.PowertoolsMultiCountryCoreDataImportService;
import de.hybris.platform.multicountryb2bsampledata.services.dataimport.impl.PowertoolsMultiCountrySampleDataImportService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;


@SystemSetup(extension = Multicountryb2bsampledataConstants.EXTENSIONNAME)
public class SampleDataSystemSetup extends AbstractSystemSetup
{
	public static final String POWERTOOLSMULTICOUNTRY = "powertoolsMultiCountry";
	public static final String POWERTOOLS_MULTICOUNTRY_UK = "powertools-multicountry-uk";
	public static final String POWERTOOLS_MULTICOUNTRY_DE = "powertools-multicountry-de";

	private static final String PAA_APPROVED_RESTRICTION = "Frontend_ProductAvailabilityAssignmentApproved";
	private static final String PRODUCT_RESTRICTION = "Frontend_ProductBaseStoreVariant";
	private static final String VARIANT_TYPES_PLACEHOLDER = "__VARIANT_TYPES_PK__";
	private static final String ARTICLE_APPROVAL_STATUS_PLACEHOLDER = "__ARTICLE_APPROVAL_STATUS__";
	private static final String STRIP_SQL_COMMENTS = "\\/\\*.*\\*\\/\\n*";

	private static final String IMPORT_CORE_DATA = "importCoreData";
	private static final String IMPORT_SAMPLE_DATA = "importSampleData";
	private static final String ACTIVATE_SOLR_CRON_JOBS = "activateSolrCronJobs";

	private PowertoolsMultiCountryCoreDataImportService coreDataImportService;
	private PowertoolsMultiCountrySampleDataImportService sampleDataImportService;
	private ModelService modelService;
	private FlexibleSearchService flexibleSearchService;

	@SystemSetupParameterMethod
	@Override
	public List<SystemSetupParameter> getInitializationOptions()
	{
		final List<SystemSetupParameter> params = new ArrayList<SystemSetupParameter>();

		params.add(createBooleanSystemSetupParameter(IMPORT_CORE_DATA, "Import Core Data", true));
		params.add(createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA, "Import Sample Data", true));
		params.add(createBooleanSystemSetupParameter(ACTIVATE_SOLR_CRON_JOBS, "Activate Solr Cron Jobs", true));

		return params;
	}

	/**
	 * This method will be called during the system initialization.
	 *
	 * @param context
	 *           the context provides the selected parameters and values
	 */
	@SystemSetup(type = SystemSetup.Type.PROJECT, process = SystemSetup.Process.ALL)
	public void createProjectData(final SystemSetupContext context)
	{
		final List<ImportData> importData = new ArrayList<ImportData>();

		final ImportData powertoolsImportData = new ImportData();
		powertoolsImportData.setProductCatalogName(POWERTOOLSMULTICOUNTRY);
		powertoolsImportData.setContentCatalogNames(Arrays.asList(POWERTOOLSMULTICOUNTRY));
		powertoolsImportData.setStoreNames(Arrays.asList(POWERTOOLS_MULTICOUNTRY_UK, POWERTOOLS_MULTICOUNTRY_DE));
		importData.add(powertoolsImportData);
		getPowertoolsMultiCountryCoreDataImportService().execute(this, context, importData);

		importImpexFile(context, "/multicountryb2bsampledata/import/coredata/productSearchRestrictions/multicountry.impex", true);
		patchSearchRestrictions();
		getEventService().publishEvent(new CoreDataImportedEvent(context, importData));

		getPowertoolsMultiCountrySampleDataImportService().execute(this, context, importData);
		getPowertoolsMultiCountrySampleDataImportService().importCommerceOrgData(context);
		getEventService().publishEvent(new SampleDataImportedEvent(context, importData));
	}

	private void patchSearchRestrictions()
	{

		final FlexibleSearchQuery restrictionLookup = new FlexibleSearchQuery(
				"SELECT {PK} FROM {SearchRestriction} WHERE {code} = ?code");
		restrictionLookup.addQueryParameter("code", PAA_APPROVED_RESTRICTION);

		final List<SearchRestrictionModel> paaRestrictions = getFlexibleSearchService()
				.<SearchRestrictionModel> search(restrictionLookup).getResult();

		// patch PPA restriction

		// get articleApprovalStatus = approved pk to substitute
		final String approvalPk = getFlexibleSearchService().<EnumerationValueModel> searchUnique(
				new FlexibleSearchQuery("select {pk} from {ArticleApprovalStatus} where code = 'approved'")).getPk().toString();

		for (final SearchRestrictionModel paaRestriction : paaRestrictions)
		{

			String queryPAARestriction = paaRestriction.getQuery();
			queryPAARestriction = queryPAARestriction.replaceAll(ARTICLE_APPROVAL_STATUS_PLACEHOLDER, approvalPk);
			queryPAARestriction = queryPAARestriction.replaceAll(STRIP_SQL_COMMENTS, "");

			paaRestriction.setQuery(queryPAARestriction);
			getModelService().save(paaRestriction);

		}

		// patch PPA restriction

		restrictionLookup.addQueryParameter("code", PRODUCT_RESTRICTION);

		final List<SearchRestrictionModel> productRestrictions = getFlexibleSearchService()
				.<SearchRestrictionModel> search(restrictionLookup).getResult();

		// get variantTypes pks

		for (final SearchRestrictionModel productRestriction : productRestrictions)
		{

			String queryProductRestriction = productRestriction.getQuery();
			queryProductRestriction = queryProductRestriction.replaceAll(VARIANT_TYPES_PLACEHOLDER, getVariantTpesPK());
			queryProductRestriction = queryProductRestriction.replaceAll(STRIP_SQL_COMMENTS, "");

			productRestriction.setQuery(queryProductRestriction);
			getModelService().save(productRestriction);

		}

	}

	private String getVariantTpesPK()
	{

		final FlexibleSearchQuery query = new FlexibleSearchQuery("SELECT {pk} FROM {VariantType}");
		query.setResultClassList(Collections.singletonList(PK.class));
		final List<PK> result = flexibleSearchService.<PK> search(query).getResult();

		if (result == null || result.isEmpty())
		{
			return "0";
		}

		return StringUtils.collectionToCommaDelimitedString(result);

	}

	public PowertoolsMultiCountryCoreDataImportService getPowertoolsMultiCountryCoreDataImportService()
	{
		return coreDataImportService;
	}

	@Required
	public void setPowertoolsMultiCountryCoreDataImportService(
			final PowertoolsMultiCountryCoreDataImportService powertoolsCoreDataImportService)
	{
		this.coreDataImportService = powertoolsCoreDataImportService;
	}

	public PowertoolsMultiCountrySampleDataImportService getPowertoolsMultiCountrySampleDataImportService()
	{
		return sampleDataImportService;
	}

	@Required
	public void setPowertoolsMultiCountrySampleDataImportService(
			final PowertoolsMultiCountrySampleDataImportService powertoolsSampleDataImportService)
	{
		this.sampleDataImportService = powertoolsSampleDataImportService;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}


}
