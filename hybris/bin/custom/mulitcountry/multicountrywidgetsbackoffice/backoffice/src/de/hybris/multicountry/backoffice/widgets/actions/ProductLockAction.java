package de.hybris.multicountry.backoffice.widgets.actions;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import java.util.function.Predicate;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public class ProductLockAction extends AbstractProductLockAction
{
	/**
	 * Product locking can only be performed on product models that are lockable by the current user.
	 * @return True or false
	 */
	@Override
	protected Predicate<Object> getPerformCondition()
	{
		return object -> object instanceof ProductModel
				&& getProductLockingService().isProductLockable((ProductModel) object);
	}

	/**
	 * Locks the given product to the current user.
	 *
	 * @param product A non-null product
	 * @return True if the product is now locked by the current user
	 */
	@Override
	protected boolean performAction(final ProductModel product)
	{
		final EmployeeModel employee = getCurrentEmployee();
		return getProductLockingService().lockProduct(product, employee);
	}

	@Override
	protected String getNotificationEvent() {
		return EVENT_LOCKED;
	}
}