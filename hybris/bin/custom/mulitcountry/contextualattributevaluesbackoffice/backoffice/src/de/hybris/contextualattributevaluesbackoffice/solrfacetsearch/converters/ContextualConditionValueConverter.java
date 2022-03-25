package de.hybris.contextualattributevaluesbackoffice.solrfacetsearch.converters;

import de.hybris.contextualattributevaluesbackoffice.solrfacetsearch.ContextualConditionValue;

import java.util.function.Function;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualConditionValueConverter implements Function<ContextualConditionValue, String>
{
	public String apply( ContextualConditionValue value )
	{
		if( value != null )
		{
			return value.getValue().toString();
		}
		return "";
	}
}
