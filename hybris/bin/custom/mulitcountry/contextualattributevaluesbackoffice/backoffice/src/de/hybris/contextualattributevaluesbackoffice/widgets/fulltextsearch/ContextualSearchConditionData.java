package de.hybris.contextualattributevaluesbackoffice.widgets.fulltextsearch;

import de.hybris.platform.contextualattributevalues.model.ContextualAttributesContextModel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualSearchConditionData extends SearchConditionData
{
	private List<ContextualAttributesContextModel> context;

	@JsonCreator
	public ContextualSearchConditionData( @JsonProperty("fieldType") final FieldType fieldType, @JsonProperty("value") final Object value,
			@JsonProperty("operator") final ValueComparisonOperator operator, @JsonProperty("context") final List<ContextualAttributesContextModel> context )
	{
		super( fieldType, value, operator );
		this.context = context;
	}

	public List<ContextualAttributesContextModel> getContext()
	{
		return context;
	}
}
