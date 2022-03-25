/**
 *
 */
package de.hybris.platform.multicountry.util;

import de.hybris.platform.catalog.model.CatalogVersionModel;
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


/**
 * @author i304602
 *
 */
public class CountryManagerUtil
{

	private static UserService userService;



	public static boolean canEditProduct(final ProductModel productModel)
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

		final EmployeeModel employee = (EmployeeModel) user;
		final Set<BaseStoreModel> stores = employee.getManagedStores();

		if (CollectionUtils.isEmpty(stores))
		{
			return true;
		}

		final Collection<ProductAvailabilityAssignmentModel> availabilities = productModel.getAvailability();
		if (CollectionUtils.isEmpty(availabilities))
		{
			return true;
		}

		for (final ProductAvailabilityAssignmentModel availability : availabilities){
			final ProductAvailabilityGroupModel group = availability.getAvailabilityGroup();
			if (group==null){
				continue;
			}

			final Set<BaseStoreModel> availabilityStores = group.getStores();
			for (final BaseStoreModel availabilityStore : availabilityStores)
			{
				if (stores.contains(availabilityStore))
				{
					return true;
				}
			}
		}

		//I didnt' find any store in availibity object....
		return false;
	}

	public static boolean canEditAvailabilityProduct(final ProductAvailabilityAssignmentModel availability)
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

		final EmployeeModel employee = (EmployeeModel) user;
		final Set<BaseStoreModel> stores = employee.getManagedStores();

		if (CollectionUtils.isEmpty(stores))
		{
			return true;
		}

		final ProductAvailabilityGroupModel group = availability.getAvailabilityGroup();
		if (group == null)
		{
			return true;
		}

		final Set<BaseStoreModel> availabilityStores = group.getStores();
		for (final BaseStoreModel availabilityStore : availabilityStores)
		{
			if (stores.contains(availabilityStore))
			{
				return true;
			}
		}

		return false;
	}

	public static boolean canEditAPriceRow(final PriceRowModel priceRowModel)
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

		if (priceRowModel.getUg() == null)
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
			if (store.getUserPriceGroup().equals(priceRowModel.getUg()))
			{
				return true;
			}
		}

		return false;
	}


	public static String getErrorMessage(final PriceRowModel priceRowModel)
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
			//	return true;
		}

		final StringBuffer sbEmploy = new StringBuffer();
		stores.forEach(store -> {
			sbEmploy.append(store.getUserPriceGroup() + " ");
		});

		final String message = "\nUser " + employee.getUid() + " cannot modifiy prices with pricegroup   " + priceRowModel.getUg()
				+ ", he can only modify prices with pricegroup " + sbEmploy.toString();
		return message;

	}



	public static String getErrorMessage(final CatalogVersionModel cv)
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


		if (CollectionUtils.isEmpty(cv.getCatalog().getBaseStores()))
		{
			return "";
		}

		final StringBuffer sbEmploy = new StringBuffer();
		stores.forEach(store -> {
			sbEmploy.append(store.getUid() + " ");
		});

		final StringBuffer sbCatalogs = new StringBuffer();
		cv.getCatalog().getBaseStores().forEach(store -> {
			sbCatalogs.append(store.getUid() + " ");
		});


		final String message = "\nUser " + employee.getUid() + " cannot modifiy object related to stores "
				+ sbCatalogs.toString().trim() + ", he can only modify object for stores " + sbEmploy.toString();
		return message;

	}

	public static String getErrorMessage(final ProductAvailabilityAssignmentModel availability)
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
			sbEmploy.append(store.getUid() + " ");
		});



		final Set<BaseStoreModel> avbStores = availability.getAvailabilityGroup().getStores();
		if (CollectionUtils.isEmpty(stores))
		{
			return "";
		}

		final StringBuffer sbCatalogs = new StringBuffer();
		avbStores.forEach(store -> {
			sbCatalogs.append(store.getUid() + " ");
		});


		final String message = "\nUser " + employee.getUid() + " cannot modifiy object related to stores "
				+ sbCatalogs.toString().trim() + ", he can only modify object for stores " + sbEmploy.toString();
		return message;

	}







	private static void init()
	{
		userService = Registry.getApplicationContext().getBean("userService", UserService.class);
	}









}
