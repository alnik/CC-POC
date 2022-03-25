/**
 *
 */
package de.hybris.platform.multicountry.solr.search.strategies.impl;

import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityAssignmentModel;
import de.hybris.platform.multicountry.model.productavailabilitygroup.ProductAvailabilityGroupModel;
import de.hybris.platform.multicountry.solr.search.strategies.ActiveProductAvailabilityGroupsStrategy;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.Set;

import com.google.common.collect.Sets;


/**
 * There should not be any restriction active on product availability assignments for this strategy to work. (i.e. we
 * should see all unapproved product assignment).
 *
 * @author marco.rolando
 *
 */
public class DefaultActiveProductAvailabilityGroupsStrategy implements ActiveProductAvailabilityGroupsStrategy
{
	@Override
	public Set<ProductAvailabilityGroupModel> getAllActiveGroups(final ProductModel product)
	{
		final Set<ProductAvailabilityGroupModel> groups = internalGetAllActiveGroups(product);
		return groups;
	}

	protected Set<ProductAvailabilityGroupModel> internalGetAllActiveGroups(final ProductModel product)
	{
		final Set<ProductAvailabilityGroupModel> groups;

		// If this is a variant, recursively collect the parent availability groups and remove the groups
		// that belongs to unapproved product availability groups on the variant.
		if (product instanceof VariantProductModel)
		{
			groups = internalGetAllActiveGroups(((VariantProductModel) product).getBaseProduct());

			for (final ProductAvailabilityAssignmentModel availability : product.getAvailability())
			{
				if (!isApproved(availability))
				{
					groups.remove(availability.getAvailabilityGroup());
				}
			}
		}
		else
		{
			// ..else, collect all active groups
			groups = Sets.newHashSet();
			if (product.getAvailability() != null && !product.getAvailability().isEmpty())
			{
				for (final ProductAvailabilityAssignmentModel availability : product.getAvailability())
				{
					if (isApproved(availability))
					{
						groups.add(availability.getAvailabilityGroup());
					}
				}
			}

		}
		return groups;
	}

	/**
	 * Determines if the product assignment is approved
	 *
	 */
	protected boolean isApproved(final ProductAvailabilityAssignmentModel productAvailabilityAssignmentModel)
	{
		return ArticleApprovalStatus.APPROVED.equals(productAvailabilityAssignmentModel.getStatus());
	}
}
