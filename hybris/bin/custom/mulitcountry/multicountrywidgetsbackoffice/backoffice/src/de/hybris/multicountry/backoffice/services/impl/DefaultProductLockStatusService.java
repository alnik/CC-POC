package de.hybris.multicountry.backoffice.services.impl;

import de.hybris.multicountry.backoffice.services.ProductLockStatusService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.multicountry.services.MulticountryProductLockingService;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public class DefaultProductLockStatusService implements ProductLockStatusService
{
	private static final String STATUS_LOCKED = "locked";
	private static final String STATUS_OWNED = "owned";
	private static final String STATUS_UNLOCKED = "unlocked";

	private MulticountryProductLockingService productLockingService;
	private UserService userService;


	@Override
	public String getStatusText(final ProductModel productModel)
	{
		final UserModel user = getUserService().getCurrentUser();
		if (!(user instanceof EmployeeModel)) {
			return STATUS_LOCKED;
		} else {
			return getStatusTextInternal(productModel, (EmployeeModel) user);
		}
	}

	private String getStatusTextInternal(final ProductModel product, final EmployeeModel employee) {
		return getProductLockingService().isProductLocked(product, employee)
				? STATUS_OWNED
				: getProductLockingService().isProductLocked(product) ? STATUS_LOCKED : STATUS_UNLOCKED;
	}

	public MulticountryProductLockingService getProductLockingService() {
		return productLockingService;
	}

	@Required
	public void setProductLockingService(MulticountryProductLockingService productLockingService) {
		this.productLockingService = productLockingService;
	}

	public UserService getUserService() {
		return userService;
	}

	@Required
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
