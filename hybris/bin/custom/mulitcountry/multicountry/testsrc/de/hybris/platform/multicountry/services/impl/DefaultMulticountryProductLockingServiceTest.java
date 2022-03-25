package de.hybris.platform.multicountry.services.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.ServicelayerTest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import javax.annotation.Resource;
import org.junit.Test;


@IntegrationTest
public class DefaultMulticountryProductLockingServiceTest extends ServicelayerTest {

    @Resource
    private DefaultMulticountryProductLockingService productLockingService;

    @Resource
    private CatalogVersionService catalogVersionService;

    @Resource
    private UserService userService;

    @Resource
    private ProductService productService;

    @Resource
    private SessionService sessionService;

    @Resource
    private ConfigurationService configurationService;


    /**
     * Tests the validations of the different "facade" methods. Functional tests will be separate.
     */
    @Test
    public void testValidations() {
        failWithoutIllegalArgument(() -> productLockingService.isProductLocked(null, new EmployeeModel()), "Product locked check requires a non-null product");
        failWithoutIllegalArgument(() -> productLockingService.isProductLocked(new ProductModel(), null), "Product locked check requires a non-null employee");

        failWithoutIllegalArgument(() -> productLockingService.isProductLocked(null), "Product locked check requires a non-null product");

        failWithoutIllegalArgument(() -> productLockingService.isProductLockedForUser(null, new EmployeeModel()), "Product locked-for check requires a non-null product");
        failWithoutIllegalArgument(() -> productLockingService.isProductLockedForUser(new ProductModel(), null), "Product locked-for check requires a non-null employee");

        failWithoutIllegalArgument(() -> productLockingService.isProductLockable(null), "Product lockable check requires a non-null employee");

        failWithoutIllegalArgument(() -> productLockingService.isProductUnlockable(null, new EmployeeModel()), "Product unlockable check requires a non-null product");
        failWithoutIllegalArgument(() -> productLockingService.isProductUnlockable(new ProductModel(), null), "Product unlockable check requires a non-null employee");

        failWithoutIllegalArgument(() -> productLockingService.lockProduct(null, new EmployeeModel()), "Product locking requires a non-null product");
        failWithoutIllegalArgument(() -> productLockingService.lockProduct(new ProductModel(), null), "Product locking requires a non-null employee");

        failWithoutIllegalArgument(() -> productLockingService.unlockProduct(null, new EmployeeModel()), "Product unlocking requires a non-null product");
        failWithoutIllegalArgument(() -> productLockingService.unlockProduct(new ProductModel(), null), "Product unlocking requires a non-null employee");
    }

