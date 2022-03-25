package de.hybris.platform.multicountry.solr.search.populators.impl;

import static org.mockito.BDDMockito.given;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SearchQueryPageableData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchQueryData;
import de.hybris.platform.commerceservices.search.solrfacetsearch.data.SolrSearchRequest;
import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.multicountry.services.MulticountryRestrictionService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.SearchConfig;
import de.hybris.platform.solrfacetsearch.search.FacetValueField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;
import org.apache.solr.client.solrj.SolrQuery;
import org.assertj.core.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


/**
 * @author i303807
 */
@UnitTest
public class ProductAvailabilityGroupPopulatorTest {

    private ProductAvailabilityGroupPopulator productAvailabilityGroupPopulator;

    @Mock
    private MulticountryRestrictionService multiCountryRestrictionService;

    @Before
    public void setUp() throws ImpExException {
        MockitoAnnotations.initMocks(this);
        final Collection<ProductAvailabilityGroupModel> groups = new ArrayList<ProductAvailabilityGroupModel>();

        // mocking all needed classes
        given(multiCountryRestrictionService.getCurrentProductAvailabilityGroup()).willReturn(groups);

        productAvailabilityGroupPopulator = new ProductAvailabilityGroupPopulator(multiCountryRestrictionService);
    }

    @Test
    public void populateShouldBeEmpty() {
        SolrQuery target = new SolrQuery();

        // populate target object
        productAvailabilityGroupPopulator.populate(new SearchQueryConverterData(), target);

        Assert.assertTrue(Arrays.isNullOrEmpty(target.getFilterQueries()));
    }

    @Test
    public void populateShouldNotBeEmpty() {
        // Prepare source and target data
        final Collection<ProductAvailabilityGroupModel> groups = new ArrayList<ProductAvailabilityGroupModel>();
        ProductAvailabilityGroupModel group1 = new ProductAvailabilityGroupModel();
        ProductAvailabilityGroupModel group2 = new ProductAvailabilityGroupModel();
        group1.setId("TestGroup1");
        group2.setId("TestGroup2");
        groups.add(group1);
        groups.add(group2);

        given(multiCountryRestrictionService.getCurrentProductAvailabilityGroup()).willReturn(groups);
        SearchQueryConverterData source = new SearchQueryConverterData();
        SolrQuery target = new SolrQuery();

        // populate target object
        productAvailabilityGroupPopulator.populate(source, target);

        Assert.assertEquals("availability_string_mv:(TestGroup1 OR TestGroup2)",
            target.getFilterQueries()[0]);
    }
}
