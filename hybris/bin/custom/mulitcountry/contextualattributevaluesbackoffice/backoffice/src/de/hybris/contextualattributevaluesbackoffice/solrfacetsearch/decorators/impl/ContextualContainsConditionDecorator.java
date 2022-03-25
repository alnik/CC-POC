package de.hybris.contextualattributevaluesbackoffice.solrfacetsearch.decorators.impl;

import de.hybris.contextualattributevaluesbackoffice.solrfacetsearch.ContextualSolrSearchCondition;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.backoffice.solrsearch.decorators.impl.DefaultContainsConditionDecorator;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;

/**
 * @author florian.mueller07@sap.com
 */
public class ContextualContainsConditionDecorator extends DefaultContainsConditionDecorator
{
	@Override
	protected SolrSearchCondition extractConditionFromValue( SolrSearchCondition condition, SolrSearchCondition.ConditionValue conditionValue )
	{
		if( condition instanceof ContextualSolrSearchCondition )
		{
			ContextualSolrSearchCondition extractedCondition;
			if( ValueComparisonOperator.CONTAINS.equals( conditionValue.getComparisonOperator() ) )
			{
				extractedCondition = new ContextualSolrSearchCondition( condition.getAttributeName(), condition.getAttributeType(), condition.isMultiValue(),
						condition.getLanguage(), SearchQuery.Operator.OR, condition.isFilterQueryCondition(),
						((ContextualSolrSearchCondition) condition).getContext() );
				extractedCondition.addConditionValue( conditionValue.getValue(), conditionValue.getComparisonOperator() );
				extractedCondition.addConditionValue( conditionValue.getValue(), ValueComparisonOperator.EQUALS );
			}
			else
			{
				extractedCondition = new ContextualSolrSearchCondition( condition.getAttributeName(), condition.getAttributeType(), condition.isMultiValue(),
						condition.getLanguage(), condition.getOperator(), condition.isFilterQueryCondition(),
						((ContextualSolrSearchCondition) condition).getContext() );
				extractedCondition.addConditionValue( conditionValue.getValue(), conditionValue.getComparisonOperator() );
			}
			return extractedCondition;
		}
		else
		{
			return super.extractConditionFromValue( condition, conditionValue );
		}
	}
}
