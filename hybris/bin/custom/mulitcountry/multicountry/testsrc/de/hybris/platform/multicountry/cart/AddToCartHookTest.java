/**
 *
 */
package de.hybris.platform.multicountry.cart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commerceservices.order.impl.DefaultCommerceCartCalculationStrategy;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.multicountry.constants.MulticountryConstants;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.services.BaseStoreService;

import java.util.Collections;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;


/**
 * @author daniel.norberg
 *
 * 		  @RunWith(HybrisJUnit4ClassRunner.class) @RunListeners( { ItemCreationListener.class, LogRunListener.class,
 *         PlatformRunListener.class })
 */
@IntegrationTest
public class AddToCartHookTest extends ServicelayerTransactionalTest
{
    
    private static final Logger LOG = Logger.getLogger(AddToCartHookTest.class);
    
    private static final String TEST_BASESITE_UID = "testSite";
    
    private static final String GROUP = "pag-test-1";
    
    private static final double EPS = 0.001;
    
    @Resource
    private UserService userService;
    @Resource
    private SessionService sessionService;
    @Resource
    private CatalogVersionService catalogVersionService;
    @Resource
    private ModelService modelService;
    @Resource
    private FlexibleSearchService flexibleSearchService;
    @Resource
    private ProductService productService;
    
    @Resource
    private BaseSiteService baseSiteService;
    @Resource
    private BaseStoreService baseStoreService; //NOPMD
    @Resource
    private CartFacade cartFacade;
    
    @Resource
    private DefaultCommerceCartCalculationStrategy commerceCartCalculationStrategy;
    
    
    private CatalogVersionModel catalogVersion;
    private ProductAvailabilityGroupModel availabilityGroup;
    
    @Before
    public void setUp() throws ImpExException
    {
        
        MockitoAnnotations.initMocks(this);
        
        // Perform import and setup as admin user
        userService.setCurrentUser(userService.getAnonymousUser());
        
        importCsv("/multicountry/test/testAddToCart.impex", "UTF-8");
        
        // Set current site
        final BaseSiteModel baseSiteForUID = baseSiteService.getBaseSiteForUID(TEST_BASESITE_UID);
        baseSiteService.setCurrentBaseSite(baseSiteForUID, false);
        
        // get Catalog version
        catalogVersion = catalogVersionService.getCatalogVersion("testCatalog", "Online");
        assertNotNull(catalogVersion);
        catalogVersionService.setSessionCatalogVersions(Collections.singletonList(catalogVersion));
        
        // get availability group
        availabilityGroup = flexibleSearchService
        .<ProductAvailabilityGroupModel> search("SELECT {pk} FROM {ProductAvailabilityGroup} WHERE {id} = ?code",
                                                Collections.singletonMap("code", GROUP))
        .getResult().iterator().next();
        
    }
    
    @Test
    public void addProductWithoutGroups() throws Exception
    {
        
        // get products
        final ProductModel product1 = productService
        .getProductForCode(catalogVersionService.getSessionCatalogVersions().iterator().next(), "HW1210-3422");
        assertNotNull(product1);
        final ProductModel product2 = productService
        .getProductForCode(catalogVersionService.getSessionCatalogVersions().iterator().next(), "HW1210-3423");
        assertNotNull(product2);
        
        // add products without availabilty
        addItemsToCartBeforeCheckout(product1, product2);
    }
    
    @Test
    public void addProductWithGroups() throws Exception
    {
        
        // get products
        final ProductModel product1 = productService
        .getProductForCode(catalogVersionService.getSessionCatalogVersions().iterator().next(), "HW1210-3422");
        assertNotNull(product1);
        final ProductModel product2 = productService
        .getProductForCode(catalogVersionService.getSessionCatalogVersions().iterator().next(), "HW1210-3423");
        assertNotNull(product2);
        
        // set the session groups
        sessionService.setAttribute(MulticountryConstants.AVAILABILITY_GROUPS, Lists.newArrayList(availabilityGroup));
        
        // add products without availabilty asssigned
        addItemsToCartBeforeCheckout(product1, product2);
    }
    
