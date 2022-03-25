package de.hybris.platform.multicountry.services;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.EmployeeModel;

/**
 * This service allows for locking and unlocking of products, as well as providing checks on the lock state for a product. A lock
 * can only ever be held by a single employee, even though the model provides for a collection of employees.
 */
public interface MulticountryProductLockingService {

    /**
     * Is the product locked by any user?
     *
     * @param product The product
     * @return True or false
     */
    boolean isProductLocked(final ProductModel product);

    /**
     * Is the product locked by the given user?
     *
     * @param product The product
     * @param employee The employee
     * @return True or false
     */
    boolean isProductLocked(final ProductModel product, final EmployeeModel employee);

    /**
     * Is the product locked by someone other than the current user? This can be used as a proxy to determine editability of the
     * product: when false, the current user is the owner and can therefore edit. Must not be used to determine if the product
     * can or cannot be locked by a given user.
     *
     * @param product The product
     * @param employee The employee
     * @return True if the user is NOT the locker, false otherwise
     */
    boolean isProductLockedForUser(final ProductModel product, final EmployeeModel employee);

    /**
     * Can the product be locked?
     *
     * @param product The product
     * @return True or false
     */
    boolean isProductLockable(final ProductModel product);

    /**
     * Can the current user unlock the product?
     *
     * @param product The product
     * @param employee The employee
     * @return True or false
     */
    boolean isProductUnlockable(final ProductModel product, final EmployeeModel employee);

    /**
     * Locks the product by the current user.
     *
     * @param product The product
     * @param employee The employee
     * @return True if the product was locked, false if the operation could not be completed
     */
    boolean lockProduct(final ProductModel product, final EmployeeModel employee);

    /**
     * Removes the lock on the product held by the current user.
     *
     * @param product The product
     * @param employee The employee
     * @return True if the product was unlocked, false if the operation could not be completed
     */
    boolean unlockProduct(final ProductModel product, final EmployeeModel employee);
}
