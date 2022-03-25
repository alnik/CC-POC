/**
 *
 */
package de.hybris.multicountry.backoffice.search.decorators;

import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.SearchQuery.Operator;
import de.hybris.platform.store.BaseStoreModel;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.Ordered;

import com.hybris.backoffice.solrsearch.dataaccess.SearchConditionData;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.backoffice.solrsearch.decorators.SearchConditionDecorator;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public class AvailabilityGroupConditionDecorator implements SearchConditionDecorator
{

	private UserService userService;
	private String propertyName;
	private String indexedType;

	@Override
	public void decorate(final SearchConditionData searchConditionData, final SearchQueryData queryData,
			final IndexedType indexedType)
	{
		if (canDecorate(indexedType))
		{
			final SolrSearchCondition availabilityGroupCondition = prepareAvailabilityGroupCondition();
			if (availabilityGroupCondition != null)
			{
				searchConditionData.addFilterQueryCondition(availabilityGroupCondition);
			}
		}
	}

	protected boolean canDecorate(final IndexedType indexedType)
	{
		if (getUserService().isAdmin(getUserService().getCurrentUser()))
		{
			return false;
		}
		else if (getIndexedType() != null)
		{
			return StringUtils.equals(getIndexedType(), indexedType.getIdentifier());
		}
		return true;
	}

	protected SolrSearchCondition prepareAvailabilityGroupCondition()
	{
		final UserModel currentUser = getUserService().getCurrentUser();
		if (currentUser instanceof EmployeeModel)
		{
			final Set<ProductAvailabilityGroupModel> availabilityGroups = new HashSet<>();
			final Set<BaseStoreModel> managedStores = ((EmployeeModel) currentUser).getManagedStores();
			{
				for (final BaseStoreModel baseStore : managedStores)
				{
					availabilityGroups.addAll(baseStore.getAvailabilityGroups());
				}
			}
			if (!availabilityGroups.isEmpty())
			{
				final SolrSearchCondition availabilityGroupSearchCondition = new SolrSearchCondition(getPropertyName(), null,
						Operator.OR);
				for (final ProductAvailabilityGroupModel availabilityGroup : availabilityGroups)
				{
					availabilityGroupSearchCondition.addConditionValue(availabilityGroup.getId(), ValueComparisonOperator.EQUALS);
				}
				return availabilityGroupSearchCondition;
			}
			else
			{
				throw new IllegalStateException("User does not have availability groups assigned");
			}

		}
		return null;
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setPropertyName(final String propertyName)
	{
		this.propertyName = propertyName;
	}

	public String getPropertyName()
	{
		return propertyName;
	}

	public void setIndexedType(final String indexedType)
	{
		this.indexedType = indexedType;
	}

	public String getIndexedType()
	{
		return indexedType;
	}

}
