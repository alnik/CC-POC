/**
 *
 */
package de.hybris.multicountry.backoffice.advisors;

import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Collection;
import java.util.Set;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public class ProductAvailabilityAssignmentInstancePermissionAdvisor
		extends AbstractMultiCountryInstancePermissionAdvisor<ProductAvailabilityAssignmentModel>
{

	@Override
	public boolean isApplicableTo(final Object instance)
	{
		return instance instanceof ProductAvailabilityAssignmentModel;
	}

	@Override
	protected boolean isObjectWritable(final ProductAvailabilityAssignmentModel productAvailabilityAssignmentModel)
	{
		boolean locked = false;
		final ProductAvailabilityGroupModel productAvailabilityGroupModel = productAvailabilityAssignmentModel
				.getAvailabilityGroup();
		if (productAvailabilityGroupModel != null)
		{
			final Set<BaseStoreModel> baseStores = productAvailabilityGroupModel.getStores();
			locked = true;
			for (final BaseStoreModel baseStore : baseStores)
			{
				if (checkEmployeeOnBaseStore(baseStore.getEmployees(), getUserService().getCurrentUser()))
				{
					locked = false;
					break;
				}
			}
		}
		return !locked;
	}

	protected boolean checkEmployeeOnBaseStore(final Collection<EmployeeModel> employees, final UserModel currentUser)
	{
		for (final EmployeeModel employee : employees)
		{
			if (employee.equals(currentUser))
			{
				return true;
			}
		}
		return false;
	}

}
