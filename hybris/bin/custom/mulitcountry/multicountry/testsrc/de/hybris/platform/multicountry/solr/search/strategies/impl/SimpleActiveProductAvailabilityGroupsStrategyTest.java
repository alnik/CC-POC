package de.hybris.platform.multicountry.solr.search.strategies.impl;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import javax.annotation.Resource;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author daniel.norberg
 */
@IntegrationTest
public class SimpleActiveProductAvailabilityGroupsStrategyTest extends ServicelayerTransactionalTest {

    @Resource
    private DefaultActiveProductAvailabilityGroupsStrategy activeProductAvailabilityGroupsStrategy;


    private static final String GROUP1 = "product-availability-test-1";
    private static final String GROUP2 = "product-availability-test-2";

    @Resource
    private ProductService productService;

    @Resource
    private ModelService modelService;

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Resource
    private CatalogService catalogService;

    private ProductModel product;

    private ProductModel variant1;
    private ProductModel variant2;
    private ProductModel variant3;
    private ProductModel variant4;

    private CatalogVersionModel catalogVersion;
    private ProductAvailabilityGroupModel availabilityGroup1;
    private ProductAvailabilityGroupModel availabilityGroup2;

    @Before
    public void setUp() throws ImpExException {
        importCsv("/multicountry/test/testSearchRestriction.impex", "UTF-8");
        importCsv("/multicountry/test/testImpersonate.impex", "UTF-8");

        // Getting Catalog version and availability groups
        setAvailabilityGroup1(flexibleSearchService.<ProductAvailabilityGroupModel>search("SELECT {pk} FROM {ProductAvailabilityGroup} WHERE {id} = ?code", Collections.singletonMap("code", GROUP1)).getResult().iterator().next());
        setAvailabilityGroup2(flexibleSearchService.<ProductAvailabilityGroupModel>search("SELECT {pk} FROM {ProductAvailabilityGroup} WHERE {id} = ?code", Collections.singletonMap("code", GROUP2)).getResult().iterator().next());

        catalogVersion = catalogService.getAllCatalogs().iterator().next().getCatalogVersions().iterator().next();

        // Get base product
        product = getProductForCode("TestProduct");

        // Get variant product
        setVariant1(getProductForCode("TestProduct_2_1"));
        setVariant2(getProductForCode("TestProduct_2_2"));
        setVariant3(getProductForCode("TestProduct_1_1"));
        setVariant4(getProductForCode("TestProduct_1_2"));
    }

    @Test
    public void populateShouldBeEmpty() {
        // Apply Availability status for products
        setStatus(product, getAvailabilityGroup1(), ArticleApprovalStatus.CHECK);

        // Getting all active availability assignments
        Assert.assertTrue(activeProductAvailabilityGroupsStrategy.getAllActiveGroups(product).isEmpty());
    }

    @Test
    public void populateShouldNotBeEmpty() {
        // Apply Availability status for products
        setStatus(product, getAvailabilityGroup1(), ArticleApprovalStatus.APPROVED);
        setStatus(product, getAvailabilityGroup2(), ArticleApprovalStatus.APPROVED);

        // Getting all active availability assignments
        final Set<ProductAvailabilityGroupModel> groups = activeProductAvailabilityGroupsStrategy.getAllActiveGroups(product);
        Assert.assertEquals(2, groups.size());
    }

    private ProductModel getProductForCode(final String code) {
        return productService.getProductForCode(code);
    }

    private void setStatus(final ProductModel product, final ProductAvailabilityGroupModel availGroup, final ArticleApprovalStatus approved) {
        final ProductAvailabilityAssignmentModel availability = modelService.create(ProductAvailabilityAssignmentModel.class);
        availability.setCatalogVersion(catalogVersion);
        availability.setProduct(product);
        availability.setAvailabilityGroup(availGroup);
        availability.setOnlineDate(DateUtils.addDays(new Date(System.currentTimeMillis()), -10));
        availability.setStatus(approved);
        modelService.save(availability);
    }

    public ProductModel getVariant1() {
        return variant1;
    }

    public void setVariant1(final ProductModel variant1) {
        this.variant1 = variant1;
    }

    public ProductModel getVariant2() {
        return variant2;
    }

    public void setVariant2(final ProductModel variant2) {
        this.variant2 = variant2;
    }

    public ProductModel getVariant3() {
        return variant3;
    }

    public void setVariant3(final ProductModel variant3) {
        this.variant3 = variant3;
    }

    public ProductModel getVariant4() {
        return variant4;
    }

    public void setVariant4(final ProductModel variant4) {
        this.variant4 = variant4;
    }

    public ProductAvailabilityGroupModel getAvailabilityGroup2() {
        return availabilityGroup2;
    }

    public void setAvailabilityGroup2(final ProductAvailabilityGroupModel availabilityGroup2) {
        this.availabilityGroup2 = availabilityGroup2;
    }

    public ProductAvailabilityGroupModel getAvailabilityGroup1() {
        return availabilityGroup1;
    }

    public void setAvailabilityGroup1(final ProductAvailabilityGroupModel availabilityGroup1) {
        this.availabilityGroup1 = availabilityGroup1;
    }
}
