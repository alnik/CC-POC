/**
 *
 */
package de.hybris.platform.multicountryb2csampledata.setup;


import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.data.ImportData;
import de.hybris.platform.commerceservices.setup.events.CoreDataImportedEvent;
import de.hybris.platform.commerceservices.util.ResponsiveUtils;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetup.Process;
import de.hybris.platform.core.initialization.SystemSetup.Type;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;
import de.hybris.platform.core.model.enumeration.EnumerationValueModel;
import de.hybris.platform.core.model.type.SearchRestrictionModel;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.util.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;


/**
 * @author santosh.ritti
 *
 */
@SystemSetup(extension = "multicountryb2csampledata")
public class SampleDataSystemSetup extends AbstractSystemSetup
{
	public static final String APPAREL_MULTI_COUNTRY = "apparelMultiCountry";
	public static final String IMPORT_SITES = "importSites";
	public static final String IMPORT_SYNC_CATALOGS = "syncProducts&ContentCatalogs";
	public static final String IMPORT_MULTICOUNTRY_DATA = "importMultiCountrySampleData";
	public static final String IMPORT_MULTICOUNTRY_SEARCH_RESTRICTION = "importMultiCountrySearchRestrictions";
	public static final String ACTIVATE_SOLR_CRON_JOBS = "activateSolrCronJobs";

	private static final String PAA_APPROVED_RESTRICTION = "Frontend_ProductAvailabilityAssignmentApproved";
	private static final String PRODUCT_RESTRICTION = "Frontend_ProductBaseStoreVariant";
	private static final String VARIANT_TYPES_PLACEHOLDER = "__VARIANT_TYPES_PK__";
	private static final String ARTICLE_APPROVAL_STATUS_PLACEHOLDER = "__ARTICLE_APPROVAL_STATUS__";

	private static final String STRIP_SQL_COMMENTS = "\\/\\*.*\\*\\/\\n*";

	static final Logger LOG = Logger.getLogger(SampleDataSystemSetup.class);

	@Resource
	private TypeService typeService;
	@Resource
	private ModelService modelService;
	@Resource
	private FlexibleSearchService flexibleSearchService;


	@Override
	@SystemSetupParameterMethod
	public List<SystemSetupParameter> getInitializationOptions()
	{
		final List<SystemSetupParameter> params = new ArrayList<SystemSetupParameter>();
		params.add(createBooleanSystemSetupParameter(IMPORT_MULTICOUNTRY_DATA, "Import Multi Country Sample Data", true)); // We need some data for an empty system, to start it somehow
		params.add(createBooleanSystemSetupParameter(IMPORT_MULTICOUNTRY_SEARCH_RESTRICTION,
				"Import Multi Country Search Restrictions", true));

		return params;
	}

	@SystemSetup(type = Type.ESSENTIAL, process = Process.ALL)
	public void createEssentialData(final SystemSetupContext context)
	{
		importImpexFile(context, "/impex/essentialdata_multicountry.impex", false);
	}

