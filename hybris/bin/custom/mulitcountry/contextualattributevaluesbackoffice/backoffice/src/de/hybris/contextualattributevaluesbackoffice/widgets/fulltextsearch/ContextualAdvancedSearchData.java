package de.hybris.contextualattributevaluesbackoffice.widgets.fulltextsearch;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.json.ser.PolymorphicSerialization;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualAdvancedSearchData extends AdvancedSearchData
{
	@JsonDeserialize(using = PolymorphicSerialization.Deserializer.class)
	private final Map<String, List<SearchConditionData>> filterQueryRawConditions;

	public ContextualAdvancedSearchData( AdvancedSearchData advancedSearchData )
	{
		super( advancedSearchData );
		filterQueryRawConditions = new LinkedHashMap<>();
	}

	public ContextualAdvancedSearchData()
	{
		filterQueryRawConditions = new LinkedHashMap<>();
	}

	public void addFilterQueryRawCondition( final FieldType field, final ValueComparisonOperator operator, final Object value )
	{
		addConditionToCollection( filterQueryRawConditions, field, operator, value, null );
	}

	public void addContextualFilterQueryRawCondition( final FieldType field, final ValueComparisonOperator operator, final Object value,
			List<ContextualAttributesContextModel> context )
	{
		addConditionToCollection( filterQueryRawConditions, field, operator, value, context );
	}

	private static void addConditionToCollection( final Map<String, List<SearchConditionData>> target, final FieldType field,
			final ValueComparisonOperator operator, final Object value, List<ContextualAttributesContextModel> context )
	{
		if( !target.containsKey( field.getName() ) )
		{
			target.put( field.getName(), new ArrayList<>() );
		}
		target.get( field.getName() ).add( new ContextualSearchConditionData( field, value, operator, context ) );
	}

	@Override
	public Set<String> getFilterQueryFields()
	{
		return Collections.unmodifiableSet( filterQueryRawConditions.keySet() );
	}

	@Override
	public List<SearchConditionData> getFilterQueryRawConditions( String name )
	{
		return filterQueryRawConditions.get( name );
	}
}
