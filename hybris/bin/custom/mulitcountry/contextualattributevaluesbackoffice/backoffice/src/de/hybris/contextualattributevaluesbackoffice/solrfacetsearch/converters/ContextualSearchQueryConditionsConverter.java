package de.hybris.contextualattributevaluesbackoffice.solrfacetsearch.converters;

import de.hybris.contextualattributevaluesbackoffice.solrfacetsearch.ContextualConditionValue;
import de.hybris.contextualattributevaluesbackoffice.solrfacetsearch.ContextualSolrSearchCondition;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.hybris.backoffice.solrsearch.converters.impl.DefaultSearchQueryConditionsConverter;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.cockpitng.search.data.SearchQueryCondition;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualSearchQueryConditionsConverter extends DefaultSearchQueryConditionsConverter
{
	@Override
	protected void appendAttributeCondition( List<SolrSearchCondition> convertedConditions, List<SearchQueryCondition> conditions, IndexedProperty property,
			SearchQuery.Operator operator )
	{
		if( property.isContextual() )
		{
			if( property.isLocalized() )
			{
				Map<Locale, List<SearchQueryCondition>> conditionsByLanguage = this.splitConditionsByLanguage( conditions );
				for( Map.Entry<Locale, List<SearchQueryCondition>> entry: conditionsByLanguage.entrySet() )
				{
					Locale language = entry.getKey();
					List<SearchQueryCondition> localizedConditions = entry.getValue();
					ContextualSolrSearchCondition contextualCondition = this.createContextualConditionForProperty( property, localizedConditions, operator,
							language );
					convertedConditions.add( contextualCondition );
				}
			}
			else
			{
				ContextualSolrSearchCondition contextualCondition = this.createContextualConditionForProperty( property, conditions, operator, null );
				convertedConditions.add( contextualCondition );
			}
		}
		else
		{
			super.appendAttributeCondition( convertedConditions, conditions, property, operator );
		}
	}

	protected ContextualSolrSearchCondition createContextualConditionForProperty( IndexedProperty indexedProperty, List<SearchQueryCondition> conditions,
			SearchQuery.Operator operator, Locale locale )
	{
		ContextualSolrSearchCondition convertedCondition = new ContextualSolrSearchCondition( indexedProperty.getName(), indexedProperty.getType(),
				indexedProperty.isMultiValue(), locale, operator, conditions.stream().allMatch( SearchQueryCondition::isFilteringCondition ),
				getContext( conditions ) );

		for( SearchQueryCondition condition: conditions )
		{
			Object value = locale != null ? this.extractLocalizedValue( condition.getValue() ) : condition.getValue();
			if( value != null || !condition.getOperator().isRequireValue() )
			{
				convertedCondition.addConditionValue( value, condition.getOperator() );
			}
		}
		return convertedCondition;
	}

	private List<ContextualAttributesContextModel> getContext( List<SearchQueryCondition> localizedConditions )
	{
		return localizedConditions.stream()
				.filter( searchQueryCondition -> searchQueryCondition.getValue() instanceof ContextualConditionValue )
				.findFirst()
				.map( searchQueryCondition -> ((ContextualConditionValue) searchQueryCondition.getValue()).getContext() )
				.orElse( null );
	}

	@Override
	protected Locale extractValueLocale( Object value )
	{
		if( value instanceof Map )
		{
			Map map = (Map) value;
			if( map.size() == 1 )
			{
				return (Locale) map.keySet().iterator().next();
			}
		}
		else if( value instanceof ContextualConditionValue )
		{
			return ((ContextualConditionValue) value).getLocale();
		}
		return null;
	}
}
