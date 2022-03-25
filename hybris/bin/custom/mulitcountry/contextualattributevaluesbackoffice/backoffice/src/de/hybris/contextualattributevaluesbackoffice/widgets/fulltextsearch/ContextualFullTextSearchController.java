package de.hybris.contextualattributevaluesbackoffice.widgets.fulltextsearch;

import com.hybris.cockpitng.config.fulltextsearch.jaxb.FulltextSearch;
import de.hybris.contextualattributevaluesbackoffice.widgets.fulltextsearch.renderer.impl.ContextualFullTextSearchFilter;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.hybris.backoffice.widgets.advancedsearch.AdvancedSearchMode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchInitContext;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.backoffice.widgets.fulltextsearch.FullTextSearchController;
import com.hybris.backoffice.widgets.fulltextsearch.renderer.FullTextSearchFilter;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualFullTextSearchController extends FullTextSearchController
{
	protected List<SearchConditionData> buildSearchConditionData( final Map<String, FullTextSearchFilter> filters )
	{
		final List<SearchConditionData> conditions = Lists.newArrayList();
		filters.values().stream()
				.filter( FullTextSearchFilter::isEnabled )
				.filter( filter -> isFilterRequiredOrHasAValue(filter) )
				.forEach( filter -> {
					final FieldType fieldType = new FieldType();
					fieldType.setName( filter.getName() );
					final SearchConditionData condition = new ContextualSearchConditionData( fieldType, filter.getValue(), filter.getOperator(),
							((ContextualFullTextSearchFilter) filter).getContext() );
					clearLocalizedValues( filter.getValue(), filter.getLocale() );
					conditions.add( condition );
				} );
		return conditions;
	}

	private boolean isFilterRequiredOrHasAValue(FullTextSearchFilter filter)
	{
		return !filter.getOperator().isRequireValue() || filter.getValue() != null;
	}

	protected void applyFilters( final ContextualAdvancedSearchData queryData )
	{
		final List<ContextualSearchConditionData> conditions = getValue( MODEL_FIELD_QUERIES, List.class );
		if( conditions != null )
		{
			for( ContextualSearchConditionData condition: conditions )
			{
				queryData.addContextualFilterQueryRawCondition( condition.getFieldType(), condition.getOperator(), condition.getValue(), condition.getContext() );
			}
		}
	}

	@Override
	protected boolean doSimpleSearch()
	{
		boolean searchExecuted = false;
		if( searchBox != null )
		{
			final String query = org.apache.commons.lang3.StringUtils.defaultIfBlank( getSearchText(), StringUtils.EMPTY );
			setValue( SIMPLE_SEARCH_TEXT_QUERY, query );
			final ContextualAdvancedSearchData searchData = new ContextualAdvancedSearchData( getValue( SEARCH_MODEL, AdvancedSearchData.class ) );
			final ContextualAdvancedSearchData queryData = buildQueryData( query, searchData.getTypeCode() );
			queryData.setTokenizable( true );
			queryData.setAdvancedSearchMode( AdvancedSearchMode.SIMPLE );
			queryData.setSelectedFacets( getValue( MODEL_KEY_SELECTED_FACETS, Map.class ) );
			applyFilters( queryData );
			sendOutput( SOCKET_OUT_SEARCH_DATA, queryData );
			searchExecuted = true;
		}
		return searchExecuted;
	}

	@Override
	protected ContextualAdvancedSearchData buildQueryData( final String searchText, final String typeCode )
	{
		final ContextualAdvancedSearchData queryData = createAdvancedSearchDataWithInitContext();
		queryData.setTypeCode( typeCode );
		queryData.setSearchQueryText( searchText );
		if( queryData.getGlobalOperator() == null )
		{
			queryData.setGlobalOperator( ValueComparisonOperator.OR );
		}
		queryData.setTokenizable( true );
		queryData.setIncludeSubtypes( Boolean.TRUE );
		applySimpleSearchConfiguration( searchText, typeCode, queryData );
		return queryData;
	}

	@Override
	protected ContextualAdvancedSearchData createAdvancedSearchDataWithInitContext()
	{
		ContextualAdvancedSearchData result = new ContextualAdvancedSearchData();
		final AdvancedSearchInitContext advancedSearchInitContext = getValue( MODEL_INIT_CONTEXT, AdvancedSearchInitContext.class );

		if( advancedSearchInitContext != null && advancedSearchInitContext.getAdvancedSearchData() != null )
		{
			result = new ContextualAdvancedSearchData( advancedSearchInitContext.getAdvancedSearchData() );
		}
		else
		{
			result = new ContextualAdvancedSearchData();
		}
		ValueComparisonOperator operator = handlePreferredGlobalOperator(result);
		result.setGlobalOperator(operator);
		return result;
	}
}
