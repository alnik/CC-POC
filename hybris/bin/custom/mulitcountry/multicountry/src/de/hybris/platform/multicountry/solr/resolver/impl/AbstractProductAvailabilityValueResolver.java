package de.hybris.platform.multicountry.solr.resolver.impl;

import com.google.common.collect.Sets;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.multicountry.solr.search.strategies.ActiveProductAvailabilityGroupsStrategy;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;
import de.hybris.platform.variants.model.VariantProductModel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.Set;

/**
 * Created on 6/6/16.
 */
public abstract class AbstractProductAvailabilityValueResolver<T> extends AbstractValueResolver<ProductModel, Collection<ProductAvailabilityGroupModel>, Object>  {
    protected static final Logger LOG = Logger.getLogger(AbstractProductAvailabilityValueResolver.class);

    private ActiveProductAvailabilityGroupsStrategy activeProductAvailabilityGroupsStrategy;

    protected Collection<ProductAvailabilityGroupModel> loadData(final IndexerBatchContext batchContext, final Collection<IndexedProperty> indexedProperties,
            final ProductModel model) throws FieldValueProviderException
    {
        return activeProductAvailabilityGroupsStrategy.getAllActiveGroups(model);
    }

    @Override
    protected void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
            final IndexedProperty indexedProperty, final ProductModel model,
            final ValueResolverContext<Collection<ProductAvailabilityGroupModel>, Object>  resolverContext) throws FieldValueProviderException {

        {
            // We look up to all the variant and parent groups!
            // If the variant does not have a value, we should look it up on its base product (or base product's base product)
            final Collection<ProductAvailabilityGroupModel> groups = resolverContext.getData();

            for (final ProductAvailabilityGroupModel group : groups)
            {
                final Object fieldValue = getPropertyValue(model, group);

                //Get the name(s) of the index field(s)...
                document.addField(indexedProperty, fieldValue, group.getId());

            }

        }

    }


    /**
     * Return the value of a property defined in {@link IndexedProperty}. If the property is null we look at the value on
     * the base product.
     *
     * @param group
     * @return the propert value to index
     */
    protected Object getPropertyValue(final ProductModel product, final ProductAvailabilityGroupModel group)
    {
        // Collect all availability assignments
        final Set<ProductAvailabilityAssignmentModel> assignments = Sets.newHashSet();
        ProductModel current = product;
        while (current != null)
        {
            final ProductAvailabilityAssignmentModel assignment = getAvailabilityAssignment(current, group);
            if (assignment != null)
            {
                assignments.add(assignment);
            }

            // Get parent product
            if (current instanceof VariantProductModel)
            {
                current = ((VariantProductModel) current).getBaseProduct();
            }
            else
            {
                current = null;
            }
        }

        // Compute the final value
        return internalGetValue(assignments);
    }

    abstract protected T internalGetValue(Collection<ProductAvailabilityAssignmentModel> availabilityAssignments);

    /**
     * Look for an availability assignment in a specific availability group
     *
     * @param product
     *           A product
     * @param group
     *           An availability group
     */
    protected ProductAvailabilityAssignmentModel getAvailabilityAssignment(final ProductModel product,
            final ProductAvailabilityGroupModel group)
    {
        ProductAvailabilityAssignmentModel baseProductAvailability = null;
        for (final ProductAvailabilityAssignmentModel availability : product.getAvailability())
        {
            if (availability.getAvailabilityGroup().equals(group))
            {
                baseProductAvailability = availability;
            }
        }
        return baseProductAvailability;
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