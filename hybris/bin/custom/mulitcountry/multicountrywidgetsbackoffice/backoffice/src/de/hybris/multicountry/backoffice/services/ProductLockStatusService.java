/**
 *
 */
package de.hybris.multicountry.backoffice.services;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;


/**
 * @author cyrill.pedol@sap.com
 *
 */
public interface ProductLockStatusService
{
	/**
	 * Gets the status text appropriate for the current user.
	 *
	 * @param productModel The product, not null
	 * @return The status text
	 */
	String getStatusText(ProductModel productModel);
}
