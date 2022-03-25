/**
 *
 */
package de.hybris.multicountry.backoffice.advisors;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.europe1.model.TaxRowModel;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Set;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public class TaxRowInstancePermissionAdvisor extends AbstractMultiCountryInstancePermissionAdvisor<TaxRowModel>
{


	@Override
	public boolean isApplicableTo(final Object instance)
	{
		return instance instanceof TaxRowModel;
	}

	@Override
	protected boolean isObjectWritable(final TaxRowModel taxRowModel)
	{
		boolean locked = false;
		final HybrisEnumValue userTaxGroup = taxRowModel.getUg();
		if (userTaxGroup != null)
		{
			final EmployeeModel currentUser = (EmployeeModel) getUserService().getCurrentUser();
			final Set<BaseStoreModel> baseStores = currentUser.getManagedStores();
			locked = true;
			for (final BaseStoreModel baseStore : baseStores)
			{
				if (baseStore.getTaxGroup().getCode().equals(userTaxGroup.getCode()))
				{
					locked = false;
					break;
				}
			}
		}
		return !locked;
	}

}
