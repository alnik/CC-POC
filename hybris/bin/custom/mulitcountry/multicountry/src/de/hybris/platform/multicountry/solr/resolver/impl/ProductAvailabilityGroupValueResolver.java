package de.hybris.platform.multicountry.solr.resolver.impl;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.multicountry.solr.search.strategies.ActiveProductAvailabilityGroupsStrategy;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

/**
 * Created on 6/8/16.
 */
public class ProductAvailabilityGroupValueResolver extends
        AbstractValueResolver<ProductModel, Collection<ProductAvailabilityGroupModel>, Object> {

    private ActiveProductAvailabilityGroupsStrategy activeProductAvailabilityGroupsStrategy;

    protected Collection<ProductAvailabilityGroupModel> loadData(final IndexerBatchContext batchContext, final Collection<IndexedProperty> indexedProperties,
    final ProductModel model) throws FieldValueProviderException
    {
        return activeProductAvailabilityGroupsStrategy.getAllActiveGroups(model);
    }

    @Override protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
            final IndexedProperty indexedProperty, final ProductModel model,
            final ValueResolverContext<Collection<ProductAvailabilityGroupModel>, Object> resolverContext)
            throws FieldValueProviderException {

        //Get all the availability group from the product and, if it's a variant, from its parent products.
        final Collection<ProductAvailabilityGroupModel> groups = resolverContext.getData();

        for (final ProductAvailabilityGroupModel group : groups)
        {
            final Object fieldValue = group.getId();

            document.addField(indexedProperty, fieldValue);
        }
    }


    /**
     * @param activeProductAvailabilityGroupsStrategy
     *           the activeProductAvailabilityGroupsStrategy to set
     */
    @Required
    public void setActiveProductAvailabilityGroupsStrategy(
            final ActiveProductAvailabilityGroupsStrategy activeProductAvailabilityGroupsStrategy)
    {
        this.activeProductAvailabilityGroupsStrategy = activeProductAvailabilityGroupsStrategy;
    }
}