	/**
	 * Implement this method to create data that is used in your project. This method will be called during the system
	 * initialization.
	 *
	 * @param context
	 *           the context provides the selected parameters and values
	 */
	@SystemSetup(type = Type.PROJECT, process = Process.ALL)
	public void createProjectData(final SystemSetupContext context)
	{
		final ImportData multiCountryImportData = new ImportData();
		multiCountryImportData.setProductCatalogName(APPAREL_MULTI_COUNTRY);
		multiCountryImportData.setContentCatalogNames(Arrays.asList(APPAREL_MULTI_COUNTRY));
		multiCountryImportData.setStoreNames(Arrays.asList("multicountry-it", "multicountry-de", "multicountry-fr",
				"multicountry-es", "multicountry-uk", "multicountry-be"));

		if (getBooleanSystemSetupParameter(context, IMPORT_MULTICOUNTRY_DATA))
		{
			logInfo(context, "Importing Multi country B2C Sample data...");

			importLanguages(context);

			// Create Catalogs

			// ApparelMultiCountryProductCatalog (also creates apparelMultiCountryCategoryCatalogIT as a sample of country specific category catalog)
			createProductCatalog(context, APPAREL_MULTI_COUNTRY);

			// ApparelMultiCountryContentCatalog
			createContentCatalog(context, APPAREL_MULTI_COUNTRY);

			// Create solr index configuration
			importSolrConfigration(context);

			// Create Stores
			createStore(context, "multicountry-be");
			createStore(context, "multicountry-de");
			createStore(context, "multicountry-es");
			createStore(context, "multicountry-fr");
			createStore(context, "multicountry-it");
			createStore(context, "multicountry-uk");

			// Import Catalogs Data
			importProjectData(context, APPAREL_MULTI_COUNTRY);
			importProductCatalog(context, "impex", APPAREL_MULTI_COUNTRY);
			importContentCatalog(context, "impex", APPAREL_MULTI_COUNTRY);

			// create product sync job
			synchronizeProductCatalog(context, APPAREL_MULTI_COUNTRY, true);

			// create country specific category sync job(s)
			synchronizeCategoryCatalog(context, "apparelMultiCountryCategoryCatalogIT", true);

			// create content sync job
			synchronizeContentCatalog(context, APPAREL_MULTI_COUNTRY, true);

			// Make content sync dependent of product sync
			final Set<String> dependentSyncJobsNames = new HashSet<String>();
			dependentSyncJobsNames.add("apparelMultiCountryContentCatalog");
			getSetupSyncJobService().assignDependentSyncJobs("apparelMultiCountryProductCatalog", dependentSyncJobsNames);

			// Make product sync dependent of category sync
			dependentSyncJobsNames.clear();
			dependentSyncJobsNames.add("apparelMultiCountryProductCatalog");
			getSetupSyncJobService().assignDependentSyncJobs("apparelMultiCountryCategoryCatalogIT", dependentSyncJobsNames);

			importImpexFile(context, "/impex/timezones_min.impex");
			importImpexFile(context, "/impex/multicountry_basestore_timezones.impex");
			importImpexFile(context, "/impex/multicountry_cms_restrictions.impex");

		}

		if (getBooleanSystemSetupParameter(context, IMPORT_MULTICOUNTRY_SEARCH_RESTRICTION))
		{

			importImpexFile(context, "/impex/productSearchRestriction/multicountry.impex", false);
			patchSearchRestrictions();

		}

		// Send an event to notify any AddOns that the core data import is
		// complete
		getEventService().publishEvent(new CoreDataImportedEvent(context, Arrays.asList(multiCountryImportData)));
	}

	protected void importLanguages(final SystemSetupContext context)
	{
		logInfo(context, "Begin importing sample languages.");
		importImpexFile(context, "/impex/projectdata_languages.impex", false);
	}

	protected void importSolrConfigration(final SystemSetupContext context)
	{
		logInfo(context, "Begin importing solr index configuration");
		importImpexFile(context, "/impex/projectdata_solr.impex", false);
	}

	protected void importSolrConfigrationB2B(final SystemSetupContext context)
	{
		logInfo(context, "Begin importing solr index configuration");
		importImpexFile(context, "/impex/projectdata_solr_b2b.impex", false);
	}

	protected void importProjectData(final SystemSetupContext context, final String productCatalogName)
	{
		logInfo(context, "Begin importing project data [" + productCatalogName + "]");

		importImpexFile(context, "/impex/projectdata_multicountry.impex", false);

		patchSearchRestrictions();
	}

	protected void createProductCatalog(final SystemSetupContext context, final String productCatalogName)
	{
		logInfo(context, "Begin importing catalog [" + productCatalogName + "]");

		importImpexFile(context,
				"/multicountryb2csampledata/import/productCatalogs/" + productCatalogName + "ProductCatalog/catalog.impex", true);

		createProductCatalogSyncJob(context, productCatalogName + "ProductCatalog");
	}

	protected void createContentCatalog(final SystemSetupContext context, final String contentCatalogName)
	{
		logInfo(context, "Begin importing catalog [" + contentCatalogName + "]");

		importImpexFile(context,
				"/multicountryb2csampledata/import/contentCatalog/" + contentCatalogName + "ContentCatalog/catalog.impex", true);

		// Create core content data
		importImpexFile(context,
				"/multicountryb2csampledata/import/contentCatalog/" + contentCatalogName + "ContentCatalog/cms-content.impex", true);

		importImpexFile(context,
				"/multicountryb2csampledata/import/contentCatalog/" + contentCatalogName + "ContentCatalog/cms-mobile-content.impex",
				true);

		importImpexFile(context,
				"/multicountryb2csampledata/import/contentCatalog/" + contentCatalogName + "ContentCatalog/email-content.impex",
				true);

		if (ResponsiveUtils.isResponsive())
		{
			importImpexFile(context, "/multicountryb2csampledata/import/contentCatalog/" + contentCatalogName
					+ "ContentCatalog/cms-responsive-content.impex", true);
		}

		createContentCatalogSyncJob(context, contentCatalogName + "ContentCatalog");
	}

