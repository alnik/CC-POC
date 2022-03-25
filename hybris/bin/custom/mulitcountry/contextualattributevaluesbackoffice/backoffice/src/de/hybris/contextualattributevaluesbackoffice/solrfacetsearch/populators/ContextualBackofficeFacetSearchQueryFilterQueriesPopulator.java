package de.hybris.contextualattributevaluesbackoffice.solrfacetsearch.populators;

import de.hybris.contextualattributevaluesbackoffice.solrfacetsearch.ContextualSolrSearchCondition;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.backoffice.solrsearch.populators.BackofficeFacetSearchQueryFilterQueriesPopulator;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualBackofficeFacetSearchQueryFilterQueriesPopulator extends BackofficeFacetSearchQueryFilterQueriesPopulator
{
	private final static String SEPERATOR = "_";

	@Override
	protected String convertSearchConditionToFilterQuery( SearchQuery searchQuery, SolrSearchCondition condition )
	{
		if( condition instanceof ContextualSolrSearchCondition && ((ContextualSolrSearchCondition) condition).getContext() != null )
		{
			if( ((ContextualSolrSearchCondition) condition).getContext().size() == 1 )
			{
				String fieldName = this.convertAttributeNameToContextualFieldName( searchQuery, condition,
						((ContextualSolrSearchCondition) condition).getContext().get( 0 ).getCode() );
				String flatFQValue = this.convertSearchConditionValuesToFilterQueryValue( condition );
				return fieldName.concat( ":" ).concat( flatFQValue );
			}
			else
			{
				List<String> contextQueries = new ArrayList<>();
				String flatFQValue = this.convertSearchConditionValuesToFilterQueryValue( condition );
				for( ContextualAttributesContextModel contextModel: ((ContextualSolrSearchCondition) condition).getContext() )
				{
					String fieldName = this.convertAttributeNameToContextualFieldName( searchQuery, condition, contextModel.getCode() );
					contextQueries.add( fieldName.concat( ":" ).concat( flatFQValue ) );
				}
				return String.format( "(%s)", Joiner.on( " OR " ).join( contextQueries ) );
			}
		}
		else
		{
			return super.convertSearchConditionToFilterQuery(searchQuery, condition);
		}
	}

	private String convertAttributeNameToContextualFieldName( SearchQuery searchQuery, SolrSearchCondition condition, String contextCode )
	{
		String convertedFieldName = super.convertAttributeNameToFieldName( searchQuery, condition );
		return postProcess( convertedFieldName, (ContextualSolrSearchCondition) condition, contextCode );
	}

	private String postProcess( String convertedFieldName, ContextualSolrSearchCondition condition, String contextCode )
	{
		if( condition.getContext() != null )
		{
			LinkedList<String> qualifierParts = Lists.newLinkedList( Arrays.asList( convertedFieldName.split( SEPERATOR ) ) );
			qualifierParts.add( 1, contextCode.toLowerCase( Locale.ENGLISH ) );
			return Joiner.on( SEPERATOR ).join( qualifierParts );
		}
		return convertedFieldName;
	}
}