    /**
     * Tests locking and unlocking combinations.
     */
    @Test
    public void testLockingAndUnlocking() throws Exception {
        createCoreData();
        createHardwareCatalog();
        createTestUsers();


        final EmployeeModel pm1 = (EmployeeModel) userService.getUserForUID("productmanager1");
        final EmployeeModel pm2 = (EmployeeModel) userService.getUserForUID("productmanager2");
        final EmployeeModel admin = userService.getAdminUser();

        sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public void executeWithoutResult() {
                // as pm1
                userService.setCurrentUser(pm1);
                catalogVersionService.setSessionCatalogVersion("hwcatalog", "Online");

                final ProductModel product1 = productService.getProductForCode("HW2300-2356");
                final ProductModel product2 = productService.getProductForCode("HW2300-3843");
                final ProductModel product3 = productService.getProductForCode("HW2300-4121");

                // none of the products are locked
                assertFalse(productLockingService.isProductLocked(product1));
                assertFalse(productLockingService.isProductLocked(product2));
                assertFalse(productLockingService.isProductLocked(product3));
                assertFalse(productLockingService.isProductLockedForUser(product1, pm1));
                assertFalse(productLockingService.isProductLockedForUser(product2, pm2));
                assertFalse(productLockingService.isProductLockedForUser(product3, admin));
                assertTrue(productLockingService.isProductLockable(product1));
                assertTrue(productLockingService.isProductLockable(product2));
                assertTrue(productLockingService.isProductLockable(product3));

                // lock the products
                assertTrue(productLockingService.lockProduct(product1, pm1));
                assertTrue(productLockingService.lockProduct(product2, pm2));
                assertTrue(productLockingService.lockProduct(product3, admin));
                // no longer lockable
                assertFalse(productLockingService.isProductLockable(product1));
                assertFalse(productLockingService.isProductLockable(product2));
                assertFalse(productLockingService.isProductLockable(product3));

                // check the locks - are they locked by anyone?
                assertTrue(productLockingService.isProductLocked(product1));
                assertTrue(productLockingService.isProductLocked(product2));
                assertTrue(productLockingService.isProductLocked(product3));
                // are they locked by a specific user?
                assertTrue(productLockingService.isProductLocked(product1, pm1));
                assertFalse(productLockingService.isProductLocked(product1, pm2));
                assertFalse(productLockingService.isProductLocked(product1, admin));
                // are they locked FOR a specific user?
                assertFalse(productLockingService.isProductLockedForUser(product1, pm1));
                assertTrue(productLockingService.isProductLockedForUser(product1, pm2));
                assertTrue(productLockingService.isProductLockedForUser(product1, admin));
                // are they unlockable by a specific user?
                assertTrue(productLockingService.isProductUnlockable(product1, pm1));
                assertFalse(productLockingService.isProductUnlockable(product1, pm2));
                assertFalse(productLockingService.isProductUnlockable(product1, admin));

                // re-lock the products, same users - not allowed!
                assertFalse(productLockingService.lockProduct(product1, pm1));
                assertFalse(productLockingService.lockProduct(product2, pm2));
                assertFalse(productLockingService.lockProduct(product3, admin));
                // re-lock by different users - not allowed
                assertFalse(productLockingService.lockProduct(product1, pm2));
                assertFalse(productLockingService.lockProduct(product2, pm1));
                assertFalse(productLockingService.lockProduct(product1, admin));

                // unlock the products by different users - not allowed
                assertFalse(productLockingService.unlockProduct(product1, pm2));
                assertFalse(productLockingService.unlockProduct(product2, pm1));
                assertFalse(productLockingService.unlockProduct(product1, admin));

                // unlock the products by correct users
                assertTrue(productLockingService.unlockProduct(product1, pm1));
                assertTrue(productLockingService.unlockProduct(product2, pm2));
                assertTrue(productLockingService.unlockProduct(product3, admin));

                // lock a product, then change to admin user
                productLockingService.lockProduct(product1, pm1);

                userService.setCurrentUser(admin);
                catalogVersionService.setSessionCatalogVersion("hwcatalog", "Online");

                assertFalse(productLockingService.isProductLockable(product1));
                assertFalse(productLockingService.isProductLockedForUser(product1, pm1));
                assertTrue(productLockingService.isProductLockedForUser(product1, admin));
                assertFalse(productLockingService.isProductUnlockable(product1, admin));
                assertFalse(productLockingService.lockProduct(product1, admin));
                assertFalse(productLockingService.unlockProduct(product1, admin));

                // change the configuration to allow admin unlocking
                configurationService.getConfiguration().setProperty(DefaultMulticountryProductLockingService.CONFIG_ADMIN_UNLOCKING_ENABLED, Boolean.TRUE.toString());
                assertFalse(productLockingService.isProductLockable(product1));
                assertTrue(productLockingService.isProductUnlockable(product1, admin));
                assertFalse(productLockingService.lockProduct(product1, admin));
                assertTrue(productLockingService.unlockProduct(product1, admin));


                // lock a product and then switch to user pm2
                productLockingService.lockProduct(product3, admin);

                userService.setCurrentUser(pm2);
                catalogVersionService.setSessionCatalogVersion("hwcatalog", "Online");

                // this user cannot change the locks (only users in admin group can do that)
                assertFalse(productLockingService.isProductLockable(product3));
                assertFalse(productLockingService.isProductLockedForUser(product3, admin));
                assertTrue(productLockingService.isProductLockedForUser(product3, pm2));
                assertFalse(productLockingService.isProductUnlockable(product3, pm2));
                assertFalse(productLockingService.lockProduct(product3, pm2));
                assertFalse(productLockingService.unlockProduct(product3, pm2));

                // danger zone! this is allowed and requires users of this service to be careful with their actions
                // unlock using a known user (not current user)
                assertTrue(productLockingService.unlockProduct(product3, admin));
            }
        });
    }

    private void createTestUsers() throws Exception {
        importCsv("/multicountry/test/productLockingUsers.impex", StandardCharsets.UTF_8.toString());
    }

    private void failWithoutIllegalArgument(final Callable<Object> callable, final String message) {
        try {
            callable.call();
            fail(message);
        } catch (final IllegalArgumentException ex) {
            //no-op
        } catch (final Throwable th) {
            fail("Unexpected exception: " + th.getMessage());
        }
    }
}