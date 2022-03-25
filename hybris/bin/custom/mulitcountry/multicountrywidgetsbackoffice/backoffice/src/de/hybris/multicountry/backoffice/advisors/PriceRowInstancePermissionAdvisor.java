/**
 *
 */
package de.hybris.multicountry.backoffice.advisors;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Collection;
import java.util.Set;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public class PriceRowInstancePermissionAdvisor extends AbstractMultiCountryInstancePermissionAdvisor<PriceRowModel>
{

	@Override
	public boolean isApplicableTo(final Object instance)
	{
		return instance instanceof PriceRowModel;
	}

	@Override
	protected boolean isObjectWritable(final PriceRowModel priceRowModel)
	{
		boolean locked = false;
		final HybrisEnumValue userPriceGroup = priceRowModel.getUg();
		if (userPriceGroup != null)
		{
			final EmployeeModel currentUser = (EmployeeModel) getUserService().getCurrentUser();
			final Set<BaseStoreModel> baseStores = currentUser.getManagedStores();
			locked = true;
			for (final BaseStoreModel baseStore : baseStores)
			{
				if (baseStore.getUserPriceGroup().getCode().equals(userPriceGroup.getCode()))
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
