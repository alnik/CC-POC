package de.hybris.contextualattributevaluesbackoffice.solrfacetsearch;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.List;
import java.util.Locale;

import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualSolrSearchCondition extends SolrSearchCondition
{
	private List<ContextualAttributesContextModel> context;

	public ContextualSolrSearchCondition( String name, String type, boolean multiValue, Locale locale, SearchQuery.Operator operator, boolean allMatch,
			List<ContextualAttributesContextModel> context )
	{
		super( name, type, multiValue, locale, operator, allMatch );
		this.context = context;
	}

	public List<ContextualAttributesContextModel> getContext()
	{
		return context;
	}
}
