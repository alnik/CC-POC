package de.hybris.platform.multicountry.solr.search.populators.impl;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.multicountry.services.MulticountryRestrictionService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author pawan.shrivastava
 *
 */
public class ProductAvailabilityGroupPopulator implements Populator<SearchQueryConverterData, SolrQuery> {
    private static final String FQ_FORMAT = "availability_string_mv:(%s)";
    private static final String OR = " OR ";
    private MulticountryRestrictionService multicountryRestrictionService;

    public ProductAvailabilityGroupPopulator(MulticountryRestrictionService multicountryRestrictionService) {
        this.multicountryRestrictionService = multicountryRestrictionService;
    }

    @Override
    public void populate(final SearchQueryConverterData source, final SolrQuery target)
        throws ConversionException {
        final Collection<ProductAvailabilityGroupModel> availabilityGroups =
            multicountryRestrictionService.getCurrentProductAvailabilityGroup();

        if (!availabilityGroups.isEmpty()) {
            String groups = availabilityGroups.stream()
                .map(ProductAvailabilityGroupModel::getId)
                .collect(Collectors.joining(OR));
            target.addFilterQuery(String.format(FQ_FORMAT, groups));
        }
    }
}
