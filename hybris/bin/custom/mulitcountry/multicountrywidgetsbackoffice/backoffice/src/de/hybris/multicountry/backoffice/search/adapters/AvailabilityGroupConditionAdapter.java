/**
 *
 */
package de.hybris.multicountry.backoffice.search.adapters;

import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.backoffice.widgets.searchadapters.conditions.SearchConditionAdapter;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public class AvailabilityGroupConditionAdapter extends SearchConditionAdapter implements PriorityOrdered
{

	private String propertyName;
	private ValueComparisonOperator operator;

	@Override
	public boolean canHandle(final NavigationNode node)
	{
		return hasBaseStoreAncestor(node);
	}

	protected boolean hasBaseStoreAncestor(final NavigationNode node)
	{
		return getBaseStoreAncestor(node) != null;
	}

	protected BaseStoreModel getBaseStoreAncestor(final NavigationNode node)
	{
		if (node.getData() instanceof BaseStoreModel)
		{
			return (BaseStoreModel) node.getData();
		}
		else
		{
			if (node.getParent() != null)
			{
				return getBaseStoreAncestor(node.getParent());
			}
		}
		return null;
	}

	@Override
	public void addSearchCondition(final AdvancedSearchData searchData, final NavigationNode node)
	{
		final BaseStoreModel baseStore = getBaseStoreAncestor(node);
		final Collection<ProductAvailabilityGroupModel> availabilityGroups = baseStore.getAvailabilityGroups();
		final List<SearchConditionData> searchConditions = availabilityGroups.stream()
				.map(group -> this.createSearchConditions(this.propertyName, group.getId(), this.operator))
				.collect(Collectors.toList());
		searchData.addConditionList(this.operator, searchConditions);
	}


	@Required
	public void setPropertyName(final String propertyName)
	{
		this.propertyName = propertyName;
	}

	@Required
	public void setOperator(final ValueComparisonOperator operator)
	{
		this.operator = operator;
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
