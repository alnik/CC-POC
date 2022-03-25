/**
 *
 */
package de.hybris.platform.contextualattributevalues.util;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.contextualattributevalues.model.ContextualAttributeValueModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;



public class ContextualAttributeValuesUtil
{

	private static UserService userService;


	public static boolean canEditContextualAttributeValue(final ContextualAttributeValueModel model)
	{
		if (userService == null)
		{
			init();
		}

		final UserModel user = userService.getCurrentUser();
		if (!(user instanceof EmployeeModel))
		{
			return true;
		}

		if (model.getContext() == null)
		{
			return true;
		}

		final EmployeeModel employee = (EmployeeModel) user;
		final Set<BaseStoreModel> stores = employee.getManagedStores();

		if (CollectionUtils.isEmpty(stores))
		{
			return true;
		}

		for (final BaseStoreModel store : stores)
		{
			if (model.getContext().equals(store.getContextualAttributesContext()))
			{
				return true;
			}
		}

		return false;
	}

	public static String getErrorMessage(final ContextualAttributeValueModel attribute)
	{
		if (userService == null)
		{
			init();
		}

		final UserModel user = userService.getCurrentUser();
		if (!(user instanceof EmployeeModel))
		{
			return "";
		}

		final EmployeeModel employee = (EmployeeModel) user;
		final Set<BaseStoreModel> stores = employee.getManagedStores();

		if (CollectionUtils.isEmpty(stores))
		{
			return "";
		}

		final StringBuffer sbEmploy = new StringBuffer();
		stores.forEach(store -> {
			if (store.getContextualAttributesContext() != null)
			{
				sbEmploy.append(store.getContextualAttributesContext().getCode() + " ");
			}
		});

		final String message = "\nUser " + employee.getUid() + " cannot modifiy attributes for context    "
				+ attribute.getContext().getCode() + ", he can only modify attributes for contexts " + sbEmploy.toString();
		return message;

	}


	private static void init()
	{
		userService = Registry.getApplicationContext().getBean("userService", UserService.class);
	}

}