	protected void importProductCatalog(final SystemSetupContext context, final String importDirectory, final String catalogName)
	{
		logInfo(context, "Begin importing Product Catalog [" + catalogName + "]");

		// Load Categories
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/categories.impex", false);

		// Load Suppliers
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/suppliers.impex", false);

		// Load medias for Categories as Suppliers loads some new Categories
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/categories-media.impex", false);

		// Load Products
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/products.impex");
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/products-media.impex", false);

		// Load Products Relations
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/products-relations.impex", false);

		// Load Products Fixes
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/products-fixup.impex", false);

		// Load Prices
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/products-prices.impex", false);

		// Load Price rows for each country
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/pricerows/product-prices_gb.impex",
				false);

		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/pricerows/product-prices_be.impex",
				false);

		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/pricerows/product-prices_de.impex",
				false);

		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/pricerows/product-prices_es.impex",
				false);

		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/pricerows/product-prices_fr.impex",
				false);

		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/pricerows/product-prices_it.impex",
				false);

		// Load Stock Levels
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/products-stocklevels.impex", false);
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/products-pos-stocklevels.impex",
				false);

		final List<String> extensionNames = Utilities.getExtensionNames();


		if (extensionNames != null && extensionNames.contains("ordermanagementaddon"))
		{
			importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/sourcing.impex", false);
		}
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/products-tax.impex", false);

		// Load product availability for countries
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/product-availability-uk.impex", false);
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/product-availability-be.impex", false);
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/product-availability-de.impex", false);
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/product-availability-es.impex", false);
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/product-availability-fr.impex", false);
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/product-availability-it.impex", false);

		//Adaptive search
		importImpexFile(context, "/" + importDirectory + "/" + catalogName + "ProductCatalog/adaptive-search.impex", false);
	}

	protected boolean synchronizeProductCatalog(final SystemSetupContext context, final String catalogName, final boolean sync)
	{
		logInfo(context,
				"Begin synchronizing Product Catalog [" + catalogName + "] - " + (sync ? "synchronizing" : "initializing job"));

		createProductCatalogSyncJob(context, catalogName + "ProductCatalog");

		//fixSyncJob(catalogName);

		boolean result = true;

		if (sync)
		{
			final PerformResult syncCronJobResult = executeCatalogSyncJob(context, catalogName + "ProductCatalog");
			if (isSyncRerunNeeded(syncCronJobResult))
			{
				logInfo(context, "Product catalog [" + catalogName + "] sync has issues.");
				result = false;
			}
		}

		logInfo(context, "Done " + (sync ? "synchronizing" : "initializing job") + " Product Catalog [" + catalogName + "]");
		return result;
	}

	/**
	 * Creates sync job for the country specific category catalogs A category catalog is just a special case of the
	 * product catalog containing only categories for a specific country. Expects the full name of the catalog (not just
	 * the prefix as the synchronizeProductCatalog method)
	 */
	protected boolean synchronizeCategoryCatalog(final SystemSetupContext context, final String catalogName, final boolean sync)
	{
		logInfo(context,
				"Begin synchronizing Category Catalog [" + catalogName + "] - " + (sync ? "synchronizing" : "initializing job"));

		createProductCatalogSyncJob(context, catalogName);

		boolean result = true;

		if (sync)
		{
			final PerformResult syncCronJobResult = executeCatalogSyncJob(context, catalogName);
			if (isSyncRerunNeeded(syncCronJobResult))
			{
				logInfo(context, "Category catalog [" + catalogName + "] sync has issues.");
				result = false;
			}
		}

		logInfo(context, "Done " + (sync ? "synchronizing" : "initializing job") + " Category Catalog [" + catalogName + "]");
		return result;
	}

	/**
	 * @param catalogName
	 *
	 *           REMOVED: configuration is maintained in spring.
	 */
	//	private void fixSyncJob(final String catalogName)
	//	{
	//		// add our new type as a root type
	//		final SyncItemJobModel syncJob = getCatalogSyncJob(catalogName + "ProductCatalog").iterator().next();
	//		final List<ComposedTypeModel> rootTypes = new LinkedList(syncJob.getRootTypes());
	//		rootTypes.add(typeService.getComposedTypeForClass(ProductAvailabilityAssignmentModel.class));
	//		rootTypes.add(typeService.getComposedTypeForClass(ContextualAttributeValueModel.class));
	//		syncJob.setRootTypes(rootTypes);
	//		modelService.save(syncJob);
	//
	//		final ComposedType productType = TypeManager.getInstance().getComposedType(Product.class);
	//		final AttributeDescriptor availabiltyDescriptor = productType
	//				.getAttributeDescriptorIncludingPrivate(ProductModel.AVAILABILITY);
	//		final AttributeDescriptor contextualAttributeValueDescriptor = productType
	//				.getAttributeDescriptorIncludingPrivate(ProductModel.CONTEXTUALATTRIBUTEVALUES);
	//
	//		final SyncItemJob syncItemJob = modelService.getSource(syncJob);
	//		final SyncAttributeDescriptorConfig attributeDescriptorConfig = syncItemJob.getConfigFor(availabiltyDescriptor, true);
	//		attributeDescriptorConfig.setCopyByValue(true);
	//		final SyncAttributeDescriptorConfig contextAttributeDescriptorConfig = syncItemJob.getConfigFor(
	//				contextualAttributeValueDescriptor, true);
	//		contextAttributeDescriptorConfig.setCopyByValue(true);
	//
	//	}

	protected void importContentCatalog(final SystemSetupContext context, final String importDirectory, final String catalogName)
	{
		logInfo(context, "Begin importing Content Catalog [" + catalogName + "]");

		final String importRoot = "/" + importDirectory + "/";

		importImpexFile(context, importRoot + catalogName + "ContentCatalog/cmscockpit-users.impex", false);
		importImpexFile(context, importRoot + catalogName + "ContentCatalog/cms-content.impex", false);
		importImpexFile(context, importRoot + catalogName + "ContentCatalog/cms-mobile-content.impex", false);
		importImpexFile(context, importRoot + catalogName + "ContentCatalog/email-content.impex", false);

		if (ResponsiveUtils.isResponsive())
		{
			importImpexFile(context, importRoot + catalogName + "ContentCatalog/cms-responsive-content.impex", false);
		}

		logInfo(context, "Done importing Content Catalog [" + catalogName + "]");
	}

	protected boolean synchronizeContentCatalog(final SystemSetupContext context, final String catalogName, final boolean sync)
	{
		logInfo(context,
				"Begin synchronizing Content Catalog [" + catalogName + "] - " + (sync ? "synchronizing" : "initializing job"));

		createContentCatalogSyncJob(context, catalogName + "ContentCatalog");

		boolean result = true;

		if (sync)
		{
			final PerformResult syncCronJobResult = executeCatalogSyncJob(context, catalogName + "ContentCatalog");
			if (isSyncRerunNeeded(syncCronJobResult))
			{
				logInfo(context, "Catalog catalog [" + catalogName + "] sync has issues.");
				result = false;
			}
		}

		logInfo(context, "Done " + (sync ? "synchronizing" : "initializing job") + " Content Catalog [" + catalogName + "]");
		return result;
	}

	private void patchSearchRestrictions()
	{

		final FlexibleSearchQuery restrictionLookup = new FlexibleSearchQuery(
				"SELECT {PK} FROM {SearchRestriction} WHERE {code} = ?code");
		restrictionLookup.addQueryParameter("code", PAA_APPROVED_RESTRICTION);

		final List<SearchRestrictionModel> paaRestrictions = flexibleSearchService
				.<SearchRestrictionModel> search(restrictionLookup).getResult();

		// patch PPA restriction

		// get articleApprovalStatus = approved pk to substitute
		final String approvalPk = flexibleSearchService.<EnumerationValueModel> searchUnique(
				new FlexibleSearchQuery("select {pk} from {ArticleApprovalStatus} where code = 'approved'")).getPk().toString();

		for (final SearchRestrictionModel paaRestriction : paaRestrictions)
		{

			String queryPAARestriction = paaRestriction.getQuery();
			queryPAARestriction = queryPAARestriction.replaceAll(ARTICLE_APPROVAL_STATUS_PLACEHOLDER, approvalPk);
			queryPAARestriction = queryPAARestriction.replaceAll(STRIP_SQL_COMMENTS, "");

			paaRestriction.setQuery(queryPAARestriction);
			modelService.save(paaRestriction);

		}

		// patch PPA restriction

		restrictionLookup.addQueryParameter("code", PRODUCT_RESTRICTION);

		final List<SearchRestrictionModel> productRestrictions = flexibleSearchService
				.<SearchRestrictionModel> search(restrictionLookup).getResult();

		// get variantTypes pks

		for (final SearchRestrictionModel productRestriction : productRestrictions)
		{

			String queryProductRestriction = productRestriction.getQuery();
			queryProductRestriction = queryProductRestriction.replaceAll(VARIANT_TYPES_PLACEHOLDER, getVariantTpesPK());
			queryProductRestriction = queryProductRestriction.replaceAll(STRIP_SQL_COMMENTS, "");

			productRestriction.setQuery(queryProductRestriction);
			modelService.save(productRestriction);

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

	private void createStore(final SystemSetupContext context, final String storeName)
	{
		logInfo(context, "Begin importing store [" + storeName + "]");

		importImpexFile(context, "/multicountryb2csampledata/import/sites/" + storeName + "/store.impex");
		importImpexFile(context, "/multicountryb2csampledata/import/sites/" + storeName + "/site.impex");

		logInfo(context, "Done importing store [" + storeName + "]");
	}

	@SuppressWarnings("unchecked")
	protected <T> T getBeanForName(final String name)
	{
		return (T) Registry.getApplicationContext().getBean(name);
	}

}
