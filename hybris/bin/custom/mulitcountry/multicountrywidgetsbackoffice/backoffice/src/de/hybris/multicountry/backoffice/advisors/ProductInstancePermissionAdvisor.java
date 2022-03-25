/**
 *
 */
package de.hybris.multicountry.backoffice.advisors;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.multicountry.services.MulticountryProductLockingService;
import org.springframework.beans.factory.annotation.Required;


/**
 * This permission advisor checks if the current user (employee) is allowed to edit a product.
 */
public class ProductInstancePermissionAdvisor extends AbstractMultiCountryInstancePermissionAdvisor<ProductModel> {

    private MulticountryProductLockingService productLockingService;

    @Override
    public boolean isApplicableTo(final Object instance) {
        return instance instanceof ProductModel;
    }

    /**
     * The object is "writable" if it is not locked, or is locked by the current user.
     *
     * @param productModel The product
     * @return True if the product is not locked, false otherwise
     */
    @Override
    protected boolean isObjectWritable(final ProductModel productModel) {
        return !getProductLockingService().isProductLockedForUser(productModel, getEmployee());
    }

    private EmployeeModel getEmployee() {
        final UserModel user = getUserService().getCurrentUser();
        return user instanceof EmployeeModel ? (EmployeeModel) user : null;
    }


    public MulticountryProductLockingService getProductLockingService() {
        return productLockingService;
    }

    @Required
    public void setProductLockingService(MulticountryProductLockingService productLockingService) {
        this.productLockingService = productLockingService;
    }
}