    @Test
    public void addProductWithGroupsAndProductAvailability() throws Exception
    {
        
        // get products
        final ProductModel product1 = productService
        .getProductForCode(catalogVersionService.getSessionCatalogVersions().iterator().next(), "HW1210-3422");
        assertNotNull(product1);
        final ProductModel product2 = productService
        .getProductForCode(catalogVersionService.getSessionCatalogVersions().iterator().next(), "HW1210-3423");
        assertNotNull(product2);
        
        setStatus(product1, ArticleApprovalStatus.APPROVED);
        setStatus(product2, ArticleApprovalStatus.APPROVED);
        
        // set the session groups
        sessionService.setAttribute(MulticountryConstants.AVAILABILITY_GROUPS, Lists.newArrayList(availabilityGroup));
        
        
        // add products with availabilty asssigned
        addItemsToCartBeforeCheckout(product1, product2);
        
    }
    
    @Test
    public void addProductWithGroupsAndProductAvailabilityBlocked() throws Exception
    {
        
        // get products
        final ProductModel product1 = productService
        .getProductForCode(catalogVersionService.getSessionCatalogVersions().iterator().next(), "HW1210-3422");
        assertNotNull(product1);
        final ProductModel product2 = productService
        .getProductForCode(catalogVersionService.getSessionCatalogVersions().iterator().next(), "HW1210-3423");
        assertNotNull(product2);
        
        setStatus(product1, ArticleApprovalStatus.UNAPPROVED);
        setStatus(product2, ArticleApprovalStatus.UNAPPROVED);
        
        // set the session groups
        sessionService.setAttribute(MulticountryConstants.AVAILABILITY_GROUPS, Lists.newArrayList(availabilityGroup));
        
        // add products with availabilty asssigned
        addItemsToCartBeforeCheckout(product1, product2);
        
    }
    
    
    protected void addItemsToCartBeforeCheckout(final ProductModel product1, final ProductModel product2) throws Exception
    {
        assertNotNull(product1);
        assertNotNull(product2);
        
        LOG.info("Add 1 item of product 1, unit cost 50 EUR...");
        
        cartFacade.addToCart(product1.getCode(), 1);
        
        
        LOG.info("Add 2 items of product 2, unit cost 100 EUR...");
        
        cartFacade.addToCart(product2.getCode(), 2);
        
        
        LOG.info("Verify cart size...");
        assertEquals(2, cartFacade.getSessionCart().getEntries().size());
        
        LOG.info("Verify products in cart...");
        assertEquals("HW1210-3422", cartFacade.getSessionCart().getEntries().get(0).getProduct().getCode());
        assertEquals("HW1210-3423", cartFacade.getSessionCart().getEntries().get(1).getProduct().getCode());
        
        LOG.info("Verify cart total price...");
        assertEquals(250, cartFacade.getSessionCart().getTotalPrice().getValue().doubleValue(), EPS);
        //assertEquals("EUR", cartFacade.getSessionCart().getTotalPrice().getCurrencyIso());
    }
    
    private void setStatus(final ProductModel product, final ArticleApprovalStatus approved)
    {
        
        final ProductAvailabilityAssignmentModel availability;
        
        if (product.getAvailability() != null && !product.getAvailability().isEmpty())
        {
            availability = product.getAvailability().iterator().next();
        }
        else
        {
            availability = modelService.create(ProductAvailabilityAssignmentModel.class);
            availability.setCatalogVersion(catalogVersion);
            availability.setProduct(product);
            availability.setAvailabilityGroup(availabilityGroup);
            
            availability.setOnlineDate(DateUtils.addDays(new Date(System.currentTimeMillis()), -10));
        }
        
        availability.setStatus(approved);
        modelService.saveAll(availability, product);
    }
    
}