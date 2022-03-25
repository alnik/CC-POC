/**
 *
 */
package de.hybris.platform.contextualattributevalues.setup;

import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetup.Process;
import de.hybris.platform.core.initialization.SystemSetup.Type;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;


@SystemSetup(extension = "contextualattributevalues")
public class SampleDataSystemSetup extends AbstractSystemSetup
{
	public static final String IMPORT_CONTEXTUALVALUES_DATA = "importMultiCountrySampleData";
	public static final String IMPORT_CONTEXTUALVALUES_SEARCH_RESTRICTION = "importMultiCountrySearchRestrictions";
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
		params.add(createBooleanSystemSetupParameter(IMPORT_CONTEXTUALVALUES_DATA, "Import Contextual Attribute Values Sample Data",
				false));
		params.add(createBooleanSystemSetupParameter(IMPORT_CONTEXTUALVALUES_SEARCH_RESTRICTION,
				"Import Contextual Attribute Values Search Restrictions", true));

		return params;
	}

	@SystemSetup(type = Type.ESSENTIAL, process = Process.ALL)
	public void createEssentialData(final SystemSetupContext context)
	{
		importImpexFile(context, "/impex/essentialdata_contextualattributevalues.impex", true);
	}

	@SystemSetup(type = Type.PROJECT, process = Process.ALL)
	public void createProjectData(final SystemSetupContext context)
	{
		if (getBooleanSystemSetupParameter(context, IMPORT_CONTEXTUALVALUES_DATA))
		{
			importImpexFile(context, "/impex/sampledata_contextualattributevalues.impex", true);
		}

		if (getBooleanSystemSetupParameter(context, IMPORT_CONTEXTUALVALUES_SEARCH_RESTRICTION))
		{
			importImpexFile(context, "/impex/contextualattributes_restrictions.impex", true);
		}
	}
}
