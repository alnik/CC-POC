/**
 *
 */
package de.hybris.platform.multicountry.search;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogVersionModel;
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

import java.util.Collections;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;


/**
 * The structure of the data is as follows
 *
 * ApparelProduct TestProduct ------------------------------------------------------------------------
 * ---ApparelStyleVariantProduct TestProduct_1 -------------------------------------------------------
 * ------ApparelSizeVariantProduct TestProduct_1_1 ---------------------------------------------------
 * ------ApparelSizeVariantProduct TestProduct_1_2 ---------------------------------------------------
 * ---ApparelStyleVariantProduct TestProduct_2 -------------------------------------------------------
 * ------ApparelSizeVariantProduct TestProduct_2_1 ---------------------------------------------------
 * ------ApparelSizeVariantProduct TestProduct_2_2 ---------------------------------------------------
 */
@IntegrationTest
public class SearchRestrictionTest extends ServicelayerTransactionalTest
{
    private static final String VERSION = "TestVersion";
    private static final String GROUP = "product-availability-test-1";
    
    private static int ALL_PRODUCTS_COUNT = 7;
    
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
    
    private ProductModel product;
    private ProductModel variant1;
    private ProductModel variant2;
    private ProductModel variant11;
    private ProductModel variant12;
    private ProductModel variant21;
    private ProductModel variant22;
    
    private CatalogVersionModel catalogVersion;
    private ProductAvailabilityGroupModel availabilityGroup;
    
    @Before
    public void setUp() throws ImpExException
    {
        // Perform import and setup an admin user
        userService.setCurrentUser(userService.getAdminUser());
        
        importCsv("/multicountry/test/testSearchRestriction.impex", "UTF-8");
        importCsv("/multicountry/test/essentialdata_multicountry.impex", "UTF-8");
        
        //		patchSearchRestrictions();
        
        catalogVersion = flexibleSearchService
        .<CatalogVersionModel> search("SELECT {pk} FROM {CatalogVersion} WHERE {version} = ?version",
                                      Collections.singletonMap("version", VERSION))
        .getResult().iterator().next();
        
        final String group = GROUP;
        availabilityGroup = flexibleSearchService
        .<ProductAvailabilityGroupModel> search("SELECT {pk} FROM {ProductAvailabilityGroup} WHERE {id} = ?code",
                                                Collections.singletonMap("code", group))
        .getResult().iterator().next();
        
        // Get products
        product = getProductForCode("TestProduct");
        
        variant1 = getProductForCode("TestProduct_1");
        variant11 = getProductForCode("TestProduct_1_1");
        variant12 = getProductForCode("TestProduct_1_2");
        
        variant2 = getProductForCode("TestProduct_2");
        variant21 = getProductForCode("TestProduct_2_1");
        variant22 = getProductForCode("TestProduct_2_2");
        
        // Active session [always last!]
        catalogVersionService.setSessionCatalogVersions(Lists.newArrayList(catalogVersion));
        userService.setCurrentUser(userService.getAnonymousUser());
        sessionService.setAttribute(MulticountryConstants.AVAILABILITY_GROUPS, Lists.newArrayList(availabilityGroup));
    }
    
    @Test
    public void allProductsVisibleWhenBaseEnabled()
    {
        setStatus(product, ArticleApprovalStatus.APPROVED);
        checkCount(getProductsCount(), ALL_PRODUCTS_COUNT);
    }
    
    @Test
    public void noProductVisibleWhenBaseDisabled()
    {
        setStatus(product, ArticleApprovalStatus.CHECK);
        checkCount(getProductsCount(), 0);
    }
    
    @Test
    public void noProductVisibleWhenNoAssignment()
    {
        checkCount(getProductsCount(), 0);
    }
    
    @Test
    public void noProductVisibleWhenOnlyVariantAssignments()
    {
        setStatus(variant1, ArticleApprovalStatus.APPROVED);
        setStatus(variant11, ArticleApprovalStatus.APPROVED);
        setStatus(variant21, ArticleApprovalStatus.APPROVED);
        checkCount(getProductsCount(), 0);
    }
    
    @Test
    public void noProductVisibleWhenBaseDisabledOnlyVariantAssignments()
    {
        setStatus(product, ArticleApprovalStatus.CHECK);
        setStatus(variant1, ArticleApprovalStatus.APPROVED);
        setStatus(variant11, ArticleApprovalStatus.APPROVED);
        setStatus(variant21, ArticleApprovalStatus.APPROVED);
        checkCount(getProductsCount(), 0);
    }
    
    @Test
    public void someProductsVisibleWhenSomeVariantsDisabled()
    {
        setStatus(product, ArticleApprovalStatus.APPROVED); // +7
        
        setStatus(variant1, ArticleApprovalStatus.CHECK); //  -3
        setStatus(variant11, ArticleApprovalStatus.APPROVED); // +-0
        
        setStatus(variant21, ArticleApprovalStatus.CHECK); // -1
        
        checkCount(getProductsCount(), 3);
    }
    
    @Test
    public void someProductsVisibleWhenSomeVariantDisabledTwo()
    {
        setStatus(product, ArticleApprovalStatus.APPROVED); // +1
        
        setStatus(variant1, ArticleApprovalStatus.APPROVED); // +1
        setStatus(variant11, ArticleApprovalStatus.APPROVED); // +1
        setStatus(variant12, ArticleApprovalStatus.UNAPPROVED); // +0
        
        setStatus(variant2, ArticleApprovalStatus.APPROVED); // +1
        setStatus(variant21, ArticleApprovalStatus.APPROVED); // +1
        setStatus(variant22, ArticleApprovalStatus.UNAPPROVED); // +0
        
        checkCount(getProductsCount(), 5);
    }
    
    private ProductModel getProductForCode(final String code)
    {
        return productService.getProductForCode(code);
    }
    
    private int getProductsCount()
    {
        return flexibleSearchService.search("SELECT {pk} FROM {Product} WHERE {code} LIKE 'TestProduct%'").getCount();
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
        }
        
        availability.setStatus(approved);
        modelService.save(availability);
        
    }
    
    private void checkCount(final int actualCount, final int predictedCount)
    {
        Assert.assertThat(Integer.valueOf(actualCount), is(equalTo(Integer.valueOf(predictedCount))));
    }
    
}