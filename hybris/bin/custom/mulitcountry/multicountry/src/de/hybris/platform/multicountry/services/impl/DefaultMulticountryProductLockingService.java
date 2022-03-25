package de.hybris.platform.multicountry.services.impl;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.multicountry.services.MulticountryProductLockingService;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;

/**
 * Default implementation of the MulticountryProductLockingService.
 */
public class DefaultMulticountryProductLockingService implements MulticountryProductLockingService {

    protected static final String CONFIG_ADMIN_UNLOCKING_ENABLED = "multicountry.admin-unlocking.enabled";
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMulticountryProductLockingService.class);
    private static final String MSG_PROD_NOT_NULL = "The product cannot be null";
    private static final String MSG_EMP_NOT_NULL = "The employee cannot be null";

    @Resource
    private SessionService sessionService;

    @Resource
    private SearchRestrictionService searchRestrictionService;

    @Resource
    private ModelService modelService;

    @Resource
    private UserService userService;

    @Resource
    private ConfigurationService configurationService;


    @Override
    public boolean isProductLocked(final ProductModel product) {
        ServicesUtil.validateParameterNotNull(product, MSG_PROD_NOT_NULL);

        return isLockedInternal(product);
    }

    @Override
    public boolean isProductLocked(final ProductModel product, final EmployeeModel employee) {
        ServicesUtil.validateParameterNotNull(product, MSG_PROD_NOT_NULL);
        ServicesUtil.validateParameterNotNull(employee, MSG_EMP_NOT_NULL);

        return isLockedByInternal(product, employee);
    }

    @Override
    public boolean isProductLockedForUser(final ProductModel product, final EmployeeModel employee) {
        ServicesUtil.validateParameterNotNull(product, MSG_PROD_NOT_NULL);
        ServicesUtil.validateParameterNotNull(employee, MSG_EMP_NOT_NULL);

        return isLockedForInternal(product, employee);
    }

    @Override
    public boolean isProductLockable(final ProductModel product) {
        ServicesUtil.validateParameterNotNull(product, MSG_PROD_NOT_NULL);

        return !isProductLocked(product);
    }

    @Override
    public boolean isProductUnlockable(final ProductModel product, final EmployeeModel employee) {
        ServicesUtil.validateParameterNotNull(product, MSG_PROD_NOT_NULL);
        ServicesUtil.validateParameterNotNull(employee, MSG_EMP_NOT_NULL);

        return isUnlockableInternal(product, employee);
    }

    @Override
    public boolean lockProduct(final ProductModel product, final EmployeeModel employee) {
        ServicesUtil.validateParameterNotNull(product, MSG_PROD_NOT_NULL);
        ServicesUtil.validateParameterNotNull(employee, MSG_EMP_NOT_NULL);

        if (isProductLocked(product)) {
            return false;
        }

        lockProductInternal(product, employee);

        modelService.refresh(product);
        return isLockedByInternal(product, employee);
    }

    @Override
    public boolean unlockProduct(final ProductModel product, final EmployeeModel employee) {
        ServicesUtil.validateParameterNotNull(product, MSG_PROD_NOT_NULL);
        ServicesUtil.validateParameterNotNull(employee, MSG_EMP_NOT_NULL);

        if (!isUnlockableInternal(product, employee)) {
            return false;
        }

        unlockProductInternal(product);

        modelService.refresh(product);
        // unlocking should remove ALL locks, so we confirm that there are none (not only for given employee)
        return !isLockedInternal(product);
    }

    /**
     * Checks if the product has a lock by any user. The check is made without search restrictions to ensure that locks held by
     * system users (i.e. admins) can be seen in any context.
     *
     * @param product The product
     * @return True or false
     */
    protected boolean isLockedInternal(final ProductModel product) {
        return sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public Object execute() {
                searchRestrictionService.disableSearchRestrictions();
                final ProductModel productModel = modelService.get(product.getPk());
                modelService.refresh(productModel);

                return CollectionUtils.isNotEmpty(productModel.getLockedBy());
            }
        });
    }

    /**
     * Checks if the product is locked by the given employee.
     *
     * @param product The product
     * @param employee The employee
     * @return True if the product is locked by the employee, false if it is unlocked or locked by another employee
     */
    protected boolean isLockedByInternal(final ProductModel product, final EmployeeModel employee) {
        return sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public Object execute() {
                searchRestrictionService.disableSearchRestrictions();
                final ProductModel productModel = modelService.get(product.getPk());
                modelService.refresh(productModel);

                final Collection<EmployeeModel> lockers = productModel.getLockedBy();
                return CollectionUtils.isNotEmpty(lockers) && lockers.contains(employee);
            }
        });
    }

    /**
     * Confirms that the product is locked but the given employee is NOT in the list of lockers. The check is made without search
     * restrictions to ensure that locks held by system users (i.e. admins) can be seen in any context.
     *
     * @param product The product
     * @param employee The employee
     * @return True or false
     */
    protected boolean isLockedForInternal(final ProductModel product, final EmployeeModel employee) {
        return sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public Object execute() {
                searchRestrictionService.disableSearchRestrictions();
                final ProductModel productModel = modelService.get(product.getPk());
                modelService.refresh(productModel);

                final Collection<EmployeeModel> lockers = productModel.getLockedBy();
                return CollectionUtils.isNotEmpty(lockers) && !lockers.contains(employee);
            }
        });
    }

    /**
     * Checks if the product can be unlocked by the employee. Non-admins can only unlock products for which they hold the lock.
     * Admins can unlock any product, even if it's not their lock, but only if this option is enabled.
     *
     * @param product The product
     * @param employee The employee
     * @return True if the product is locked and can be unlocked by the employee, false if the product has no locks or the
     * employee is not allowed to remove the current lock
     */
    protected boolean isUnlockableInternal(final ProductModel product, final EmployeeModel employee) {
        if (isAdminUnlockingAllowed() && userService.isAdmin(employee)) {
            return isProductLocked(product);
        } else {
            return isProductLocked(product, employee);
        }
    }

    protected boolean isAdminUnlockingAllowed() {
        return configurationService.getConfiguration().getBoolean(CONFIG_ADMIN_UNLOCKING_ENABLED, false);
    }

    /**
     * Locks the product by the given employee. Any existing lock will be lost; the caller of this method must first ensure that
     * the lock is not already held by another employee.
     *
     * @param product The product
     * @param employee The employee
     */
    protected void lockProductInternal(final ProductModel product, final EmployeeModel employee) {
        LOG.debug("Locking product {} by employee {}, there are currently {} lock(s)", product.getCode(), employee.getUid(), CollectionUtils.size(product.getLockedBy()));

        product.setLockedBy(Collections.singleton(employee));
        modelService.save(product);
    }

    /**
     * Unlocks a product by removing all locks.
     *
     * @param product The product
     */
    protected void unlockProductInternal(final ProductModel product) {
        LOG.debug("Unlocking product {}, there are currently {} lock(s)", product.getCode(), CollectionUtils.size(product.getLockedBy()));

        product.setLockedBy(Collections.emptySet());
        modelService.save(product);
    }
}
